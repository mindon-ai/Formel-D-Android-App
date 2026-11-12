package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.core.db.AppDatabase
import com.example.core.molist.MoRepository
import com.example.core.network.ConvexDataSource
import com.example.core.network.NetworkMonitor
import com.example.core.outbox.OutboxSyncManager
import com.example.core.scanner.ScannerManager
import com.example.core.session.SessionManager
import com.example.ui.AppNavHost
import com.example.ui.theme.IndustrialNavyDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private lateinit var database: AppDatabase
  private lateinit var sessionManager: SessionManager
  private lateinit var networkMonitor: NetworkMonitor
  private lateinit var convexDataSource: ConvexDataSource
  private lateinit var outboxSyncManager: OutboxSyncManager
  private lateinit var scannerManager: ScannerManager
  private lateinit var moRepository: MoRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize dependencies
    database = AppDatabase.getDatabase(this)
    sessionManager = SessionManager(this)
    networkMonitor = NetworkMonitor(this, lifecycleScope)
    convexDataSource = ConvexDataSource(this)
    outboxSyncManager = OutboxSyncManager(
      database = database,
      networkMonitor = networkMonitor,
      convexDataSource = convexDataSource,
      sessionManager = sessionManager,
      scope = lifecycleScope
    )
    scannerManager = ScannerManager(this, lifecycleScope)
    moRepository = MoRepository(database, convexDataSource, networkMonitor, lifecycleScope)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
          color = IndustrialNavyDark
        ) {
          AppNavHost(
            sessionManager = sessionManager,
            networkMonitor = networkMonitor,
            convexDataSource = convexDataSource,
            outboxSyncManager = outboxSyncManager,
            scannerManager = scannerManager,
            moRepository = moRepository
          )
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    scannerManager.registerDataWedgeReceiver()
  }

  override fun onPause() {
    super.onPause()
    scannerManager.unregisterReceiver()
  }
}
