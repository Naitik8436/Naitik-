package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.VaultCategory
import com.example.data.model.VaultItem
import com.example.ui.screens.*
import com.example.ui.theme.PrimaryCyan
import com.example.ui.viewmodel.DashboardTab
import com.example.ui.viewmodel.DeviceDiagnostics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveDashboardContainer(
    isPasswordSet: Boolean,
    isLocked: Boolean,
    passwordHint: String,
    lockErrorMessage: String?,
    activeTab: DashboardTab,
    vaultItems: List<VaultItem>,
    tasks: List<TaskItem>,
    auditLogs: List<AuditLog>,
    deviceDiagnostics: DeviceDiagnostics,
    themeMode: Int,
    autoLockEnabled: Boolean,
    lockTimeoutSeconds: Int,
    vaultSearchQuery: String,
    vaultCategoryFilter: VaultCategory?,
    onUnlock: (String) -> Boolean,
    onSetPassword: (String, String, String) -> Boolean,
    onSecurityAnswerUnlock: (String) -> Boolean,
    onSelectTab: (DashboardTab) -> Unit,
    onLockClicked: () -> Unit,
    onVaultSearchQueryChange: (String) -> Unit,
    onVaultCategoryFilterChange: (VaultCategory?) -> Unit,
    onToggleVaultFavorite: (VaultItem) -> Unit,
    onDeleteVaultItem: (VaultItem) -> Unit,
    onAddVaultItem: (String, String, String, VaultCategory, String) -> Unit,
    onAddTask: (String, String, TaskPriority) -> Unit,
    onUpdateTaskStatus: (TaskItem, TaskStatus) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onAutoLockEnabledChange: (Boolean) -> Unit,
    onLockTimeoutSecondsChange: (Int) -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onClearAllData: () -> Unit
) {
    if (isLocked) {
        LockScreen(
            isPasswordSet = isPasswordSet,
            passwordHint = passwordHint,
            errorMessage = lockErrorMessage,
            onUnlock = onUnlock,
            onSetPassword = onSetPassword,
            onSecurityAnswerUnlock = onSecurityAnswerUnlock
        )
    } else {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isExpandedWidth = maxWidth >= 600.dp

            if (isExpandedWidth) {
                // Expanded Screen Layout: Navigation Rail + Main Content
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    NavigationRail(
                        modifier = Modifier.testTag("desktop_navigation_rail"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            IconButton(
                                onClick = onLockClicked,
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .testTag("rail_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Lock Dashboard",
                                    tint = PrimaryCyan
                                )
                            }
                        }
                    ) {
                        NavigationRailItem(
                            selected = activeTab == DashboardTab.OVERVIEW,
                            onClick = { onSelectTab(DashboardTab.OVERVIEW) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                            label = { Text("Overview") },
                            modifier = Modifier.testTag("rail_tab_overview")
                        )
                        NavigationRailItem(
                            selected = activeTab == DashboardTab.VAULT,
                            onClick = { onSelectTab(DashboardTab.VAULT) },
                            icon = { Icon(Icons.Default.VpnKey, contentDescription = "Vault") },
                            label = { Text("Vault") },
                            modifier = Modifier.testTag("rail_tab_vault")
                        )
                        NavigationRailItem(
                            selected = activeTab == DashboardTab.TASKS,
                            onClick = { onSelectTab(DashboardTab.TASKS) },
                            icon = { Icon(Icons.Default.Task, contentDescription = "Tasks") },
                            label = { Text("Tasks") },
                            modifier = Modifier.testTag("rail_tab_tasks")
                        )
                        NavigationRailItem(
                            selected = activeTab == DashboardTab.DIAGNOSTICS,
                            onClick = { onSelectTab(DashboardTab.DIAGNOSTICS) },
                            icon = { Icon(Icons.Default.Speed, contentDescription = "Telemetry") },
                            label = { Text("Diagnostics") },
                            modifier = Modifier.testTag("rail_tab_diagnostics")
                        )
                        NavigationRailItem(
                            selected = activeTab == DashboardTab.SETTINGS,
                            onClick = { onSelectTab(DashboardTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") },
                            modifier = Modifier.testTag("rail_tab_settings")
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ActiveScreenContent(
                            activeTab = activeTab,
                            vaultCount = vaultItems.size,
                            pendingTaskCount = tasks.count { it.status != TaskStatus.DONE },
                            deviceDiagnostics = deviceDiagnostics,
                            auditLogs = auditLogs,
                            vaultItems = vaultItems,
                            tasks = tasks,
                            vaultSearchQuery = vaultSearchQuery,
                            vaultCategoryFilter = vaultCategoryFilter,
                            themeMode = themeMode,
                            autoLockEnabled = autoLockEnabled,
                            lockTimeoutSeconds = lockTimeoutSeconds,
                            onSelectTab = onSelectTab,
                            onLockClicked = onLockClicked,
                            onVaultSearchQueryChange = onVaultSearchQueryChange,
                            onVaultCategoryFilterChange = onVaultCategoryFilterChange,
                            onToggleVaultFavorite = onToggleVaultFavorite,
                            onDeleteVaultItem = onDeleteVaultItem,
                            onAddVaultItem = onAddVaultItem,
                            onAddTask = onAddTask,
                            onUpdateTaskStatus = onUpdateTaskStatus,
                            onDeleteTask = onDeleteTask,
                            onRefreshDiagnostics = onRefreshDiagnostics,
                            onThemeModeChange = onThemeModeChange,
                            onAutoLockEnabledChange = onAutoLockEnabledChange,
                            onLockTimeoutSecondsChange = onLockTimeoutSecondsChange,
                            onChangePassword = onChangePassword,
                            onClearAllData = onClearAllData,
                            isExpandedScreen = true
                        )
                    }
                }
            } else {
                // Compact Screen Layout: Top Bar + Content + Bottom NavigationBar
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (activeTab) {
                                        DashboardTab.OVERVIEW -> "Secure Dashboard"
                                        DashboardTab.VAULT -> "Vault & Credentials"
                                        DashboardTab.TASKS -> "Project Workspace"
                                        DashboardTab.DIAGNOSTICS -> "Hardware Diagnostics"
                                        DashboardTab.SETTINGS -> "Dashboard Settings"
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = onLockClicked,
                                    modifier = Modifier.testTag("top_app_bar_lock")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = PrimaryCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                selected = activeTab == DashboardTab.OVERVIEW,
                                onClick = { onSelectTab(DashboardTab.OVERVIEW) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                label = { Text("Overview") },
                                modifier = Modifier.testTag("nav_overview")
                            )
                            NavigationBarItem(
                                selected = activeTab == DashboardTab.VAULT,
                                onClick = { onSelectTab(DashboardTab.VAULT) },
                                icon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                                label = { Text("Vault") },
                                modifier = Modifier.testTag("nav_vault")
                            )
                            NavigationBarItem(
                                selected = activeTab == DashboardTab.TASKS,
                                onClick = { onSelectTab(DashboardTab.TASKS) },
                                icon = { Icon(Icons.Default.Task, contentDescription = null) },
                                label = { Text("Tasks") },
                                modifier = Modifier.testTag("nav_tasks")
                            )
                            NavigationBarItem(
                                selected = activeTab == DashboardTab.DIAGNOSTICS,
                                onClick = { onSelectTab(DashboardTab.DIAGNOSTICS) },
                                icon = { Icon(Icons.Default.Speed, contentDescription = null) },
                                label = { Text("Metrics") },
                                modifier = Modifier.testTag("nav_metrics")
                            )
                            NavigationBarItem(
                                selected = activeTab == DashboardTab.SETTINGS,
                                onClick = { onSelectTab(DashboardTab.SETTINGS) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        ActiveScreenContent(
                            activeTab = activeTab,
                            vaultCount = vaultItems.size,
                            pendingTaskCount = tasks.count { it.status != TaskStatus.DONE },
                            deviceDiagnostics = deviceDiagnostics,
                            auditLogs = auditLogs,
                            vaultItems = vaultItems,
                            tasks = tasks,
                            vaultSearchQuery = vaultSearchQuery,
                            vaultCategoryFilter = vaultCategoryFilter,
                            themeMode = themeMode,
                            autoLockEnabled = autoLockEnabled,
                            lockTimeoutSeconds = lockTimeoutSeconds,
                            onSelectTab = onSelectTab,
                            onLockClicked = onLockClicked,
                            onVaultSearchQueryChange = onVaultSearchQueryChange,
                            onVaultCategoryFilterChange = onVaultCategoryFilterChange,
                            onToggleVaultFavorite = onToggleVaultFavorite,
                            onDeleteVaultItem = onDeleteVaultItem,
                            onAddVaultItem = onAddVaultItem,
                            onAddTask = onAddTask,
                            onUpdateTaskStatus = onUpdateTaskStatus,
                            onDeleteTask = onDeleteTask,
                            onRefreshDiagnostics = onRefreshDiagnostics,
                            onThemeModeChange = onThemeModeChange,
                            onAutoLockEnabledChange = onAutoLockEnabledChange,
                            onLockTimeoutSecondsChange = onLockTimeoutSecondsChange,
                            onChangePassword = onChangePassword,
                            onClearAllData = onClearAllData,
                            isExpandedScreen = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveScreenContent(
    activeTab: DashboardTab,
    vaultCount: Int,
    pendingTaskCount: Int,
    deviceDiagnostics: DeviceDiagnostics,
    auditLogs: List<AuditLog>,
    vaultItems: List<VaultItem>,
    tasks: List<TaskItem>,
    vaultSearchQuery: String,
    vaultCategoryFilter: VaultCategory?,
    themeMode: Int,
    autoLockEnabled: Boolean,
    lockTimeoutSeconds: Int,
    onSelectTab: (DashboardTab) -> Unit,
    onLockClicked: () -> Unit,
    onVaultSearchQueryChange: (String) -> Unit,
    onVaultCategoryFilterChange: (VaultCategory?) -> Unit,
    onToggleVaultFavorite: (VaultItem) -> Unit,
    onDeleteVaultItem: (VaultItem) -> Unit,
    onAddVaultItem: (String, String, String, VaultCategory, String) -> Unit,
    onAddTask: (String, String, TaskPriority) -> Unit,
    onUpdateTaskStatus: (TaskItem, TaskStatus) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onAutoLockEnabledChange: (Boolean) -> Unit,
    onLockTimeoutSecondsChange: (Int) -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onClearAllData: () -> Unit,
    isExpandedScreen: Boolean
) {
    Crossfade(targetState = activeTab, label = "tab_transition") { tab ->
        when (tab) {
            DashboardTab.OVERVIEW -> OverviewScreen(
                vaultCount = vaultCount,
                pendingTaskCount = pendingTaskCount,
                deviceDiagnostics = deviceDiagnostics,
                auditLogs = auditLogs,
                onNavigateTab = onSelectTab,
                onLockClicked = onLockClicked,
                isExpandedScreen = isExpandedScreen
            )
            DashboardTab.VAULT -> VaultScreen(
                vaultItems = vaultItems,
                searchQuery = vaultSearchQuery,
                onSearchQueryChange = onVaultSearchQueryChange,
                categoryFilter = vaultCategoryFilter,
                onCategoryFilterChange = onVaultCategoryFilterChange,
                onToggleFavorite = onToggleVaultFavorite,
                onDeleteVaultItem = onDeleteVaultItem,
                onAddVaultItem = onAddVaultItem
            )
            DashboardTab.TASKS -> TasksScreen(
                tasks = tasks,
                onAddTask = onAddTask,
                onUpdateTaskStatus = onUpdateTaskStatus,
                onDeleteTask = onDeleteTask
            )
            DashboardTab.DIAGNOSTICS -> DiagnosticsScreen(
                deviceDiagnostics = deviceDiagnostics,
                onRefresh = onRefreshDiagnostics
            )
            DashboardTab.SETTINGS -> SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                autoLockEnabled = autoLockEnabled,
                onAutoLockEnabledChange = onAutoLockEnabledChange,
                lockTimeoutSeconds = lockTimeoutSeconds,
                onLockTimeoutSecondsChange = onLockTimeoutSecondsChange,
                onChangePassword = onChangePassword,
                onClearAllData = onClearAllData,
                onLockNow = onLockClicked
            )
        }
    }
}
