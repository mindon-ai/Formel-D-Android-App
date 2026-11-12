package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.QcSuccessGreen
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast

@Composable
fun ConnectivityBanner(
  isOnline: Boolean,
  pendingCount: Int,
  onOutboxClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isOnline) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .background(Color(0xFF78350F))
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.CloudOff,
          contentDescription = "Offline",
          tint = QcWarningAmber,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "OFFLINE MODE • Inspections queue safely in Outbox",
          color = Color(0xFFFEF3C7),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        if (pendingCount > 0) {
          Box(
            modifier = Modifier
              .background(QcWarningAmber, RoundedCornerShape(12.dp))
              .clickable { onOutboxClick() }
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "$pendingCount queued",
              color = TextDarkOnLight,
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }
      }
    }
  } else if (pendingCount > 0) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .background(Color(0xFF1E3A8A))
        .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.Sync,
          contentDescription = "Syncing",
          tint = Color(0xFF93C5FD),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Syncing outbox to Convex ($pendingCount remaining)...",
          color = Color(0xFFEFF6FF),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .background(Color(0xFF3B82F6), RoundedCornerShape(12.dp))
            .clickable { onOutboxClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "View",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
