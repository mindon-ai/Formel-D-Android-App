package com.example.core.session

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("formeld_qc_session", Context.MODE_PRIVATE)

  private val _currentUser = MutableStateFlow<User?>(loadUser())
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _token = MutableStateFlow<String?>(prefs.getString("token", null))
  val token: StateFlow<String?> = _token.asStateFlow()

  val isLoggedIn: Boolean
    get() = _token.value != null && _currentUser.value != null

  fun saveSession(token: String, user: User) {
    prefs.edit()
      .putString("token", token)
      .putString("user_id", user.id)
      .putString("user_number", user.userNumber)
      .putString("display_name", user.displayName)
      .putString("role", user.role)
      .putBoolean("must_change_pw", user.mustChangePassword)
      .apply()

    _token.value = token
    _currentUser.value = user
  }

  fun updatePasswordChanged() {
    val current = _currentUser.value ?: return
    val updated = current.copy(mustChangePassword = false)
    prefs.edit().putBoolean("must_change_pw", false).apply()
    _currentUser.value = updated
  }

  fun clearSession() {
    prefs.edit().clear().apply()
    _token.value = null
    _currentUser.value = null
  }

  private fun loadUser(): User? {
    val id = prefs.getString("user_id", null) ?: return null
    val number = prefs.getString("user_number", null) ?: return null
    val name = prefs.getString("display_name", null) ?: return null
    val role = prefs.getString("role", "Operator") ?: "Operator"
    val mustChange = prefs.getBoolean("must_change_pw", false)
    return User(id, number, name, role, mustChange)
  }
}
