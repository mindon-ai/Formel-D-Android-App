package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMuted

enum class BigButtonStyle {
  PRIMARY_AMBER,
  PRIMARY_BLUE,
  SECONDARY_OUTLINE,
  DANGER,
  SUCCESS
}

@Composable
fun BigButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  style: BigButtonStyle = BigButtonStyle.PRIMARY_AMBER,
  icon: ImageVector? = null,
  minHeight: Dp = 72.dp,
  testTag: String = "big_button"
) {
  val containerColor = when (style) {
    BigButtonStyle.PRIMARY_AMBER -> RenaultAmber
    BigButtonStyle.PRIMARY_BLUE -> Color(0xFF2563EB)
    BigButtonStyle.SECONDARY_OUTLINE -> IndustrialCard
    BigButtonStyle.DANGER -> Color(0xFFDC2626)
    BigButtonStyle.SUCCESS -> Color(0xFF10B981)
  }

  val contentColor = when (style) {
    BigButtonStyle.PRIMARY_AMBER -> TextDarkOnLight
    BigButtonStyle.SECONDARY_OUTLINE -> TextHighContrast
    else -> Color.White
  }

  Button(
    onClick = onClick,
    enabled = enabled && !isLoading,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor,
      disabledContainerColor = Color(0xFF1E293B),
      disabledContentColor = TextMuted
    ),
    border = if (style == BigButtonStyle.SECONDARY_OUTLINE) BorderStroke(1.5.dp, IndustrialCardBorder) else null,
    modifier = modifier
      .height(minHeight)
      .testTag(testTag)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(28.dp),
          color = contentColor,
          strokeWidth = 3.dp
        )
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
  }
}
