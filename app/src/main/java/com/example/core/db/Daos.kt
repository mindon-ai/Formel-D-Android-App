package com.example.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
  @Query("SELECT * FROM pending_submissions ORDER BY createdAt DESC")
  fun getAllSubmissions(): Flow<List<SubmissionEntity>>

  @Query("SELECT * FROM pending_submissions WHERE status = 'PENDING' ORDER BY createdAt ASC")
  suspend fun getPendingSubmissions(): List<SubmissionEntity>

  @Query("SELECT COUNT(*) FROM pending_submissions WHERE status = 'PENDING' OR status = 'SENDING'")
  fun getPendingCount(): Flow<Int>

  @Query("SELECT * FROM pending_submissions WHERE id = :id")
  suspend fun getById(id: String): SubmissionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubmission(entity: SubmissionEntity)

  @Update
  suspend fun updateSubmission(entity: SubmissionEntity)

  @Query("UPDATE pending_submissions SET status = :status, attempts = attempts + 1, lastError = :lastError WHERE id = :id")
  suspend fun updateStatus(id: String, status: String, lastError: String? = null)

  @Query("DELETE FROM pending_submissions WHERE id = :id")
  suspend fun deleteSubmission(id: String)

  @Query("DELETE FROM pending_submissions WHERE status = 'SENT' AND createdAt < :cutoff")
  suspend fun pruneSentSubmissions(cutoff: Long)
}

@Dao
interface CachedMoDao {
  @Query("SELECT * FROM cached_mos WHERE status = 'ACTIVE' ORDER BY moNumber ASC")
  fun getActiveMos(): Flow<List<CachedMoEntity>>

  @Query("SELECT * FROM cached_mos WHERE status = 'ACTIVE' ORDER BY moNumber ASC")
  suspend fun getAllActiveMos(): List<CachedMoEntity>

  @Query("SELECT * FROM cached_mos WHERE status = 'ACTIVE' AND (moNumber LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR partReference LIKE '%' || :query || '%') ORDER BY moNumber ASC")
  suspend fun searchActiveMos(query: String): List<CachedMoEntity>

  @Query("SELECT * FROM cached_mos WHERE id = :moId")
  suspend fun getMoById(moId: String): CachedMoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMos(mos: List<CachedMoEntity>)

  @Query("DELETE FROM cached_mos")
  suspend fun clearAll()
}
