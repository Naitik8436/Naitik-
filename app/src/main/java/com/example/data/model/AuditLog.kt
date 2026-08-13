package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LogLevel {
    INFO,
    WARNING,
    SECURITY,
    SUCCESS
}

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val detail: String = "",
    val level: LogLevel = LogLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
)
