package com.example.core.molist

import com.example.core.db.AppDatabase
import com.example.core.db.CachedMoEntity
import com.example.core.model.MissionOrder
import com.example.core.model.Problem
import com.example.core.network.ConvexDataSource
import com.example.core.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class MoListResult(
  val items: List<MissionOrder>,
  val isCached: Boolean,
  val error: String? = null
)

class MoRepository(
  private val database: AppDatabase,
  private val convexDataSource: ConvexDataSource,
  private val networkMonitor: NetworkMonitor,
  private val scope: CoroutineScope
) {

  init {
    // Proactively pre-cache MOs on app launch and whenever network reconnects
    scope.launch(Dispatchers.IO) {
      syncCacheFromNetwork()
    }

    scope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          syncCacheFromNetwork()
        }
      }
    }
  }

  suspend fun syncCacheFromNetwork() = withContext(Dispatchers.IO) {
    try {
      val allMos = convexDataSource.listActiveMos("")
      if (allMos.isNotEmpty()) {
        saveToDb(allMos)
      }
    } catch (_: Exception) {
      // In offline or network error, cache remains intact
    }
  }

  fun getActiveMosFlow(search: String = ""): Flow<MoListResult> = flow {
    val q = search.trim().lowercase()

    // 1. Immediately emit cached data from SQLite Room database
    val cachedList = loadFromDb(q)
    if (cachedList.isNotEmpty()) {
      emit(MoListResult(items = cachedList, isCached = !networkMonitor.isOnline.value))
    }

    // 2. If online, fetch fresh data and update Room cache
    if (networkMonitor.isOnline.value) {
      try {
        // Fetch all active to ensure full cache persistence
        val allActiveMos = convexDataSource.listActiveMos("")
        if (allActiveMos.isNotEmpty()) {
          saveToDb(allActiveMos)
        }

        val filtered = if (q.isBlank()) {
          allActiveMos
        } else {
          allActiveMos.filter {
            it.moNumber.lowercase().contains(q) ||
              it.title.lowercase().contains(q) ||
              it.partReference.lowercase().contains(q)
          }
        }
        emit(MoListResult(items = filtered, isCached = false))
      } catch (e: Exception) {
        if (cachedList.isEmpty()) {
          emit(MoListResult(items = emptyList(), isCached = true, error = e.message ?: "Failed to load"))
        }
      }
    } else {
      // Offline mode
      if (cachedList.isEmpty()) {
        // If DB had not synced yet, seed from default catalog to guarantee offline availability
        val fallbackAll = convexDataSource.listActiveMos("")
        saveToDb(fallbackAll)
        val refreshed = loadFromDb(q)
        emit(MoListResult(items = refreshed, isCached = true))
      }
    }
  }

  suspend fun getMoForInspection(moId: String): MissionOrder? = withContext(Dispatchers.IO) {
    if (networkMonitor.isOnline.value) {
      try {
        val live = convexDataSource.getForInspection(moId)
        if (live != null) {
          // Update cached version
          saveToDb(listOf(live))
          return@withContext live
        }
      } catch (_: Exception) {}
    }

    // Offline fallback: load from Room database
    val entity = database.cachedMoDao().getMoById(moId)
    if (entity != null) {
      return@withContext entity.toDomain()
    }

    // Fallback lookup
    return@withContext convexDataSource.getForInspection(moId)
  }

  private suspend fun loadFromDb(search: String): List<MissionOrder> = withContext(Dispatchers.IO) {
    val entities = if (search.isBlank()) {
      database.cachedMoDao().getAllActiveMos()
    } else {
      database.cachedMoDao().searchActiveMos(search)
    }
    entities.map { it.toDomain() }
  }

  private suspend fun saveToDb(mos: List<MissionOrder>) = withContext(Dispatchers.IO) {
    val entities = mos.map { mo ->
      val problemsJson = JSONArray().apply {
        mo.problems.forEach { p ->
          put(JSONObject().apply {
            put("id", p.id)
            put("code", p.code)
            put("label", p.label)
            put("sortOrder", p.sortOrder)
          })
        }
      }.toString()

      CachedMoEntity(
        id = mo.id,
        moNumber = mo.moNumber,
        title = mo.title,
        partReference = mo.partReference,
        status = mo.status,
        problemsVersion = mo.problemsVersion,
        problemCount = mo.problems.size,
        problemsJson = problemsJson,
        lastUpdated = System.currentTimeMillis()
      )
    }
    database.cachedMoDao().insertMos(entities)
  }

  private fun CachedMoEntity.toDomain(): MissionOrder {
    val problemsList = mutableListOf<Problem>()
    try {
      val arr = JSONArray(problemsJson)
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        problemsList.add(
          Problem(
            id = obj.getString("id"),
            code = obj.getString("code"),
            label = obj.getString("label"),
            sortOrder = obj.optInt("sortOrder", i)
          )
        )
      }
    } catch (_: Exception) {}

    return MissionOrder(
      id = id,
      moNumber = moNumber,
      title = title,
      partReference = partReference,
      status = status,
      problemsVersion = problemsVersion,
      problemCount = problemCount,
      problems = problemsList
    )
  }
}
