package com.example.core.outbox

import com.example.core.db.AppDatabase
import com.example.core.db.SubmissionEntity
import com.example.core.model.InspectionDraft
import com.example.core.model.InspectionSubmission
import com.example.core.model.SubmissionStatus
import com.example.core.network.ConvexDataSource
import com.example.core.network.NetworkMonitor
import com.example.core.network.SubmitResult
import com.example.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class OutboxSyncManager(
  private val database: AppDatabase,
  private val networkMonitor: NetworkMonitor,
  private val convexDataSource: ConvexDataSource,
  private val sessionManager: SessionManager,
  private val scope: CoroutineScope
) {
  val pendingCount: Flow<Int> = database.submissionDao().getPendingCount()

  val allSubmissions: Flow<List<InspectionSubmission>> =
    database.submissionDao().getAllSubmissions().map { entities ->
      entities.map { it.toDomain() }
    }

  init {
    // Automatically trigger sync when connectivity is restored
    scope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          syncPendingOutbox()
        }
      }
    }
  }

  suspend fun submitInspection(draft: InspectionDraft): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val currentUser = sessionManager.currentUser.value
      ?: return@withContext false to "Operator session required"

    val submissionId = UUID.randomUUID().toString()
    val countsJson = JSONObject(draft.defectCounts).toString()

    val entity = SubmissionEntity(
      id = submissionId,
      userId = currentUser.id,
      userNumber = currentUser.userNumber,
      moId = draft.moId,
      moNumber = draft.moNumber,
      partSerial = draft.partSerial,
      boxId = draft.boxId,
      quantity = draft.quantity,
      defectCountsJson = countsJson,
      totalDefects = draft.totalDefects,
      goodQuantity = draft.goodQuantity,
      status = SubmissionStatus.PENDING.name,
      clientSubmittedAt = System.currentTimeMillis(),
      deviceLabel = "Zebra TC51",
      appVersion = "1.0.0",
      attempts = 0,
      lastError = null,
      createdAt = System.currentTimeMillis()
    )

    // 1. Insert into local Room database outbox
    database.submissionDao().insertSubmission(entity)

    // 2. If online, attempt immediate sync
    if (networkMonitor.isOnline.value) {
      val success = trySendSubmission(entity)
      if (success) {
        return@withContext true to "SENT"
      }
    }

    return@withContext true to "SAVED_LOCAL"
  }

  suspend fun syncPendingOutbox() = withContext(Dispatchers.IO) {
    val pendingList = database.submissionDao().getPendingSubmissions()
    for (item in pendingList) {
      if (!networkMonitor.isOnline.value) break
      trySendSubmission(item)
    }
  }

  suspend fun retryAll() = withContext(Dispatchers.IO) {
    syncPendingOutbox()
  }

  suspend fun deleteSubmission(id: String) = withContext(Dispatchers.IO) {
    database.submissionDao().deleteSubmission(id)
  }

  private suspend fun trySendSubmission(entity: SubmissionEntity): Boolean {
    val token = sessionManager.token.value ?: return false
    val countsMap = parseCountsJson(entity.defectCountsJson)

    database.submissionDao().updateStatus(entity.id, SubmissionStatus.SENDING.name)

    val result = convexDataSource.submitInspection(
      token = token,
      clientSubmissionId = entity.id,
      moId = entity.moId,
      partSerial = entity.partSerial,
      boxId = entity.boxId,
      quantity = entity.quantity,
      counts = countsMap,
      clientSubmittedAt = entity.clientSubmittedAt,
      deviceLabel = entity.deviceLabel,
      appVersion = entity.appVersion
    )

    return when (result) {
      is SubmitResult.Success -> {
        database.submissionDao().updateStatus(entity.id, SubmissionStatus.SENT.name, null)
        true
      }
      is SubmitResult.Rejected -> {
        database.submissionDao().updateStatus(entity.id, SubmissionStatus.REJECTED.name, result.reason)
        false
      }
      is SubmitResult.Retryable -> {
        database.submissionDao().updateStatus(entity.id, SubmissionStatus.PENDING.name, result.message)
        false
      }
      is SubmitResult.AuthRequired -> {
        database.submissionDao().updateStatus(entity.id, SubmissionStatus.PENDING.name, "Session expired")
        false
      }
    }
  }

  private fun parseCountsJson(jsonStr: String): Map<String, Int> {
    return try {
      val obj = JSONObject(jsonStr)
      val map = mutableMapOf<String, Int>()
      val keys = obj.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        map[key] = obj.getInt(key)
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun SubmissionEntity.toDomain(): InspectionSubmission {
    return InspectionSubmission(
      id = id,
      userId = userId,
      userNumber = userNumber,
      moId = moId,
      moNumber = moNumber,
      partSerial = partSerial,
      boxId = boxId,
      quantity = quantity,
      defectCounts = parseCountsJson(defectCountsJson),
      totalDefects = totalDefects,
      goodQuantity = goodQuantity,
      status = try { SubmissionStatus.valueOf(status) } catch (_: Exception) { SubmissionStatus.PENDING },
      clientSubmittedAt = clientSubmittedAt,
      deviceLabel = deviceLabel,
      appVersion = appVersion,
      attempts = attempts,
      lastError = lastError,
      createdAt = createdAt
    )
  }
}
