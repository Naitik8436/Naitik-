package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ResponsiveDashboardContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: DashboardViewModel = viewModel()

            val isPasswordSet by viewModel.isPasswordSet.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
            val passwordHint by viewModel.passwordHint.collectAsStateWithLifecycle()
            val lockErrorMessage by viewModel.lockErrorMessage.collectAsStateWithLifecycle()
            val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val autoLockEnabled by viewModel.autoLockEnabled.collectAsStateWithLifecycle()
            val lockTimeoutSeconds by viewModel.lockTimeoutSeconds.collectAsStateWithLifecycle()

            val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
            val tasks by viewModel.tasks.collectAsStateWithLifecycle()
            val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
            val deviceDiagnostics by viewModel.deviceDiagnostics.collectAsStateWithLifecycle()

            val vaultSearchQuery by viewModel.vaultSearchQuery.collectAsStateWithLifecycle()
            val vaultCategoryFilter by viewModel.vaultCategoryFilter.collectAsStateWithLifecycle()

            // Auto-lock lifecycle observer on app resume
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.checkAutoLockOnResume()
                        viewModel.refreshDiagnostics()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val isDarkTheme = when (themeMode) {
                1 -> true
                2 -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                ResponsiveDashboardContainer(
                    isPasswordSet = isPasswordSet,
                    isLocked = isLocked,
                    passwordHint = passwordHint,
                    lockErrorMessage = lockErrorMessage,
                    activeTab = activeTab,
                    vaultItems = vaultItems,
                    tasks = tasks,
                    auditLogs = auditLogs,
                    deviceDiagnostics = deviceDiagnostics,
                    themeMode = themeMode,
                    autoLockEnabled = autoLockEnabled,
                    lockTimeoutSeconds = lockTimeoutSeconds,
                    vaultSearchQuery = vaultSearchQuery,
                    vaultCategoryFilter = vaultCategoryFilter,
                    onUnlock = { password -> viewModel.unlockDashboard(password) },
                    onSetPassword = { pass, hint, answer -> viewModel.setMasterPassword(pass, hint, answer) },
                    onSecurityAnswerUnlock = { answer -> viewModel.unlockWithSecurityAnswer(answer) },
                    onSelectTab = { tab -> viewModel.selectTab(tab) },
                    onLockClicked = { viewModel.lockDashboard() },
                    onVaultSearchQueryChange = { query -> viewModel.setVaultSearchQuery(query) },
                    onVaultCategoryFilterChange = { category -> viewModel.setVaultCategoryFilter(category) },
                    onToggleVaultFavorite = { item -> viewModel.toggleVaultFavorite(item) },
                    onDeleteVaultItem = { item -> viewModel.deleteVaultItem(item) },
                    onAddVaultItem = { title, user, secret, category, notes ->
                        viewModel.addVaultItem(title, user, secret, category, notes)
                    },
                    onAddTask = { title, desc, prio -> viewModel.addTask(title, desc, prio) },
                    onUpdateTaskStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onRefreshDiagnostics = { viewModel.refreshDiagnostics() },
                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                    onAutoLockEnabledChange = { enabled -> viewModel.setAutoLockEnabled(enabled) },
                    onLockTimeoutSecondsChange = { seconds -> viewModel.setLockTimeoutSeconds(seconds) },
                    onChangePassword = { newPass, hint, answer ->
                        viewModel.setMasterPassword(newPass, hint, answer)
                    },
                    onClearAllData = { viewModel.clearAllData() }
                )
            }
        }
    }
}
