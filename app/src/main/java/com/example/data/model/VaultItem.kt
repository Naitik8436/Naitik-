package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultCategory {
    CREDENTIAL,
    NOTE,
    API_KEY,
    FINANCIAL,
    PERSONAL
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val usernameOrKey: String = "",
    val secretValue: String,
    val category: VaultCategory = VaultCategory.CREDENTIAL,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
