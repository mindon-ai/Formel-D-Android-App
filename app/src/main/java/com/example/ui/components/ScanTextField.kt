package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.ScanFlashGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMuted

@Composable
fun ScanTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  isFocused: Boolean = false,
  isRecentlyScanned: Boolean = false,
  onFocusChange: (Boolean) -> Unit = {},
  onDone: (() -> Unit)? = null,
  imeAction: ImeAction = ImeAction.Next,
  testTag: String = "scan_text_field"
) {
  val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

  LaunchedEffect(isFocused) {
    if (isFocused) {
      try {
        focusRequester.requestFocus()
      } catch (_: Exception) {}
    }
  }

  val animatedBorderColor by animateColorAsState(
    targetValue = when {
      isRecentlyScanned -> ScanFlashGreen
      isFocused -> RenaultAmber
      else -> IndustrialCardBorder
    },
    animationSpec = tween(durationMillis = 300),
    label = "borderColor"
  )

  val animatedBgColor by animateColorAsState(
    targetValue = when {
      isRecentlyScanned -> Color(0xFF064E3B)
      isFocused -> Color(0xFF1E2A3C)
      else -> IndustrialCard
    },
    animationSpec = tween(durationMillis = 300),
    label = "bgColor"
  )

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Text(
        text = label,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = if (isFocused) RenaultAmber else TextHighContrast
      )
      Spacer(modifier = Modifier.weight(1f))
      if (isRecentlyScanned) {
        Text(
          text = "SCANNED",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = ScanFlashGreen
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(animatedBgColor, RoundedCornerShape(10.dp))
        .border(BorderStroke(if (isFocused || isRecentlyScanned) 2.5.dp else 1.5.dp, animatedBorderColor), RoundedCornerShape(10.dp))
        .padding(horizontal = 14.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.QrCodeScanner,
          contentDescription = "Scan Icon",
          tint = if (isFocused) RenaultAmber else TextMuted,
          modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f)) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              color = TextMuted,
              fontSize = 18.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
              color = TextHighContrast,
              fontSize = 20.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(RenaultAmber),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
              onNext = { onDone?.invoke() },
              onDone = { onDone?.invoke() }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .onFocusChanged { onFocusChange(it.isFocused) }
              .testTag(testTag)
          )
        }

        if (value.isNotEmpty()) {
          IconButton(
            onClick = { onValueChange("") },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
