package com.example.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.core.model.ScannerMode
import com.example.core.network.NetworkMonitor
import com.example.core.scanner.ScannerManager
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted

@Composable
fun SettingsDialog(
  scannerManager: ScannerManager,
  networkMonitor: NetworkMonitor,
  convexDataSource: com.example.core.network.ConvexDataSource,
  onDismiss: () -> Unit
) {
  val currentConfig by scannerManager.config.collectAsState()
  val isSimulatedOffline = networkMonitor.isSimulatedOffline()

  var convexUrl by remember { mutableStateOf(convexDataSource.backendUrl) }
  var intentAction by remember { mutableStateOf(currentConfig.intentAction) }
  var extraKey by remember { mutableStateOf(currentConfig.extraKey) }
  var autoAdvance by remember { mutableStateOf(currentConfig.autoAdvance) }
  var hapticFeedback by remember { mutableStateOf(currentConfig.hapticFeedback) }
  var audioBeep by remember { mutableStateOf(currentConfig.audioBeep) }
  var testBarcode by remember { mutableStateOf("RN-TEST-9901") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("settings_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp)
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = null,
              tint = RenaultAmber,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Device & Scanner Settings",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = TextHighContrast
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 0: Convex Backend Configuration
        Text(
          text = "CONVEX BACKEND CONFIGURATION",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = RenaultAmber,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = convexUrl,
          onValueChange = { convexUrl = it },
          label = { Text("Convex Deployment URL") },
          placeholder = { Text("https://<your-project>.convex.cloud") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IndustrialCard,
            unfocusedContainerColor = IndustrialCard,
            focusedBorderColor = RenaultAmber,
            unfocusedBorderColor = IndustrialCardBorder,
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast
          ),
          modifier = Modifier.fillMaxWidth()
        )
        Text(
          text = "Paste your Convex Deployment URL from 'npx convex dev' or dashboard.",
          fontSize = 11.sp,
          color = TextMuted,
          modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // SECTION 1: Zebra DataWedge Configuration
        Text(
          text = "ZEBRA DATAWEDGE SCANNER",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = RenaultAmber,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = intentAction,
          onValueChange = { intentAction = it },
          label = { Text("Broadcast Intent Action") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IndustrialCard,
            unfocusedContainerColor = IndustrialCard,
            focusedBorderColor = RenaultAmber,
            unfocusedBorderColor = IndustrialCardBorder,
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = extraKey,
          onValueChange = { extraKey = it },
          label = { Text("Intent Extra Key") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IndustrialCard,
            unfocusedContainerColor = IndustrialCard,
            focusedBorderColor = RenaultAmber,
            unfocusedBorderColor = IndustrialCardBorder,
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggles
        SettingSwitchRow(
          title = "Auto-advance Focus on Scan",
          subtitle = "Automatically advance Part Serial -> Box ID -> Quantity",
          checked = autoAdvance,
          onCheckedChange = { autoAdvance = it }
        )

        SettingSwitchRow(
          title = "Vibration Feedback",
          subtitle = "Haptic buzz upon successful barcode decode",
          checked = hapticFeedback,
          onCheckedChange = { hapticFeedback = it }
        )

        SettingSwitchRow(
          title = "Audio Beep Tone",
          subtitle = "Audible high-pitch chirp on accepted scan",
          checked = audioBeep,
          onCheckedChange = { audioBeep = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 2: Connectivity & Offline Simulation
        Text(
          text = "CONNECTIVITY SIMULATION",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = RenaultAmber,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingSwitchRow(
          title = "Force Offline Mode",
          subtitle = "Simulates factory warehouse dead-zone to test Outbox queue",
          checked = isSimulatedOffline,
          onCheckedChange = { networkMonitor.setSimulatedOffline(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 3: Test Scanner Trigger
        Text(
          text = "SCAN TEST INJECTOR",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = RenaultAmber,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = testBarcode,
            onValueChange = { testBarcode = it },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          BigButton(
            text = "TEST",
            minHeight = 52.dp,
            onClick = {
              scannerManager.simulateScan(testBarcode)
            },
            modifier = Modifier.width(90.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device info
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(text = "Target Device: Zebra TC51 / TC56 Rugged Handheld", fontSize = 11.sp, color = TextMuted)
            Text(text = "Engine: SE4710 2D Imager • Screen: 5\" 720p (360x640 dp)", fontSize = 11.sp, color = TextMuted)
            Text(text = "Build: Formel D QC v1.0.0 (Renault Manufacturing)", fontSize = 11.sp, color = TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save & Apply
        BigButton(
          text = "SAVE & CLOSE",
          onClick = {
            convexDataSource.backendUrl = convexUrl.trim()
            scannerManager.updateConfig(
              currentConfig.copy(
                intentAction = intentAction.trim(),
                extraKey = extraKey.trim(),
                autoAdvance = autoAdvance,
                hapticFeedback = hapticFeedback,
                audioBeep = audioBeep
              )
            )
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
private fun SettingSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHighContrast)
      Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = RenaultAmber,
        checkedTrackColor = Color(0xFF78350F)
      )
    )
  }
}
