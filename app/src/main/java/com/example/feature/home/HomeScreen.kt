package com.example.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.outbox.OutboxSyncManager
import com.example.core.session.SessionManager
import com.example.ui.components.ConnectivityBanner
import com.example.ui.components.HeaderBar
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted

@Composable
fun HomeScreen(
  sessionManager: SessionManager,
  outboxSyncManager: OutboxSyncManager,
  isOnline: Boolean,
  onNavigateToMoList: () -> Unit,
  onNavigateToOutbox: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onLogout: () -> Unit
) {
  val currentUser by sessionManager.currentUser.collectAsState()
  val pendingCount by outboxSyncManager.pendingCount.collectAsState(initial = 0)
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
  ) {
    // Top Bar with operator info and outbox badge
    HeaderBar(
      user = currentUser,
      isOnline = isOnline,
      pendingCount = pendingCount,
      onOutboxClick = onNavigateToOutbox,
      onSettingsClick = onNavigateToSettings,
      onLogoutClick = {
        if (pendingCount > 0) {
          showLogoutConfirmDialog = true
        } else {
          sessionManager.clearSession()
          onLogout()
        }
      }
    )

    // Online / Offline Connectivity Banner
    ConnectivityBanner(
      isOnline = isOnline,
      pendingCount = pendingCount,
      onOutboxClick = onNavigateToOutbox
    )

    // Main Content: 4 Large Industrial Buttons
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "QUALITY CONTROL OPERATIONS",
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = RenaultAmber,
        modifier = Modifier.padding(bottom = 4.dp)
      )

      // Button 1: MO (Active)
      HomeModuleCard(
        title = "MISSION ORDER (MO)",
        subtitle = "Vehicle Part Inspection & Defect Counting",
        icon = Icons.Default.Assignment,
        isActive = true,
        onClick = onNavigateToMoList,
        testTag = "home_mo_button",
        modifier = Modifier.weight(1f)
      )

      // Button 2: Rework / Retouche (Coming Soon)
      HomeModuleCard(
        title = "REWORK / RETOUCHE",
        subtitle = "Part Rectification & Post-Fix Inspection",
        icon = Icons.Default.Build,
        isActive = false,
        onClick = {},
        testTag = "home_rework_button",
        modifier = Modifier.weight(1f)
      )

      // Button 3: Sorting / Tri (Coming Soon)
      HomeModuleCard(
        title = "SORTING / TRI 100%",
        subtitle = "Batch Segregation & Containment Action",
        icon = Icons.Default.FilterList,
        isActive = false,
        onClick = {},
        testTag = "home_sorting_button",
        modifier = Modifier.weight(1f)
      )

      // Button 4: Inventory / Stock (Coming Soon)
      HomeModuleCard(
        title = "INVENTORY / STOCK",
        subtitle = "Buffer Warehouse & Line-Side Counts",
        icon = Icons.Default.Inventory,
        isActive = false,
        onClick = {},
        testTag = "home_inventory_button",
        modifier = Modifier.weight(1f)
      )
    }
  }

  // Logout Guard Dialog when items are pending
  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = QcWarningAmber, modifier = Modifier.size(36.dp))
      },
      title = {
        Text("Pending Submissions in Outbox", fontWeight = FontWeight.Bold, color = TextHighContrast)
      },
      text = {
        Text(
          "You have $pendingCount inspection(s) queued locally that have not yet uploaded to the Convex server.\n\nLogging out will pause syncing until you log in again with User #${currentUser?.userNumber}.",
          fontSize = 14.sp,
          color = TextMediumContrast
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirmDialog = false
            sessionManager.clearSession()
            onLogout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = QcErrorRed)
        ) {
          Text("Log Out Anyway", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirmDialog = false }) {
          Text("Cancel", color = TextHighContrast)
        }
      }
    )
  }
}

@Composable
private fun HomeModuleCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isActive: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  val containerColor = if (isActive) Color(0xFF1E2D44) else Color(0xFF111827)
  val borderColor = if (isActive) RenaultAmber else Color(0xFF1F2937)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isActive) 2.dp else 1.dp, borderColor),
    modifier = modifier
      .fillMaxWidth()
      .clickable(enabled = isActive) { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .background(
            if (isActive) RenaultAmber.copy(alpha = 0.18f) else Color(0xFF1F2937),
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isActive) RenaultAmber else Color(0xFF4B5563),
          modifier = Modifier.size(30.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) TextHighContrast else Color(0xFF6B7280)
          )
          if (!isActive) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .background(Color(0xFF374151), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "COMING SOON",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9CA3AF)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = if (isActive) TextMediumContrast else Color(0xFF4B5563)
        )
      }
    }
  }
}
