package com.example.data.repository

import com.example.data.db.AuditLogDao
import com.example.data.db.TaskDao
import com.example.data.db.VaultDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DashboardRepository(
    private val vaultDao: VaultDao,
    private val taskDao: TaskDao,
    private val auditLogDao: AuditLogDao,
    val securityPreferences: SecurityPreferences
) {
    val allVaultItems: Flow<List<VaultItem>> = vaultDao.getAllVaultItems()
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val recentAuditLogs: Flow<List<AuditLog>> = auditLogDao.getRecentLogs()

    suspend fun insertVaultItem(item: VaultItem) {
        vaultDao.insertVaultItem(item)
        logEvent("Vault Added", "Created item '${item.title}'", LogLevel.INFO)
    }

    suspend fun updateVaultItem(item: VaultItem) {
        vaultDao.updateVaultItem(item)
        logEvent("Vault Updated", "Updated item '${item.title}'", LogLevel.INFO)
    }

    suspend fun deleteVaultItem(item: VaultItem) {
        vaultDao.deleteVaultItem(item)
        logEvent("Vault Deleted", "Removed item '${item.title}'", LogLevel.WARNING)
    }

    suspend fun insertTask(task: TaskItem) {
        taskDao.insertTask(task)
        logEvent("Task Created", "Created task '${task.title}'", LogLevel.INFO)
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
        logEvent("Task Updated", "Updated task status to ${task.status}", LogLevel.INFO)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
        logEvent("Task Removed", "Deleted task '${task.title}'", LogLevel.WARNING)
    }

    suspend fun logEvent(title: String, detail: String, level: LogLevel = LogLevel.INFO) {
        auditLogDao.insertLog(AuditLog(title = title, detail = detail, level = level))
    }

    suspend fun seedInitialDataIfNeeded() {
        val vaultList = allVaultItems.first()
        if (vaultList.isEmpty()) {
            vaultDao.insertVaultItem(
                VaultItem(
                    title = "Primary Email Account",
                    usernameOrKey = "admin@company.org",
                    secretValue = "P@ssw0rd12345!",
                    category = VaultCategory.CREDENTIAL,
                    notes = "Master administrator email credentials",
                    isFavorite = true
                )
            )
            vaultDao.insertVaultItem(
                VaultItem(
                    title = "Database API Key",
                    usernameOrKey = "PROD_GCP_KEY_v2",
                    secretValue = "AIzaSyD-x9876543210_SecureKeyToken",
                    category = VaultCategory.API_KEY,
                    notes = "Production Firestore / Cloud API access key",
                    isFavorite = true
                )
            )
            vaultDao.insertVaultItem(
                VaultItem(
                    title = "Server Infrastructure Note",
                    usernameOrKey = "SSH Node 01",
                    secretValue = "192.168.1.105 - Port 2222",
                    category = VaultCategory.NOTE,
                    notes = "Backup server connection config and access instructions",
                    isFavorite = false
                )
            )
        }

        val taskList = allTasks.first()
        if (taskList.isEmpty()) {
            taskDao.insertTask(
                TaskItem(
                    title = "Audit Dashboard Access Logs",
                    description = "Verify user access timestamps and verify active lock settings",
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.IN_PROGRESS
                )
            )
            taskDao.insertTask(
                TaskItem(
                    title = "Perform System Diagnostics",
                    description = "Check memory, storage, and battery telemetry across test devices",
                    priority = TaskPriority.URGENT,
                    status = TaskStatus.TODO
                )
            )
            taskDao.insertTask(
                TaskItem(
                    title = "Configure Master Passcode",
                    description = "Set up high-entropy master password and security hint",
                    priority = TaskPriority.MEDIUM,
                    status = TaskStatus.DONE
                )
            )
        }

        val logs = recentAuditLogs.first()
        if (logs.isEmpty()) {
            logEvent("System Boot", "Secure Dashboard initialized with active encryption", LogLevel.SUCCESS)
            logEvent("Security Audit", "Device telemetry check passed OK", LogLevel.INFO)
        }
    }
}
