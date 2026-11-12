package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.User
import com.example.ui.theme.FormelDBlue
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcSuccessGreen
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMuted

@Composable
fun HeaderBar(
  user: User?,
  isOnline: Boolean,
  pendingCount: Int,
  onOutboxClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onLogoutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(IndustrialSurface)
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      // User info and connectivity pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(Color(0xFF2563EB), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = user?.userNumber ?: "OP",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = user?.displayName ?: "Operator",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextHighContrast
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Status dot
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(if (isOnline) QcSuccessGreen else QcWarningAmber, CircleShape)
            )
          }
          Text(
            text = "${user?.role ?: "Quality Control"} • TC51",
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }

      // Actions: Outbox, Settings, Logout
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Outbox Badge Button
        IconButton(
          onClick = onOutboxClick,
          modifier = Modifier
            .size(44.dp)
            .testTag("outbox_button")
        ) {
          BadgedBox(
            badge = {
              if (pendingCount > 0) {
                Badge(
                  containerColor = QcWarningAmber,
                  contentColor = TextDarkOnLight
                ) {
                  Text(text = pendingCount.toString(), fontWeight = FontWeight.Bold)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.Inbox,
              contentDescription = "Outbox",
              tint = if (pendingCount > 0) QcWarningAmber else TextHighContrast,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Settings
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier
            .size(44.dp)
            .testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        // Logout
        IconButton(
          onClick = onLogoutClick,
          modifier = Modifier
            .size(44.dp)
            .testTag("logout_button")
        ) {
          Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Log Out",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}
