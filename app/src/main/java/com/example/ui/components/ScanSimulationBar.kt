package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMuted

@Composable
fun ScanSimulationBar(
  focusedField: String?,
  onInjectBarcode: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF0F172A))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        imageVector = Icons.Default.QrCodeScanner,
        contentDescription = "Zebra Laser",
        tint = RenaultAmber,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Zebra Imager / Test Injector (Target: ${focusedField ?: "None"})",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = RenaultAmber
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
    ) {
      val presets = listOf(
        "RN-82001-A904" to "Serial A",
        "RN-77002-C112" to "Serial B",
        "RN-94812-F550" to "Serial C",
        "BOX-4029-A" to "Box 4029",
        "BOX-8812-B" to "Box 8812",
        "BOX-1033-C" to "Box 1033"
      )

      presets.forEach { (code, label) ->
        Box(
          modifier = Modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
            .clickable { onInjectBarcode(code) }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("inject_$label")
        ) {
          Column {
            Text(
              text = label,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = RenaultAmber
            )
            Text(
              text = code,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = TextHighContrast
            )
          }
        }
      }
    }
  }
}
