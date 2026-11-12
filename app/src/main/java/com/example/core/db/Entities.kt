package com.example.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_submissions")
data class SubmissionEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val userNumber: String,
  val moId: String,
  val moNumber: String,
  val partSerial: String,
  val boxId: String,
  val quantity: Int,
  val defectCountsJson: String,
  val totalDefects: Int,
  val goodQuantity: Int,
  val status: String,
  val clientSubmittedAt: Long,
  val deviceLabel: String,
  val appVersion: String,
  val attempts: Int,
  val lastError: String?,
  val createdAt: Long
)

@Entity(tableName = "cached_mos")
data class CachedMoEntity(
  @PrimaryKey val id: String,
  val moNumber: String,
  val title: String,
  val partReference: String,
  val status: String,
  val problemsVersion: Int,
  val problemCount: Int,
  val problemsJson: String,
  val lastUpdated: Long
)
