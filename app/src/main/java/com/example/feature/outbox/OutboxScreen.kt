package com.example.feature.outbox

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.core.model.InspectionSubmission
import com.example.core.model.SubmissionStatus
import com.example.core.outbox.OutboxSyncManager
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.QcSuccessGreen
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OutboxScreen(
  outboxSyncManager: OutboxSyncManager,
  isOnline: Boolean,
  onBackClick: () -> Unit
) {
  val submissions by outboxSyncManager.allSubmissions.collectAsState(initial = emptyList())
  val pendingCount by outboxSyncManager.pendingCount.collectAsState(initial = 0)
  var isRetrying by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<InspectionSubmission?>(null) }

  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
  ) {
    // Top Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(IndustrialSurface)
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("outbox_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextHighContrast
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Outbox & Offline Queue",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighContrast
          )
          Text(
            text = if (pendingCount > 0) "$pendingCount pending sync" else "All submissions synchronized",
            fontSize = 11.sp,
            color = if (pendingCount > 0) QcWarningAmber else QcSuccessGreen
          )
        }

        if (pendingCount > 0) {
          IconButton(
            onClick = {
              isRetrying = true
              scope.launch {
                outboxSyncManager.retryAll()
                isRetrying = false
              }
            },
            enabled = isOnline && !isRetrying,
            modifier = Modifier.testTag("outbox_retry_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Retry All",
              tint = if (isOnline) RenaultAmber else TextMuted
            )
          }
        }
      }
    }

    // List of Submissions
    Box(modifier = Modifier.weight(1f)) {
      if (submissions.isEmpty()) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Outbox is Empty",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighContrast
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Submissions completed during your shift will appear here.",
            fontSize = 13.sp,
            color = TextMuted
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(submissions, key = { it.id }) { item ->
            OutboxItemCard(
              item = item,
              onDelete = { itemToDelete = item }
            )
          }
        }
      }
    }

    // Bottom Action Bar: Retry All Pending Button
    if (pendingCount > 0) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(IndustrialSurface)
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        BigButton(
          text = if (isOnline) "SYNC ALL PENDING NOW ($pendingCount)" else "OFFLINE • SYNC WILL RUN AUTOMATICALLY",
          icon = Icons.Default.Sync,
          enabled = isOnline,
          isLoading = isRetrying,
          onClick = {
            isRetrying = true
            scope.launch {
              outboxSyncManager.retryAll()
              isRetrying = false
            }
          },
          style = BigButtonStyle.PRIMARY_AMBER,
          modifier = Modifier.fillMaxWidth(),
          testTag = "sync_all_button"
        )
      }
    }
  }

  // Delete / Discard confirmation dialog
  if (itemToDelete != null) {
    val target = itemToDelete!!
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      icon = {
        Icon(Icons.Default.Delete, contentDescription = null, tint = QcErrorRed)
      },
      title = {
        Text("Discard Submission?", fontWeight = FontWeight.Bold, color = TextHighContrast)
      },
      text = {
        Text(
          "Discard inspection for Part Serial ${target.partSerial} (${target.moNumber})?\n\nThis action cannot be undone.",
          fontSize = 14.sp,
          color = TextMediumContrast
        )
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              outboxSyncManager.deleteSubmission(target.id)
              itemToDelete = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = QcErrorRed)
        ) {
          Text("Discard", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel", color = TextHighContrast)
        }
      }
    )
  }
}

@Composable
private fun OutboxItemCard(
  item: InspectionSubmission,
  onDelete: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()) }
  val formattedTime = remember(item.createdAt) { dateFormat.format(Date(item.createdAt)) }

  val statusColor = when (item.status) {
    SubmissionStatus.SENT -> QcSuccessGreen
    SubmissionStatus.SENDING -> Color(0xFF60A5FA)
    SubmissionStatus.PENDING -> QcWarningAmber
    SubmissionStatus.REJECTED -> QcErrorRed
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = IndustrialCard),
    border = BorderStroke(1.5.dp, IndustrialCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.moNumber,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = RenaultAmber
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• Op #${item.userNumber}",
            fontSize = 12.sp,
            color = TextMuted
          )
        }

        // Status Badge
        Box(
          modifier = Modifier
            .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .border(1.dp, statusColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
          Text(
            text = item.status.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = statusColor
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Serial: ${item.partSerial}  |  Box: ${item.boxId}",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = TextHighContrast
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Qty: ${item.quantity} (Defects: ${item.totalDefects}, Good: ${item.goodQuantity})",
          fontSize = 12.sp,
          color = TextMediumContrast
        )
        Text(
          text = formattedTime,
          fontSize = 11.sp,
          color = TextMuted
        )
      }

      // Rejection / Error details if any
      if (item.lastError != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF450A0A), RoundedCornerShape(6.dp))
            .padding(8.dp)
        ) {
          Text(
            text = "Error: ${item.lastError}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFCA5A5)
          )
        }
      }

      // Delete action for rejected items or sent items
      if (item.status == SubmissionStatus.REJECTED || item.status == SubmissionStatus.SENT) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.fillMaxWidth()
        ) {
          TextButton(
            onClick = onDelete,
            modifier = Modifier.height(36.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Discard", fontSize = 12.sp, color = TextMuted)
          }
        }
      }
    }
  }
}
