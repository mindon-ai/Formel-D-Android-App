package com.example.feature.molist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.core.molist.MoRepository
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcWarningAmber
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun MoListScreen(
  moRepository: MoRepository,
  isOnline: Boolean,
  onMoSelected: (MissionOrder) -> Unit,
  onBackClick: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var debouncedQuery by remember { mutableStateOf("") }
  var mos by remember { mutableStateOf<List<MissionOrder>>(emptyList()) }
  var isCached by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var refreshTrigger by remember { mutableStateOf(0) }

  // 200ms debounce for search query
  LaunchedEffect(searchQuery) {
    delay(200)
    debouncedQuery = searchQuery
  }

  LaunchedEffect(debouncedQuery, refreshTrigger, isOnline) {
    isLoading = true
    errorMessage = null
    moRepository.getActiveMosFlow(debouncedQuery).collect { result ->
      mos = result.items
      isCached = result.isCached
      errorMessage = result.error
      isLoading = false
    }
  }

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
          modifier = Modifier.testTag("mo_list_back_button")
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
            text = "Select Mission Order",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighContrast
          )
          Text(
            text = if (isOnline) "Live from Convex Server" else "Offline Cache Mode",
            fontSize = 11.sp,
            color = if (isOnline) RenaultAmber else QcWarningAmber
          )
        }

        IconButton(
          onClick = { refreshTrigger++ },
          modifier = Modifier.testTag("refresh_mos_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = TextHighContrast
          )
        }
      }
    }

    // Offline Banner if serving cached data
    if (!isOnline || isCached) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF78350F))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = QcWarningAmber,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Offline Mode • Showing cached Mission Orders",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFEF3C7)
          )
        }
      }
    }

    // Fixed Search Field at Top
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(IndustrialSurface)
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Filter by MO number, title, part ref...", color = TextMuted) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = RenaultAmber)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
            }
          }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = IndustrialCard,
          unfocusedContainerColor = IndustrialCard,
          focusedBorderColor = RenaultAmber,
          unfocusedBorderColor = IndustrialCardBorder,
          focusedTextColor = TextHighContrast,
          unfocusedTextColor = TextHighContrast
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("mo_search_input")
      )
    }

    // Content: List of Active MOs
    Box(modifier = Modifier.fillMaxSize()) {
      if (isLoading && mos.isEmpty()) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxSize()
        ) {
          CircularProgressIndicator(color = RenaultAmber)
        }
      } else if (mos.isEmpty()) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (searchQuery.isNotBlank()) "No MO matches '$searchQuery'" else "No active Mission Orders",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighContrast
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Contact your shift supervisor to assign an active inspection order.",
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
          items(mos, key = { it.id }) { mo ->
            MoItemCard(
              mo = mo,
              onClick = { onMoSelected(mo) },
              modifier = Modifier.testTag("mo_card_${mo.moNumber}")
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MoItemCard(
  mo: MissionOrder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = IndustrialCard),
    border = BorderStroke(1.5.dp, IndustrialCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = mo.moNumber,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = RenaultAmber
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = mo.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextHighContrast
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Ref: ${mo.partReference}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMediumContrast
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "• ${mo.problems.size} defect types",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Select MO",
        tint = RenaultAmber,
        modifier = Modifier.size(28.dp)
      )
    }
  }
}
