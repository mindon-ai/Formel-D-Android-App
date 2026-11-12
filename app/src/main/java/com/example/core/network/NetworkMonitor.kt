package com.example.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NetworkMonitor(context: Context, private val scope: CoroutineScope) {
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

  private val _deviceConnected = MutableStateFlow(checkInitialConnectivity())
  private val _simulatedOffline = MutableStateFlow(false)

  val isOnline: StateFlow<Boolean>
  private val _effectiveOnline = MutableStateFlow(checkInitialConnectivity())

  init {
    isOnline = _effectiveOnline.asStateFlow()

    scope.launch {
      combine(_deviceConnected, _simulatedOffline) { connected, simulatedOffline ->
        connected && !simulatedOffline
      }.collect {
        _effectiveOnline.value = it
      }
    }

    try {
      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

      connectivityManager?.registerNetworkCallback(
        request,
        object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            _deviceConnected.value = true
          }

          override fun onLost(network: Network) {
            _deviceConnected.value = false
          }
        }
      )
    } catch (_: Exception) {
      _deviceConnected.value = true
    }
  }

  fun setSimulatedOffline(offline: Boolean) {
    _simulatedOffline.value = offline
  }

  fun isSimulatedOffline(): Boolean = _simulatedOffline.value

  private fun checkInitialConnectivity(): Boolean {
    val cm = connectivityManager ?: return true
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
