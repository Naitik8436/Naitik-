package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("secure_dashboard_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSWORD_HASH = "key_password_hash"
        private const val KEY_PASSWORD_HINT = "key_password_hint"
        private const val KEY_AUTO_LOCK_ENABLED = "key_auto_lock_enabled"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "key_lock_timeout_seconds"
        private const val KEY_LAST_UNLOCKED = "key_last_unlocked"
        private const val KEY_THEME_MODE = "key_theme_mode" // 0: SYSTEM, 1: DARK, 2: LIGHT
        private const val KEY_SECURITY_QUESTIONS_SET = "key_security_questions_set"
        private const val KEY_SECURITY_ANSWER_HASH = "key_security_answer_hash"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
    }

    fun isPasswordSet(): Boolean {
        return prefs.getString(KEY_PASSWORD_HASH, null) != null
    }

    fun setPassword(password: String, hint: String = "", securityAnswer: String = "") {
        val hash = hashString(password)
        val answerHash = if (securityAnswer.isNotBlank()) hashString(securityAnswer.lowercase().trim()) else ""
        
        prefs.edit()
            .putString(KEY_PASSWORD_HASH, hash)
            .putString(KEY_PASSWORD_HINT, hint)
            .putString(KEY_SECURITY_ANSWER_HASH, answerHash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
    }

    fun verifyPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        val inputHash = hashString(password)
        val matches = storedHash == inputHash
        if (matches) {
            resetFailedAttempts()
            recordUnlockTime()
        } else {
            incrementFailedAttempts()
        }
        return matches
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val storedHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        if (storedHash.isBlank()) return false
        return storedHash == hashString(answer.lowercase().trim())
    }

    fun getPasswordHint(): String {
        return prefs.getString(KEY_PASSWORD_HINT, "") ?: ""
    }

    fun isAutoLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LOCK_ENABLED, true)
    }

    fun setAutoLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCK_ENABLED, enabled).apply()
    }

    fun getLockTimeoutSeconds(): Int {
        return prefs.getInt(KEY_LOCK_TIMEOUT_SECONDS, 60) // Default 60 seconds
    }

    fun setLockTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECONDS, seconds).apply()
    }

    fun recordUnlockTime() {
        prefs.edit().putLong(KEY_LAST_UNLOCKED, System.currentTimeMillis()).apply()
    }

    fun isSessionExpired(): Boolean {
        if (!isAutoLockEnabled()) return false
        val lastUnlocked = prefs.getLong(KEY_LAST_UNLOCKED, 0L)
        if (lastUnlocked == 0L) return true
        val timeoutMs = getLockTimeoutSeconds() * 1000L
        return System.currentTimeMillis() - lastUnlocked > timeoutMs
    }

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, 1) // Default DARK mode for executive look
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getFailedAttempts(): Int {
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    private fun incrementFailedAttempts() {
        val current = getFailedAttempts()
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, current + 1).apply()
    }

    fun resetFailedAttempts() {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
