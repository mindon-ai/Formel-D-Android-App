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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcSuccessGreen
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted

@Composable
fun ResultScreen(
  status: String,
  draftViewModel: InspectionDraftViewModel,
  onNewEntrySameMo: () -> Unit,
  onReturnToMoList: () -> Unit
) {
  val draft by draftViewModel.draft.collectAsState()
  val isOnlineSent = status == "SENT"

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Status Badge & Icon
      Box(
        modifier = Modifier
          .size(88.dp)
          .background(
            if (isOnlineSent) Color(0xFF064E3B) else Color(0xFF78350F),
            CircleShape
          )
          .border(
            3.dp,
            if (isOnlineSent) QcSuccessGreen else QcWarningAmber,
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isOnlineSent) Icons.Default.CheckCircle else Icons.Default.CloudQueue,
          contentDescription = null,
          tint = if (isOnlineSent) QcSuccessGreen else QcWarningAmber,
          modifier = Modifier.size(54.dp)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = if (isOnlineSent) "INSPECTION SENT" else "SAVED ON DEVICE",
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = if (isOnlineSent) QcSuccessGreen else QcWarningAmber,
        letterSpacing = 1.sp
      )

      Text(
        text = if (isOnlineSent)
          "Successfully transmitted to Convex backend"
        else
          "Device offline. Queued in Outbox, will send when online.",
        fontSize = 13.sp,
        color = TextMediumContrast,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Inspection Summary Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IndustrialCard),
        border = BorderStroke(1.5.dp, IndustrialCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "SUBMISSION RECEIPT",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RenaultAmber,
            letterSpacing = 1.sp
          )

          SummaryRow("Mission Order", draft.moNumber)
          SummaryRow("Part Reference", draft.partReference)
          SummaryRow("Part Serial", draft.partSerial)
          SummaryRow("Box ID", draft.boxId)

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Total Qty", fontSize = 11.sp, color = TextMuted)
              Text("${draft.quantity}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHighContrast)
            }
            Column {
              Text("Defects", fontSize = 11.sp, color = if (draft.totalDefects > 0) RenaultAmber else TextMuted)
              Text("${draft.totalDefects}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (draft.totalDefects > 0) RenaultAmber else TextHighContrast)
            }
            Column {
              Text("Good Conforming", fontSize = 11.sp, color = QcSuccessGreen)
              Text("${draft.goodQuantity}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = QcSuccessGreen)
            }
          }
        }
      }
    }

    // Actions at bottom
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Button 1: New entry (same MO) - primary recommended workflow
      BigButton(
        text = "NEW ENTRY (SAME MO)",
        icon = Icons.Default.PostAdd,
        onClick = {
          draftViewModel.resetForSameMo()
          onNewEntrySameMo()
        },
        style = BigButtonStyle.PRIMARY_AMBER,
        modifier = Modifier.fillMaxWidth(),
        testTag = "result_new_entry_button"
      )

      // Button 2: MO list
      BigButton(
        text = "RETURN TO MO LIST",
        icon = Icons.Default.Assignment,
        onClick = {
          draftViewModel.clear()
          onReturnToMoList()
        },
        style = BigButtonStyle.SECONDARY_OUTLINE,
        modifier = Modifier.fillMaxWidth(),
        testTag = "result_mo_list_button"
      )
    }
  }
}

@Composable
private fun SummaryRow(label: String, value: String) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = TextMuted
    )
    Text(
      text = value,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = TextHighContrast
    )
  }
}
