package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.molist.MoRepository
import com.example.core.network.ConvexDataSource
import com.example.core.network.NetworkMonitor
import com.example.core.outbox.OutboxSyncManager
import com.example.core.scanner.ScannerManager
import com.example.core.session.SessionManager
import com.example.feature.auth.LoginScreen
import com.example.feature.home.HomeScreen
import com.example.feature.inspection.InspectionDraftViewModel
import com.example.feature.inspection.MoInputScreen
import com.example.feature.inspection.ProblemsScreen
import com.example.feature.inspection.ResultScreen
import com.example.feature.molist.MoListScreen
import com.example.feature.outbox.OutboxScreen
import com.example.feature.settings.SettingsDialog
import com.example.ui.theme.IndustrialNavyDark

object AppDestinations {
  const val LOGIN = "login"
  const val HOME = "home"
  const val MO_LIST = "mo_list"
  const val INSPECTION_INPUT = "inspection_input"
  const val INSPECTION_PROBLEMS = "inspection_problems"
  const val INSPECTION_RESULT = "inspection_result"
  const val OUTBOX = "outbox"
}

@Composable
fun AppNavHost(
  sessionManager: SessionManager,
  networkMonitor: NetworkMonitor,
  convexDataSource: ConvexDataSource,
  outboxSyncManager: OutboxSyncManager,
  scannerManager: ScannerManager,
  moRepository: MoRepository
) {
  val navController = rememberNavController()
  val isOnline by networkMonitor.isOnline.collectAsState()
  val currentUser by sessionManager.currentUser.collectAsState()

  // Shared draft viewModel across inspection flow
  val draftViewModel: InspectionDraftViewModel = viewModel()

  var showSettingsDialog by remember { mutableStateOf(false) }

  val startDestination = if (sessionManager.isLoggedIn) AppDestinations.HOME else AppDestinations.LOGIN

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = IndustrialNavyDark
  ) {
    NavHost(
      navController = navController,
      startDestination = startDestination
    ) {
      // S1. Login Screen
      composable(AppDestinations.LOGIN) {
        LoginScreen(
          sessionManager = sessionManager,
          convexDataSource = convexDataSource,
          onLoginSuccess = {
            navController.navigate(AppDestinations.HOME) {
              popUpTo(AppDestinations.LOGIN) { inclusive = true }
            }
          }
        )
      }

      // S2. Home Screen
      composable(AppDestinations.HOME) {
        HomeScreen(
          sessionManager = sessionManager,
          outboxSyncManager = outboxSyncManager,
          isOnline = isOnline,
          onNavigateToMoList = {
            navController.navigate(AppDestinations.MO_LIST)
          },
          onNavigateToOutbox = {
            navController.navigate(AppDestinations.OUTBOX)
          },
          onNavigateToSettings = {
            showSettingsDialog = true
          },
          onLogout = {
            navController.navigate(AppDestinations.LOGIN) {
              popUpTo(AppDestinations.HOME) { inclusive = true }
            }
          }
        )
      }

      // S3. MO List Screen
      composable(AppDestinations.MO_LIST) {
        BackHandler {
          navController.popBackStack()
        }
        MoListScreen(
          moRepository = moRepository,
          isOnline = isOnline,
          onMoSelected = { mo ->
            draftViewModel.initForMo(mo)
            navController.navigate(AppDestinations.INSPECTION_INPUT)
          },
          onBackClick = {
            navController.popBackStack()
          }
        )
      }

      // S4. MO Input Screen (Start bottom-left, Back bottom-right)
      composable(AppDestinations.INSPECTION_INPUT) {
        MoInputScreen(
          draftViewModel = draftViewModel,
          scannerManager = scannerManager,
          onNavigateToProblems = {
            navController.navigate(AppDestinations.INSPECTION_PROBLEMS)
          },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      // S5. Problems Screen (Send bottom-left, Back bottom-right)
      composable(AppDestinations.INSPECTION_PROBLEMS) {
        BackHandler {
          navController.popBackStack()
        }
        ProblemsScreen(
          draftViewModel = draftViewModel,
          outboxSyncManager = outboxSyncManager,
          onNavigateToResult = { status ->
            navController.navigate("${AppDestinations.INSPECTION_RESULT}/$status")
          },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      // S6. Result Screen
      composable(
        route = "${AppDestinations.INSPECTION_RESULT}/{status}",
        arguments = listOf(navArgument("status") { type = NavType.StringType })
      ) { backStackEntry ->
        val status = backStackEntry.arguments?.getString("status") ?: "SENT"

        BackHandler {
          // Default to MO list
          draftViewModel.clear()
          navController.navigate(AppDestinations.MO_LIST) {
            popUpTo(AppDestinations.HOME)
          }
        }

        ResultScreen(
          status = status,
          draftViewModel = draftViewModel,
          onNewEntrySameMo = {
            navController.navigate(AppDestinations.INSPECTION_INPUT) {
              popUpTo(AppDestinations.INSPECTION_INPUT) { inclusive = true }
            }
          },
          onReturnToMoList = {
            navController.navigate(AppDestinations.MO_LIST) {
              popUpTo(AppDestinations.HOME)
            }
          }
        )
      }

      // S7. Outbox Screen
      composable(AppDestinations.OUTBOX) {
        BackHandler {
          navController.popBackStack()
        }
        OutboxScreen(
          outboxSyncManager = outboxSyncManager,
          isOnline = isOnline,
          onBackClick = {
            navController.popBackStack()
          }
        )
      }
    }

    if (showSettingsDialog) {
      SettingsDialog(
        scannerManager = scannerManager,
        networkMonitor = networkMonitor,
        convexDataSource = convexDataSource,
        onDismiss = { showSettingsDialog = false }
      )
    }
  }
}
