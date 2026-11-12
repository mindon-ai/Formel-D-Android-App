package com.example.feature.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.ConvexDataSource
import com.example.core.session.SessionManager
import com.example.ui.components.BigButton
import com.example.ui.components.BigButtonStyle
import com.example.ui.theme.FormelDBlue
import com.example.ui.theme.IndustrialCard
import com.example.ui.theme.IndustrialCardBorder
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.QcErrorBg
import com.example.ui.theme.QcErrorRed
import com.example.ui.theme.RenaultAmber
import com.example.ui.theme.TextDarkOnLight
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  sessionManager: SessionManager,
  convexDataSource: ConvexDataSource,
  onLoginSuccess: () -> Unit
) {
  var userNumber by remember { mutableStateOf("025") }
  var password by remember { mutableStateOf("pass123") }
  var passwordVisible by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  var showChangePasswordDialog by remember { mutableStateOf(false) }
  var newPassword by remember { mutableStateOf("") }

  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(IndustrialNavyDark)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Logo / Branding Header
      Box(
        modifier = Modifier
          .size(76.dp)
          .background(IndustrialCard, RoundedCornerShape(18.dp))
          .border(2.dp, RenaultAmber, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "FD",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = RenaultAmber
          )
          Text(
            text = "QC",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Formel D Quality Control",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TextHighContrast,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Renault Manufacturing Plant • Zebra TC51",
        fontSize = 13.sp,
        color = TextMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Error Banner
      if (errorMessage != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(QcErrorBg, RoundedCornerShape(8.dp))
            .border(1.dp, QcErrorRed, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text(
            text = errorMessage ?: "",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // User Number Field
      OutlinedTextField(
        value = userNumber,
        onValueChange = {
          userNumber = it
          errorMessage = null
        },
        label = { Text("Operator Number (e.g. 025)") },
        leadingIcon = {
          Icon(Icons.Default.Badge, contentDescription = null, tint = RenaultAmber)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = IndustrialCard,
          unfocusedContainerColor = IndustrialCard,
          focusedBorderColor = RenaultAmber,
          unfocusedBorderColor = IndustrialCardBorder,
          focusedTextColor = TextHighContrast,
          unfocusedTextColor = TextHighContrast,
          focusedLabelColor = RenaultAmber,
          unfocusedLabelColor = TextMuted
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("user_number_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Password Field
      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          errorMessage = null
        },
        label = { Text("Password") },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = null, tint = RenaultAmber)
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle password",
              tint = TextMuted
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = IndustrialCard,
          unfocusedContainerColor = IndustrialCard,
          focusedBorderColor = RenaultAmber,
          unfocusedBorderColor = IndustrialCardBorder,
          focusedTextColor = TextHighContrast,
          unfocusedTextColor = TextHighContrast,
          focusedLabelColor = RenaultAmber,
          unfocusedLabelColor = TextMuted
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("password_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Demo quick fills
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .clickable {
              userNumber = "025"
              password = "pass123"
            }
            .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
          Column {
            Text(text = "Demo Op #025", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RenaultAmber)
            Text(text = "Jean Dupont", fontSize = 11.sp, color = TextMediumContrast)
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .clickable {
              userNumber = "233"
              password = "pass123"
            }
            .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
          Column {
            Text(text = "Demo Op #233", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RenaultAmber)
            Text(text = "Marie Curie", fontSize = 11.sp, color = TextMediumContrast)
          }
        }
      }

      Spacer(modifier = Modifier.height(26.dp))

      // Large Login Button (72dp)
      BigButton(
        text = "LOG IN",
        icon = Icons.Default.Login,
        isLoading = isLoading,
        onClick = {
          if (userNumber.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both User Number and Password"
            return@BigButton
          }

          isLoading = true
          errorMessage = null

          scope.launch {
            val result = convexDataSource.login(userNumber, password)
            isLoading = false
            if (result != null) {
              val (token, user) = result
              sessionManager.saveSession(token, user)
              if (user.mustChangePassword) {
                showChangePasswordDialog = true
              } else {
                onLoginSuccess()
              }
            } else {
              errorMessage = "Invalid credentials. Please verify your User Number and Password."
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
        testTag = "login_button"
      )
    }

    // Footer info
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 24.dp)
    ) {
      Text(
        text = "Formel D Quality Systems v1.0.0",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted
      )
      Text(
        text = "Internal Sideload • Convex Enterprise Sync",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )
    }
  }

  // Mandatory Change Password Dialog if flag set
  if (showChangePasswordDialog) {
    AlertDialog(
      onDismissRequest = {},
      title = {
        Text("Change Default Password", fontWeight = FontWeight.Bold, color = TextHighContrast)
      },
      text = {
        Column {
          Text(
            "An administrator has requested that you change your password before beginning your shift.",
            fontSize = 13.sp,
            color = TextMediumContrast
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPassword.isNotBlank()) {
              scope.launch {
                convexDataSource.changePassword(userNumber, newPassword)
                sessionManager.updatePasswordChanged()
                showChangePasswordDialog = false
                onLoginSuccess()
              }
            }
          },
          enabled = newPassword.length >= 4
        ) {
          Text("Update & Proceed")
        }
      }
    )
  }
}
