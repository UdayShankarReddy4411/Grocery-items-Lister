package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {

    // --- Grocery Lists ---
    @Query("SELECT * FROM grocery_lists ORDER BY createdAt DESC")
    fun getAllListsFlow(): Flow<List<GroceryList>>

    @Query("SELECT * FROM grocery_lists")
    suspend fun getAllListsDirect(): List<GroceryList>

    @Query("SELECT * FROM grocery_lists WHERE id = :listId")
    suspend fun getListById(listId: String): GroceryList?

    @Query("SELECT * FROM grocery_lists WHERE syncCode = :syncCode")
    suspend fun getListBySyncCode(syncCode: String): GroceryList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: GroceryList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<GroceryList>)

    @Query("DELETE FROM grocery_lists WHERE id = :listId")
    suspend fun deleteListById(listId: String)

    @Query("DELETE FROM grocery_items WHERE listId = :listId")
    suspend fun deleteItemsByListId(listId: String)

    @Transaction
    suspend fun deleteListAndItems(listId: String) {
        deleteItemsByListId(listId)
        deleteListById(listId)
    }

    // --- Grocery Items ---
    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY addedAt DESC")
    fun getItemsFlowForList(listId: String): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE listId = :listId")
    suspend fun getItemsDirectForList(listId: String): List<GroceryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItem>)

    @Delete
    suspend fun deleteItem(item: GroceryItem)

    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    // Sync helpers
    @Transaction
    suspend fun syncListAndItems(lists: List<GroceryList>, items: List<GroceryItem>) {
        insertLists(lists)
        insertItems(items)
    }
}
