package com.example.core.model

import java.util.UUID

data class User(
  val id: String,
  val userNumber: String,
  val displayName: String,
  val role: String = "Operator",
  val mustChangePassword: Boolean = false
)

data class Problem(
  val id: String,
  val code: String,
  val label: String,
  val sortOrder: Int
)

data class MissionOrder(
  val id: String,
  val moNumber: String,
  val title: String,
  val partReference: String,
  val status: String = "ACTIVE",
  val problemsVersion: Int = 1,
  val problemCount: Int = 0,
  val problems: List<Problem> = emptyList()
)

data class InspectionDraft(
  val moId: String = "",
  val moNumber: String = "",
  val moTitle: String = "",
  val partReference: String = "",
  val partSerial: String = "",
  val boxId: String = "",
  val quantity: Int = 1,
  val defectCounts: Map<String, Int> = emptyMap()
) {
  val totalDefects: Int
    get() = defectCounts.values.sum()

  val goodQuantity: Int
    get() = (quantity - totalDefects).coerceAtLeast(0)

  val isExceedingQuantity: Boolean
    get() = totalDefects > quantity

  val isValidForStart: Boolean
    get() = partSerial.isNotBlank() && boxId.isNotBlank() && quantity >= 1

  val isValidForSend: Boolean
    get() = isValidForStart && !isExceedingQuantity
}

enum class SubmissionStatus {
  PENDING,
  SENDING,
  SENT,
  REJECTED
}

data class InspectionSubmission(
  val id: String = UUID.randomUUID().toString(),
  val userId: String,
  val userNumber: String,
  val moId: String,
  val moNumber: String,
  val partSerial: String,
  val boxId: String,
  val quantity: Int,
  val defectCounts: Map<String, Int>,
  val totalDefects: Int,
  val goodQuantity: Int,
  val status: SubmissionStatus = SubmissionStatus.PENDING,
  val clientSubmittedAt: Long = System.currentTimeMillis(),
  val deviceLabel: String = "Zebra TC51",
  val appVersion: String = "1.0.0",
  val attempts: Int = 0,
  val lastError: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

enum class ScannerMode {
  DATAWEDGE_BROADCAST,
  KEYBOARD_WEDGE
}

data class ScannerConfig(
  val mode: ScannerMode = ScannerMode.DATAWEDGE_BROADCAST,
  val intentAction: String = "com.dwexample.ACTION",
  val extraKey: String = "com.symbol.datawedge.data_string",
  val autoAdvance: Boolean = true,
  val hapticFeedback: Boolean = true,
  val audioBeep: Boolean = true,
  val simulatedOffline: Boolean = false
)
