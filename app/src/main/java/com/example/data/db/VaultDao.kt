package com.example.data.db

import androidx.room.*
import com.example.data.model.VaultCategory
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE category = :category ORDER BY updatedAt DESC")
    fun getVaultItemsByCategory(category: VaultCategory): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long

    @Update
    suspend fun updateVaultItem(item: VaultItem)

    @Delete
    suspend fun deleteVaultItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemById(id: Long)

    @Query("DELETE FROM vault_items")
    suspend fun clearAllVaultItems()
}
