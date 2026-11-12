package com.example.feature.inspection

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.MissionOrder
import com.example.core.scanner.ScannerManager
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.components.NumberStepperField
import com.example.ui.components.ScanTextField
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted

@Composable
fun MoInputScreen(
  draftViewModel: InspectionDraftViewModel,
  scannerManager: ScannerManager,
  onNavigateToProblems: () -> Unit,
  onNavigateBack: () -> Unit
) {
  val draft by draftViewModel.draft.collectAsState()
  val selectedMo by draftViewModel.selectedMo.collectAsState()

  val focusedField by scannerManager.focusedField.collectAsState()
  val lastScannedField by scannerManager.lastScannedField.collectAsState()

  var showDiscardDialog by remember { mutableStateOf(false) }

  // Set default focus to partSerial on first enter
  LaunchedEffect(Unit) {
    if (focusedField == null) {
      scannerManager.setFocusedField("partSerial")
    }
  }

  // Listen to incoming barcode scans from DataWedge or simulation injector
  LaunchedEffect(Unit) {
    scannerManager.scanEvents.collect { scanEvent ->
      val scannedData = scanEvent.data
      when (scannerManager.focusedField.value) {
        "partSerial" -> {
          draftViewModel.updatePartSerial(scannedData)
          if (scannerManager.config.value.autoAdvance) {
            scannerManager.setFocusedField("boxId")
          }
        }
        "boxId" -> {
          draftViewModel.updateBoxId(scannedData)
          if (scannerManager.config.value.autoAdvance) {
            scannerManager.setFocusedField("quantity")
          }
        }
        "quantity" -> {
          val num = scannedData.toIntOrNull()
          if (num != null && num > 0) {
            draftViewModel.updateQuantity(num)
          }
        }
        else -> {
          // If no field focused, default to partSerial
          draftViewModel.updatePartSerial(scannedData)
          if (scannerManager.config.value.autoAdvance) {
            scannerManager.setFocusedField("boxId")
          }
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
  ) {
    // Top Sticky Header with MO details
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(IndustrialSurface)
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = selectedMo?.moNumber ?: draft.moNumber,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = RenaultAmber
          )
          Box(
            modifier = Modifier
              .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
              .border(1.dp, RenaultAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "INSPECTION INPUT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RenaultAmber
            )
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = selectedMo?.title ?: draft.moTitle,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = TextHighContrast
        )
        Text(
          text = "Part Ref: ${selectedMo?.partReference ?: draft.partReference}",
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          color = TextMuted
        )
      }
    }

    // Scrollable Form Fields
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Part Serial Field
      ScanTextField(
        value = draft.partSerial,
        onValueChange = { draftViewModel.updatePartSerial(it) },
        label = "1. PART SERIAL NUMBER *",
        placeholder = "Scan or type part serial",
        isFocused = focusedField == "partSerial",
        isRecentlyScanned = lastScannedField == "partSerial",
        onFocusChange = { if (it) scannerManager.setFocusedField("partSerial") },
        onDone = { scannerManager.setFocusedField("boxId") },
        testTag = "input_part_serial"
      )

      // 2. Box ID Field
      ScanTextField(
        value = draft.boxId,
        onValueChange = { draftViewModel.updateBoxId(it) },
        label = "2. BOX CONTAINER ID *",
        placeholder = "Scan or type container/box ID",
        isFocused = focusedField == "boxId",
        isRecentlyScanned = lastScannedField == "boxId",
        onFocusChange = { if (it) scannerManager.setFocusedField("boxId") },
        onDone = { scannerManager.setFocusedField("quantity") },
        testTag = "input_box_id"
      )

      // 3. Quantity Field (Stepper + manual input)
      NumberStepperField(
        value = draft.quantity,
        onValueChange = { draftViewModel.updateQuantity(it) },
        label = "3. QUANTITY TO INSPECT *",
        min = 1,
        max = 1000,
        testTag = "input_quantity"
      )
    }

    // BOTTOM ACTION BAR: BACK (LEFT) and START (RIGHT)
    // Per the specification: "Back is bottom right and Start is bottom left."
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
        // BACK BUTTON ON BOTTOM LEFT
        BigButton(
          text = "BACK",
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          onClick = {
            if (draft.partSerial.isNotBlank() || draft.boxId.isNotBlank()) {
              showDiscardDialog = true
            } else {
              draftViewModel.clear()
              onNavigateBack()
            }
          },
          style = BigButtonStyle.SECONDARY_OUTLINE,
          modifier = Modifier.weight(1f),
          testTag = "mo_input_back_button"
        )

        // START BUTTON ON BOTTOM RIGHT (Enabled only when valid)
        BigButton(
          text = "START",
          icon = Icons.AutoMirrored.Filled.ArrowForward,
          enabled = draft.isValidForStart,
          onClick = onNavigateToProblems,
          style = BigButtonStyle.PRIMARY_AMBER,
          modifier = Modifier.weight(1f),
          testTag = "mo_input_start_button"
        )
      }
    }
  }

  // Back confirmation dialog
  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = RenaultAmber)
      },
      title = {
        Text("Discard Entered Data?", fontWeight = FontWeight.Bold, color = TextHighContrast)
      },
      text = {
        Text(
          "Entered serial number, box ID, and quantity will be lost. Do you want to go back to the Mission Order list?",
          fontSize = 14.sp,
          color = TextMediumContrast
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDiscardDialog = false
            draftViewModel.clear()
            onNavigateBack()
          },
          colors = ButtonDefaults.buttonColors(containerColor = QcErrorRed)
        ) {
          Text("Discard & Go Back", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDiscardDialog = false }) {
          Text("Cancel", color = TextHighContrast)
        }
      }
    )
  }
}
