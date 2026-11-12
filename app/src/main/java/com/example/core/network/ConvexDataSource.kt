package com.example.core.network

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.MissionOrder
import com.example.core.model.Problem
import com.example.core.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SubmitResult {
  data class Success(val inspectionId: String, val duplicate: Boolean = false) : SubmitResult()
  data class Retryable(val message: String) : SubmitResult()
  data class Rejected(val reason: String) : SubmitResult()
  object AuthRequired : SubmitResult()
}

class ConvexDataSource(context: Context? = null) {
  private val prefs: SharedPreferences? =
    context?.getSharedPreferences("formeld_convex_prefs", Context.MODE_PRIVATE)

  var backendUrl: String
    get() = prefs?.getString("convex_backend_url", "https://formeld-renault-qc.convex.cloud")
      ?: "https://formeld-renault-qc.convex.cloud"
    set(value) {
      prefs?.edit()?.putString("convex_backend_url", value.trim())?.apply()
    }

  var mockLatencyMs: Long = 300L

  private val client = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .writeTimeout(8, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val mockUsers = mutableListOf(
    User("usr_025", "025", "Jean Dupont", "Quality Inspector (Operator)"),
    User("usr_233", "233", "Marie Curie", "Senior Quality Controller"),
    User("usr_042", "042", "Alexandre Dumas", "Shift Lead Inspector", mustChangePassword = false),
    User("usr_101", "101", "Camille Laurent", "Quality Auditor (Operator)", mustChangePassword = true)
  )

  private val mockMos = mutableListOf(
    MissionOrder(
      id = "mo_0891",
      moNumber = "MO-2026-0891",
      title = "Brake Caliper Front Right - Megane IV",
      partReference = "RN-7701208945",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 7,
      problems = listOf(
        Problem("p1", "DEF-01", "Rayures / Surface Scratches", 1),
        Problem("p2", "DEF-02", "Enfoncement / Dent & Deformation", 2),
        Problem("p3", "DEF-03", "Hors cote / Out of Tolerance", 3),
        Problem("p4", "DEF-04", "Bavure / Burrs & Sharp Edges", 4),
        Problem("p5", "DEF-05", "Défaut peinture / Coating Defect", 5),
        Problem("p6", "DEF-06", "Filetage abîmé / Damaged Thread", 6),
        Problem("p7", "DEF-07", "Perçage manquant / Missing Hole", 7)
      )
    ),
    MissionOrder(
      id = "mo_0902",
      moNumber = "MO-2026-0902",
      title = "Steering Knuckle Left - Clio V",
      partReference = "RN-8200781234",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 6,
      problems = listOf(
        Problem("p11", "DEF-01", "Rayures / Surface Scratches", 1),
        Problem("p12", "DEF-02", "Enfoncement / Dent", 2),
        Problem("p13", "DEF-04", "Bavure usinage / Machining Burr", 3),
        Problem("p14", "DEF-08", "Fissure / Micro Crack", 4),
        Problem("p15", "DEF-06", "Filetage / Thread Fault", 5),
        Problem("p16", "DEF-09", "Traces oxydation / Corrosion", 6)
      )
    ),
    MissionOrder(
      id = "mo_0915",
      moNumber = "MO-2026-0915",
      title = "Turbocharger Wastegate Actuator - Austral E-Tech",
      partReference = "RN-144116528R",
      status = "ACTIVE",
      problemsVersion = 2,
      problemCount = 5,
      problems = listOf(
        Problem("p21", "DEF-01", "Rayure tige / Rod Scratch", 1),
        Problem("p22", "DEF-03", "Jeu axial / Axial Clearance", 2),
        Problem("p23", "DEF-10", "Défaut soudure / Weld Defect", 3),
        Problem("p24", "DEF-11", "Connecteur cassé / Broken Pin", 4),
        Problem("p25", "DEF-05", "Défaut étanchéité / Seal Leak", 5)
      )
    ),
    MissionOrder(
      id = "mo_0923",
      moNumber = "MO-2026-0923",
      title = "High Pressure Fuel Rail - Captur II",
      partReference = "RN-166004147R",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 6,
      problems = listOf(
        Problem("p31", "DEF-01", "Rayure portée joint / O-ring Scratch", 1),
        Problem("p32", "DEF-06", "Filetage injecteur / Injector Thread", 2),
        Problem("p33", "DEF-04", "Bavure interne / Internal Burr", 3),
        Problem("p34", "DEF-08", "Crique soudure / Weld Pore", 4),
        Problem("p35", "DEF-12", "Capteur desserré / Loose Sensor", 5),
        Problem("p36", "DEF-02", "Choc transport / Handling Dent", 6)
      )
    ),
    MissionOrder(
      id = "mo_0940",
      moNumber = "MO-2026-0940",
      title = "Rear Axle Suspension Arm - Dacia Duster III",
      partReference = "RN-551103982R",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 5,
      problems = listOf(
        Problem("p41", "DEF-02", "Déformation bras / Arm Bend", 1),
        Problem("p42", "DEF-05", "Manque cataphorèse / E-Coat Gap", 2),
        Problem("p43", "DEF-13", "Silentbloc déchiré / Bushing Tear", 3),
        Problem("p44", "DEF-07", "Alésage hors cote / Bore Deviation", 4),
        Problem("p45", "DEF-10", "Projection soudure / Weld Spatter", 5)
      )
    ),
    MissionOrder(
      id = "mo_0954",
      moNumber = "MO-2026-0954",
      title = "Door Latch Electric Central - Scenic E-Tech",
      partReference = "RN-805021590R",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 4,
      problems = listOf(
        Problem("p51", "DEF-14", "Blocage mécanique / Mechanism Jam", 1),
        Problem("p52", "DEF-11", "Câblage pincé / Pinched Wire", 2),
        Problem("p53", "DEF-01", "Rayure carter / Housing Scratch", 3),
        Problem("p54", "DEF-02", "Clip cassé / Broken Snap Fit", 4)
      )
    )
  )

  private val submittedIds = mutableSetOf<String>()

  suspend fun login(userNumber: String, pass: String): Pair<String, User>? = withContext(Dispatchers.IO) {
    val trimmedNumber = userNumber.trim()

    // 1. Try real Convex HTTP mutation if URL configured
    if (backendUrl.startsWith("https://") && !backendUrl.contains("formeld-renault-qc.convex.cloud")) {
      try {
        val payload = JSONObject().apply {
          put("path", "auth:login")
          put("args", JSONObject().apply {
            put("userNumber", trimmedNumber)
            put("password", pass)
            put("clientType", "android")
            put("deviceLabel", "Zebra TC51")
            put("appVersion", "1.0.0")
          })
        }.toString()

        val request = Request.Builder()
          .url("${backendUrl.trimEnd('/')}/api/mutation")
          .post(payload.toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: ""
          val json = JSONObject(respBody)
          if (json.optString("status") == "success") {
            val valObj = json.getJSONObject("value")
            val token = valObj.getString("token")
            val userObj = valObj.getJSONObject("user")
            val user = User(
              id = userObj.getString("id"),
              userNumber = userObj.optString("userNumber", trimmedNumber),
              displayName = userObj.getString("displayName"),
              role = userObj.optString("role", "Operator"),
              mustChangePassword = userObj.optBoolean("mustChangePassword", false)
            )
            return@withContext token to user
          }
        }
      } catch (_: Exception) {
        // Fallback to local demo data
      }
    }

    // 2. Fallback to local demo credentials
    delay(mockLatencyMs)
    val user = mockUsers.find { it.userNumber == trimmedNumber }
    if (user != null && pass.isNotBlank()) {
      val token = "tok_${user.id}_${System.currentTimeMillis()}"
      return@withContext token to user
    }
    return@withContext null
  }

  suspend fun changePassword(userNumber: String, newPass: String): Boolean = withContext(Dispatchers.IO) {
    if (backendUrl.startsWith("https://") && !backendUrl.contains("formeld-renault-qc.convex.cloud")) {
      try {
        val payload = JSONObject().apply {
          put("path", "auth:changePassword")
          put("args", JSONObject().apply {
            put("userNumber", userNumber.trim())
            put("newPassword", newPass)
          })
        }.toString()

        val request = Request.Builder()
          .url("${backendUrl.trimEnd('/')}/api/mutation")
          .post(payload.toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) return@withContext true
      } catch (_: Exception) {}
    }

    delay(mockLatencyMs)
    val index = mockUsers.indexOfFirst { it.userNumber == userNumber.trim() }
    if (index >= 0) {
      val user = mockUsers[index]
      mockUsers[index] = user.copy(mustChangePassword = false)
      return@withContext true
    }
    return@withContext false
  }

  suspend fun listActiveMos(search: String = ""): List<MissionOrder> = withContext(Dispatchers.IO) {
    val q = search.trim().lowercase()

    // 1. Try real Convex HTTP query
    if (backendUrl.startsWith("https://") && !backendUrl.contains("formeld-renault-qc.convex.cloud")) {
      try {
        val payload = JSONObject().apply {
          put("path", "mo:listActive")
          put("args", JSONObject().apply {
            if (q.isNotBlank()) put("search", q)
          })
        }.toString()

        val request = Request.Builder()
          .url("${backendUrl.trimEnd('/')}/api/query")
          .post(payload.toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: ""
          val json = JSONObject(respBody)
          if (json.optString("status") == "success") {
            val arr = json.getJSONArray("value")
            val list = mutableListOf<MissionOrder>()
            for (i in 0 until arr.length()) {
              val item = arr.getJSONObject(i)
              val problemsArray = item.optJSONArray("problems") ?: JSONArray()
              val problems = mutableListOf<Problem>()
              for (j in 0 until problemsArray.length()) {
                val p = problemsArray.getJSONObject(j)
                problems.add(
                  Problem(
                    id = p.getString("id"),
                    code = p.optString("code", "DEF-${j + 1}"),
                    label = p.getString("label"),
                    sortOrder = p.optInt("sortOrder", j)
                  )
                )
              }

              list.add(
                MissionOrder(
                  id = item.getString("id"),
                  moNumber = item.getString("moNumber"),
                  title = item.getString("title"),
                  partReference = item.getString("partReference"),
                  status = item.optString("status", "ACTIVE"),
                  problemsVersion = item.optInt("problemsVersion", 1),
                  problemCount = if (problems.isNotEmpty()) problems.size else item.optInt("problemCount", 0),
                  problems = problems
                )
              )
            }
            if (list.isNotEmpty()) return@withContext list
          }
        }
      } catch (_: Exception) {}
    }

    // 2. Fallback to local demo list
    delay(mockLatencyMs)
    if (q.isBlank()) return@withContext mockMos
    return@withContext mockMos.filter {
      it.moNumber.lowercase().contains(q) ||
        it.title.lowercase().contains(q) ||
        it.partReference.lowercase().contains(q)
    }
  }

  suspend fun getForInspection(moId: String): MissionOrder? = withContext(Dispatchers.IO) {
    if (backendUrl.startsWith("https://") && !backendUrl.contains("formeld-renault-qc.convex.cloud")) {
      try {
        val payload = JSONObject().apply {
          put("path", "mo:getForInspection")
          put("args", JSONObject().apply { put("moId", moId) })
        }.toString()

        val request = Request.Builder()
          .url("${backendUrl.trimEnd('/')}/api/query")
          .post(payload.toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: ""
          val json = JSONObject(respBody)
          if (json.optString("status") == "success") {
            val item = json.getJSONObject("value")
            val problemsArray = item.optJSONArray("problems") ?: JSONArray()
            val problems = mutableListOf<Problem>()
            for (j in 0 until problemsArray.length()) {
              val p = problemsArray.getJSONObject(j)
              problems.add(
                Problem(
                  id = p.getString("id"),
                  code = p.optString("code", "DEF-${j + 1}"),
                  label = p.getString("label"),
                  sortOrder = p.optInt("sortOrder", j)
                )
              )
            }

            return@withContext MissionOrder(
              id = item.getString("id"),
              moNumber = item.getString("moNumber"),
              title = item.getString("title"),
              partReference = item.optString("partReference", ""),
              status = item.optString("status", "ACTIVE"),
              problemsVersion = item.optInt("problemsVersion", 1),
              problemCount = problems.size,
              problems = problems
            )
          }
        }
      } catch (_: Exception) {}
    }

    delay(mockLatencyMs / 2)
    return@withContext mockMos.find { it.id == moId }
  }

  suspend fun submitInspection(
    token: String?,
    clientSubmissionId: String,
    moId: String,
    partSerial: String,
    boxId: String,
    quantity: Int,
    counts: Map<String, Int>,
    clientSubmittedAt: Long,
    deviceLabel: String,
    appVersion: String
  ): SubmitResult = withContext(Dispatchers.IO) {
    if (token.isNullOrBlank()) {
      return@withContext SubmitResult.AuthRequired
    }

    // 1. Try real Convex HTTP mutation
    if (backendUrl.startsWith("https://") && !backendUrl.contains("formeld-renault-qc.convex.cloud")) {
      try {
        val countsArray = JSONArray()
        counts.forEach { (pId, count) ->
          countsArray.put(JSONObject().apply {
            put("problemId", pId)
            put("count", count)
          })
        }

        val payload = JSONObject().apply {
          put("path", "inspections:submit")
          put("args", JSONObject().apply {
            put("token", token)
            put("clientSubmissionId", clientSubmissionId)
            put("moId", moId)
            put("partSerial", partSerial)
            put("boxId", boxId)
            put("quantity", quantity)
            put("counts", countsArray)
            put("clientSubmittedAt", clientSubmittedAt)
            put("deviceLabel", deviceLabel)
            put("appVersion", appVersion)
          })
        }.toString()

        val request = Request.Builder()
          .url("${backendUrl.trimEnd('/')}/api/mutation")
          .post(payload.toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        val respBody = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(respBody)
          if (json.optString("status") == "success") {
            val valObj = json.optJSONObject("value")
            val inspectionId = valObj?.optString("inspectionId") ?: "insp_${System.currentTimeMillis()}"
            val duplicate = valObj?.optBoolean("duplicate", false) ?: false
            return@withContext SubmitResult.Success(inspectionId = inspectionId, duplicate = duplicate)
          } else {
            val errMsg = json.optString("errorMessage", "Server error")
            if (errMsg.contains("UNAUTHORIZED", ignoreCase = true)) {
              return@withContext SubmitResult.AuthRequired
            }
            return@withContext SubmitResult.Rejected(errMsg)
          }
        } else {
          return@withContext SubmitResult.Retryable("Convex HTTP ${response.code}: $respBody")
        }
      } catch (e: Exception) {
        return@withContext SubmitResult.Retryable(e.message ?: "Network error connecting to Convex")
      }
    }

    // 2. Fallback to local mock submission
    delay(mockLatencyMs)

    if (submittedIds.contains(clientSubmissionId)) {
      return@withContext SubmitResult.Success(inspectionId = "insp_$clientSubmissionId", duplicate = true)
    }

    val mo = mockMos.find { it.id == moId }
    if (mo == null || mo.status != "ACTIVE") {
      return@withContext SubmitResult.Rejected("MO is not active or does not exist")
    }

    val totalDefects = counts.values.sum()
    if (totalDefects > quantity) {
      return@withContext SubmitResult.Rejected("Validation failed: Defect count ($totalDefects) exceeds quantity ($quantity)")
    }

    submittedIds.add(clientSubmissionId)
    return@withContext SubmitResult.Success(inspectionId = "insp_${System.currentTimeMillis()}", duplicate = false)
  }
}
