package com.example.feature.inspection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Problem
import com.example.core.outbox.OutboxSyncManager
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.components.NumberStepperField
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcErrorBg
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.QcSuccessGreen
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun ProblemsScreen(
  draftViewModel: InspectionDraftViewModel,
  outboxSyncManager: OutboxSyncManager,
  onNavigateToResult: (status: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  val draft by draftViewModel.draft.collectAsState()
  val selectedMo by draftViewModel.selectedMo.collectAsState()

  var isSubmitting by remember { mutableStateOf(false) }
  var showAllGoodConfirmDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val scope = rememberCoroutineScope()
  val problems = selectedMo?.problems ?: emptyList()

  val totalDefects = draft.totalDefects
  val goodCount = draft.goodQuantity
  val isExceeded = draft.isExceedingQuantity

  fun performSend() {
    isSubmitting = true
    errorMessage = null
    scope.launch {
      val (success, status) = outboxSyncManager.submitInspection(draft)
      isSubmitting = false
      if (success) {
        onNavigateToResult(status)
      } else {
        errorMessage = status
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
  ) {
    // STICKY TOP SUMMARY HEADER (Compact 2 lines)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(IndustrialSurface)
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Column {
        // Line 1: MO, Serial, Box, Qty
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "${draft.moNumber} • ${draft.partSerial}",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = RenaultAmber
          )
          Text(
            text = "Box: ${draft.boxId}",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = TextHighContrast
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Line 2: Live status line: Defects X of Qty, Good: Y
        val statusBg = if (isExceeded) QcErrorBg else Color(0xFF1E293B)
        val statusBorder = if (isExceeded) QcErrorRed else IndustrialCardBorder

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .background(statusBg, RoundedCornerShape(8.dp))
            .border(1.5.dp, statusBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "TOTAL QTY: ",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TextMuted
            )
            Text(
              text = "${draft.quantity}",
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              color = TextHighContrast
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "DEFECTS: ",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isExceeded) QcErrorRed else RenaultAmber
            )
            Text(
              text = "$totalDefects",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = if (isExceeded) QcErrorRed else RenaultAmber
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "GOOD: ",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = QcSuccessGreen
            )
            Text(
              text = "$goodCount",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = QcSuccessGreen
            )
          }
        }

        // Live validation warning banner if exceeded
        if (isExceeded) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = QcErrorRed,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Rule violation: Defects ($totalDefects) exceed Quantity (${draft.quantity})! Adjust counts.",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = QcErrorRed
            )
          }
        }
      }
    }

    // Scrollable List of Problems
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(problems, key = { it.id }) { problem ->
        val count = draft.defectCounts[problem.id] ?: 0
        ProblemRowCard(
          problem = problem,
          count = count,
          onCountChange = { newCount ->
            draftViewModel.updateProblemCount(problem.id, newCount)
          }
        )
      }
    }

    // Error message banner if submit failed
    if (errorMessage != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(QcErrorBg)
          .padding(8.dp)
      ) {
        Text(
          text = errorMessage ?: "",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // BOTTOM ACTION BAR: SEND (LEFT) and BACK (RIGHT)
    // Per explicit brief: "Send (bottom left) and Back (bottom right) returns to S4 with data kept."
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(IndustrialSurface)
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // BACK BUTTON ON BOTTOM LEFT (Keeps entered counts)
        BigButton(
          text = "BACK",
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          enabled = !isSubmitting,
          onClick = onNavigateBack,
          style = BigButtonStyle.SECONDARY_OUTLINE,
          modifier = Modifier.weight(0.9f),
          testTag = "problems_back_button"
        )

        // SEND BUTTON ON BOTTOM RIGHT (Enabled only when valid, i.e. totalDefects <= quantity)
        BigButton(
          text = if (totalDefects == 0) "SEND (ALL GOOD)" else "SEND ($totalDefects DEFECTS)",
          icon = Icons.AutoMirrored.Filled.Send,
          enabled = draft.isValidForSend,
          isLoading = isSubmitting,
          onClick = {
            if (totalDefects == 0) {
              showAllGoodConfirmDialog = true
            } else {
              performSend()
            }
          },
          style = if (totalDefects == 0) BigButtonStyle.SUCCESS else BigButtonStyle.PRIMARY_AMBER,
          modifier = Modifier.weight(1.3f),
          testTag = "problems_send_button"
        )
      }
    }
  }

  // All-Good Confirmation Dialog
  if (showAllGoodConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showAllGoodConfirmDialog = false },
      icon = {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = QcSuccessGreen, modifier = Modifier.size(36.dp))
      },
      title = {
        Text("No Defects Reported", fontWeight = FontWeight.Bold, color = TextHighContrast)
      },
      text = {
        Text(
          "All ${draft.quantity} part(s) are recorded as 100% conforming (0 defects).\n\nConfirm inspection submission for ${draft.partSerial}?",
          fontSize = 14.sp,
          color = TextMediumContrast
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showAllGoodConfirmDialog = false
            performSend()
          },
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = QcSuccessGreen)
        ) {
          Text("Confirm & Send", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAllGoodConfirmDialog = false }) {
          Text("Review Again", color = TextHighContrast)
        }
      }
    )
  }
}

@Composable
private fun ProblemRowCard(
  problem: Problem,
  count: Int,
  onCountChange: (Int) -> Unit
) {
  val hasDefects = count > 0

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (hasDefects) Color(0xFF261D15) else IndustrialCard
    ),
    border = BorderStroke(
      if (hasDefects) 2.dp else 1.dp,
      if (hasDefects) RenaultAmber else IndustrialCardBorder
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = problem.code,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = if (hasDefects) RenaultAmber else TextMuted
          )
          if (hasDefects) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(RenaultAmber, RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "$count DEFECTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = TextDarkOnLight
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = problem.label,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextHighContrast
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Compact Stepper for each problem
      NumberStepperField(
        value = count,
        onValueChange = onCountChange,
        min = 0,
        max = 999,
        modifier = Modifier.width(170.dp),
        testTag = "stepper_${problem.code}"
      )
    }
  }
}
