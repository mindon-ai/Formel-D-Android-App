package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMuted

@Composable
fun NumberStepperField(
  value: Int,
  onValueChange: (Int) -> Unit,
  label: String? = null,
  min: Int = 0,
  max: Int = 9999,
  modifier: Modifier = Modifier,
  testTag: String = "number_stepper"
) {
  var isFocused by remember { mutableStateOf(false) }
  var textInput by remember { mutableStateOf(value.toString()) }
  val focusManager = LocalFocusManager.current

  // Keep text input synchronized when value changes externally (e.g. from buttons or reset)
  LaunchedEffect(value) {
    if (!isFocused) {
      textInput = value.toString()
    }
  }

  Column(modifier = modifier) {
    if (label != null) {
      Text(
        text = label,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextHighContrast,
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      // Big Decrement Button (glove friendly >= 56dp)
      Button(
        onClick = {
          val next = (value - 1).coerceAtLeast(min)
          onValueChange(next)
          textInput = next.toString()
        },
        enabled = value > min,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF334155),
          contentColor = TextHighContrast,
          disabledContainerColor = Color(0xFF1E293B),
          disabledContentColor = TextMuted
        ),
        modifier = Modifier
          .size(58.dp)
          .testTag("${testTag}_decrement")
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = "Decrease",
          modifier = Modifier.size(28.dp)
        )
      }

      // Editable Value Display Box (allows manual typing and shows keyboard)
      Box(
        modifier = Modifier
          .weight(1f)
          .height(58.dp)
          .background(if (isFocused) Color(0xFF1E2A3C) else IndustrialCard, RoundedCornerShape(10.dp))
          .border(
            BorderStroke(
              if (isFocused) 2.dp else 1.5.dp,
              if (isFocused) RenaultAmber else IndustrialCardBorder
            ),
            RoundedCornerShape(10.dp)
          )
          .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        BasicTextField(
          value = textInput,
          onValueChange = { newText ->
            val digitsOnly = newText.filter { it.isDigit() }.take(5)
            textInput = digitsOnly
            val parsed = digitsOnly.toIntOrNull()
            if (parsed != null) {
              val clamped = parsed.coerceIn(min, max)
              onValueChange(clamped)
            } else if (digitsOnly.isEmpty()) {
              // If user backspaces completely, set to min (e.g. 0 or 1)
              onValueChange(min)
            }
          },
          singleLine = true,
          textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if ((textInput.toIntOrNull() ?: 0) > 0) RenaultAmber else TextHighContrast,
            textAlign = TextAlign.Center
          ),
          cursorBrush = SolidColor(RenaultAmber),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              val parsed = textInput.toIntOrNull() ?: min
              val clamped = parsed.coerceIn(min, max)
              textInput = clamped.toString()
              onValueChange(clamped)
              focusManager.clearFocus()
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
              isFocused = focusState.isFocused
              if (!focusState.isFocused) {
                // Commit on blur
                val parsed = textInput.toIntOrNull() ?: min
                val clamped = parsed.coerceIn(min, max)
                textInput = clamped.toString()
                onValueChange(clamped)
              }
            }
            .testTag("${testTag}_input")
        )
      }

      // Big Increment Button (glove friendly >= 56dp)
      Button(
        onClick = {
          val next = (value + 1).coerceAtMost(max)
          onValueChange(next)
          textInput = next.toString()
        },
        enabled = value < max,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = RenaultAmber,
          contentColor = TextDarkOnLight,
          disabledContainerColor = Color(0xFF1E293B),
          disabledContentColor = TextMuted
        ),
        modifier = Modifier
          .size(58.dp)
          .testTag("${testTag}_increment")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Increase",
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}
