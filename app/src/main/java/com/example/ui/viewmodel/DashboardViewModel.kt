package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DashboardRepository
import com.example.data.repository.SecurityPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DashboardTab {
    OVERVIEW,
    VAULT,
    TASKS,
    DIAGNOSTICS,
    SETTINGS
}

data class DeviceDiagnostics(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val storageUsedGb: Double = 0.0,
    val storageTotalGb: Double = 0.0,
    val storagePercentage: Int = 0,
    val memoryUsedMb: Long = 0,
    val memoryMaxMb: Long = 0,
    val osVersion: String = "",
    val deviceModel: String = "",
    val networkConnected: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val securityPrefs = SecurityPreferences(application)
    val repository = DashboardRepository(
        vaultDao = db.vaultDao(),
        taskDao = db.taskDao(),
        auditLogDao = db.auditLogDao(),
        securityPreferences = securityPrefs
    )

    // Security State
    private val _isPasswordSet = MutableStateFlow(securityPrefs.isPasswordSet())
    val isPasswordSet: StateFlow<Boolean> = _isPasswordSet.asStateFlow()

    private val _isLocked = MutableStateFlow(securityPrefs.isPasswordSet())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _passwordHint = MutableStateFlow(securityPrefs.getPasswordHint())
    val passwordHint: StateFlow<String> = _passwordHint.asStateFlow()

    private val _lockErrorMessage = MutableStateFlow<String?>(null)
    val lockErrorMessage: StateFlow<String?> = _lockErrorMessage.asStateFlow()

    // Navigation & Theme
    private val _activeTab = MutableStateFlow(DashboardTab.OVERVIEW)
    val activeTab: StateFlow<DashboardTab> = _activeTab.asStateFlow()

    private val _themeMode = MutableStateFlow(securityPrefs.getThemeMode())
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _autoLockEnabled = MutableStateFlow(securityPrefs.isAutoLockEnabled())
    val autoLockEnabled: StateFlow<Boolean> = _autoLockEnabled.asStateFlow()

    private val _lockTimeoutSeconds = MutableStateFlow(securityPrefs.getLockTimeoutSeconds())
    val lockTimeoutSeconds: StateFlow<Int> = _lockTimeoutSeconds.asStateFlow()

    // Data Streams
    val vaultItems: StateFlow<List<VaultItem>> = repository.allVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Diagnostics State
    private val _deviceDiagnostics = MutableStateFlow(fetchDiagnostics())
    val deviceDiagnostics: StateFlow<DeviceDiagnostics> = _deviceDiagnostics.asStateFlow()

    // Search & Filter States
    private val _vaultSearchQuery = MutableStateFlow("")
    val vaultSearchQuery: StateFlow<String> = _vaultSearchQuery.asStateFlow()

    private val _vaultCategoryFilter = MutableStateFlow<VaultCategory?>(null)
    val vaultCategoryFilter: StateFlow<VaultCategory?> = _vaultCategoryFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun setMasterPassword(password: String, hint: String, answer: String): Boolean {
        if (password.length < 4) {
            _lockErrorMessage.value = "Password must be at least 4 characters"
            return false
        }
        securityPrefs.setPassword(password, hint, answer)
        _isPasswordSet.value = true
        _isLocked.value = false
        _passwordHint.value = hint
        _lockErrorMessage.value = null
        
        viewModelScope.launch {
            repository.logEvent("Password Configured", "Master password set successfully", LogLevel.SUCCESS)
        }
        return true
    }

    fun unlockDashboard(password: String): Boolean {
        val success = securityPrefs.verifyPassword(password)
        if (success) {
            _isLocked.value = false
            _lockErrorMessage.value = null
            viewModelScope.launch {
                repository.logEvent("Dashboard Unlocked", "Access granted via password verification", LogLevel.SUCCESS)
            }
        } else {
            val failed = securityPrefs.getFailedAttempts()
            _lockErrorMessage.value = "Incorrect password (Attempt $failed)"
            viewModelScope.launch {
                repository.logEvent("Unlock Failed", "Failed attempt #$failed", LogLevel.SECURITY)
            }
        }
        return success
    }

    fun unlockWithSecurityAnswer(answer: String): Boolean {
        val success = securityPrefs.verifySecurityAnswer(answer)
        if (success) {
            _isLocked.value = false
            _lockErrorMessage.value = null
            viewModelScope.launch {
                repository.logEvent("Security Override", "Unlocked via security question", LogLevel.WARNING)
            }
        } else {
            _lockErrorMessage.value = "Incorrect security answer"
        }
        return success
    }

    fun lockDashboard() {
        _isLocked.value = true
        _lockErrorMessage.value = null
        viewModelScope.launch {
            repository.logEvent("Dashboard Locked", "User locked workspace session", LogLevel.INFO)
        }
    }

    fun checkAutoLockOnResume() {
        if (_isPasswordSet.value && securityPrefs.isSessionExpired()) {
            _isLocked.value = true
        }
    }

    fun selectTab(tab: DashboardTab) {
        _activeTab.value = tab
    }

    fun setVaultSearchQuery(query: String) {
        _vaultSearchQuery.value = query
    }

    fun setVaultCategoryFilter(category: VaultCategory?) {
        _vaultCategoryFilter.value = category
    }

    fun addVaultItem(title: String, usernameOrKey: String, secretValue: String, category: VaultCategory, notes: String) {
        viewModelScope.launch {
            val item = VaultItem(
                title = title,
                usernameOrKey = usernameOrKey,
                secretValue = secretValue,
                category = category,
                notes = notes
            )
            repository.insertVaultItem(item)
        }
    }

    fun toggleVaultFavorite(item: VaultItem) {
        viewModelScope.launch {
            repository.updateVaultItem(item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteVaultItem(item)
        }
    }

    fun addTask(title: String, description: String, priority: TaskPriority) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                description = description,
                priority = priority,
                status = TaskStatus.TODO
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: TaskItem, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun refreshDiagnostics() {
        _deviceDiagnostics.value = fetchDiagnostics()
        viewModelScope.launch {
            repository.logEvent("Diagnostics Refreshed", "Updated real-time telemetry metrics", LogLevel.INFO)
        }
    }

    fun setThemeMode(mode: Int) {
        securityPrefs.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun setAutoLockEnabled(enabled: Boolean) {
        securityPrefs.setAutoLockEnabled(enabled)
        _autoLockEnabled.value = enabled
    }

    fun setLockTimeoutSeconds(seconds: Int) {
        securityPrefs.setLockTimeoutSeconds(seconds)
        _lockTimeoutSeconds.value = seconds
    }

    fun clearAllData() {
        viewModelScope.launch {
            db.clearAllTables()
            securityPrefs.clearAllData()
            _isPasswordSet.value = false
            _isLocked.value = false
            _passwordHint.value = ""
            repository.logEvent("Reset", "All user data and settings cleared", LogLevel.WARNING)
        }
    }

    private fun fetchDiagnostics(): DeviceDiagnostics {
        val context = getApplication<Application>()
        
        // Battery
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Storage
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes

        val totalGb = totalBytes.toDouble() / (1024 * 1024 * 1024)
        val usedGb = usedBytes.toDouble() / (1024 * 1024 * 1024)
        val pct = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0

        // Memory
        val runtime = Runtime.getRuntime()
        val maxMemoryMb = runtime.maxMemory() / (1024 * 1024)
        val allocatedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        return DeviceDiagnostics(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            storageUsedGb = usedGb,
            storageTotalGb = totalGb,
            storagePercentage = pct,
            memoryUsedMb = allocatedMemoryMb,
            memoryMaxMb = maxMemoryMb,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER.capitalize()} ${Build.MODEL}",
            networkConnected = true
        )
    }
}
