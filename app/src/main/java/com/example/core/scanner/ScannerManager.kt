package com.example.core.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.core.model.ScannerConfig
import com.example.core.model.ScannerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanEvent(
  val data: String,
  val timestamp: Long = System.currentTimeMillis()
)

class ScannerManager(
  private val context: Context,
  private val scope: CoroutineScope
) {
  private val _config = MutableStateFlow(ScannerConfig())
  val config: StateFlow<ScannerConfig> = _config.asStateFlow()

  private val _scanEvents = MutableSharedFlow<ScanEvent>(extraBufferCapacity = 10)
  val scanEvents: SharedFlow<ScanEvent> = _scanEvents.asSharedFlow()

  // Track currently active/focused field ID (e.g. "partSerial", "boxId", "quantity")
  private val _focusedField = MutableStateFlow<String?>("partSerial")
  val focusedField: StateFlow<String?> = _focusedField.asStateFlow()

  // Flash highlight event for feedback
  private val _lastScannedField = MutableStateFlow<String?>(null)
  val lastScannedField: StateFlow<String?> = _lastScannedField.asStateFlow()

  private var receiver: BroadcastReceiver? = null
  private var toneGenerator: ToneGenerator? = null

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    } catch (_: Exception) {
      // Ignored if audio unavailable
    }
    registerDataWedgeReceiver()
  }

  fun setFocusedField(fieldId: String?) {
    _focusedField.value = fieldId
  }

  fun updateConfig(newConfig: ScannerConfig) {
    val oldAction = _config.value.intentAction
    _config.value = newConfig
    if (oldAction != newConfig.intentAction) {
      registerDataWedgeReceiver()
    }
  }

  fun registerDataWedgeReceiver() {
    unregisterReceiver()
    val filter = IntentFilter().apply {
      addAction(_config.value.intentAction)
      addAction("com.symbol.datawedge.api.RESULT_ACTION")
      addAction("com.dwexample.ACTION")
    }

    val newReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val extraKey = _config.value.extraKey
        val barcode = intent.getStringExtra(extraKey)
          ?: intent.getStringExtra("com.symbol.datawedge.data_string")
          ?: intent.getStringExtra("data")

        if (!barcode.isNullOrBlank()) {
          handleIncomingScan(barcode.trim())
        }
      }
    }

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(newReceiver, filter, Context.RECEIVER_EXPORTED)
      } else {
        context.registerReceiver(newReceiver, filter)
      }
      receiver = newReceiver
    } catch (_: Exception) {
      // Fallback
    }
  }

  fun unregisterReceiver() {
    receiver?.let {
      try {
        context.unregisterReceiver(it)
      } catch (_: Exception) {}
      receiver = null
    }
  }

  fun simulateScan(barcode: String) {
    handleIncomingScan(barcode.trim())
  }

  private fun handleIncomingScan(rawBarcode: String) {
    val cleanBarcode = rawBarcode.filter { it >= ' ' && it != '\u007F' }
    if (cleanBarcode.isBlank()) return

    scope.launch {
      triggerFeedback()
      val currentField = _focusedField.value
      _lastScannedField.value = currentField
      _scanEvents.emit(ScanEvent(cleanBarcode))
    }
  }

  private fun triggerFeedback() {
    if (_config.value.hapticFeedback) {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          vibratorManager?.defaultVibrator?.vibrate(
            VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
          )
        } else {
          @Suppress("DEPRECATION")
          val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(80)
          }
        }
      } catch (_: Exception) {}
    }

    if (_config.value.audioBeep) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
      } catch (_: Exception) {}
    }
  }
}
