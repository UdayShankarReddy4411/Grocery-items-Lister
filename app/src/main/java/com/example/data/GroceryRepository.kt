package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GroceryRepository(private val groceryDao: GroceryDao) {

    val allListsFlow: Flow<List<GroceryList>> = groceryDao.getAllListsFlow()

    fun getItemsForList(listId: String): Flow<List<GroceryItem>> =
        groceryDao.getItemsFlowForList(listId)

    suspend fun createList(name: String, syncCode: String? = null): GroceryList {
        val newList = GroceryList(
            id = UUID.randomUUID().toString(),
            name = name,
            syncCode = syncCode?.uppercase()?.trim()
        )
        groceryDao.insertList(newList)
        return newList
    }

    suspend fun getListById(listId: String): GroceryList? {
        return groceryDao.getListById(listId)
    }

    suspend fun getListBySyncCode(syncCode: String): GroceryList? {
        return groceryDao.getListBySyncCode(syncCode.uppercase().trim())
    }

    suspend fun updateListSyncCode(listId: String, syncCode: String?) {
        val current = groceryDao.getListById(listId) ?: return
        val updated = current.copy(
            syncCode = syncCode?.uppercase()?.trim(),
            lastSyncedAt = System.currentTimeMillis()
        )
        groceryDao.insertList(updated)
    }

    suspend fun renameList(listId: String, newName: String) {
        val current = groceryDao.getListById(listId) ?: return
        groceryDao.insertList(current.copy(name = newName))
    }

    suspend fun deleteListAndItems(listId: String) {
        groceryDao.deleteListAndItems(listId)
    }

    suspend fun addItem(
        listId: String,
        name: String,
        quantity: String,
        category: String,
        notes: String,
        createdBy: String = "Me"
    ) {
        val item = GroceryItem(
            listId = listId,
            name = name,
            quantity = quantity,
            category = category,
            notes = notes,
            updatedBy = createdBy
        )
        groceryDao.insertItem(item)
    }

    suspend fun updateItem(item: GroceryItem) {
        groceryDao.insertItem(item.copy(lastUpdated = System.currentTimeMillis()))
    }

    suspend fun toggleItemCart(itemId: String, listId: String, isAdded: Boolean, updatedBy: String = "Me") {
        // Query the item direct, toggle, and save
        val items = groceryDao.getItemsDirectForList(listId)
        val target = items.find { it.id == itemId } ?: return
        val updated = target.copy(
            isAddedToCart = isAdded,
            lastUpdated = System.currentTimeMillis(),
            updatedBy = updatedBy
        )
        groceryDao.insertItem(updated)
    }

    suspend fun deleteItemById(itemId: String) {
        groceryDao.deleteItemById(itemId)
    }

    /**
     * Performs a sync round with kvdb.io for a specified listing
     */
    suspend fun performCloudSync(listId: String): Boolean {
        val localList = groceryDao.getListById(listId) ?: return false
        val roomCode = localList.syncCode ?: return false
        if (roomCode.isBlank()) return false

        Log.d("GroceryRepository", "Starting cloud sync for list $listId with code $roomCode")

        // 1. Download Remote State
        val remotePayload = SyncEngine.downloadListState(roomCode)

        // 2. Query Local Items
        val localItems = groceryDao.getItemsDirectForList(listId)

        if (remotePayload == null) {
            // No remote state exists yet, let's push our local state to establish the room!
            Log.d("GroceryRepository", "No remote payload, uploading local state")
            val successful = SyncEngine.uploadListState(localList, localItems, roomCode)
            if (successful) {
                groceryDao.insertList(localList.copy(lastSyncedAt = System.currentTimeMillis()))
            }
            return successful
        }

        // 3. Merging logic
        // Compare remote and local list metadata (keep newer name if modified)
        val mergedListName = if (remotePayload.lastUpdated > localList.lastSyncedAt) {
            remotePayload.name
        } else {
            localList.name
        }

        val updatedLocalList = localList.copy(
            name = mergedListName,
            lastSyncedAt = System.currentTimeMillis()
        )

        // Merge Items based on ID and timestamps
        val remoteItems = remotePayload.items
        val mergedItems = mutableListOf<GroceryItem>()

        // Maps for O(1) lookups
        val localItemsMap = localItems.associateBy { it.id }.toMutableMap()
        val remoteItemsMap = remoteItems.associateBy { it.id }

        // Process all remote items
        for ((itemId, remoteItem) in remoteItemsMap) {
            val localItem = localItemsMap[itemId]
            if (localItem == null) {
                // Item is on remote but not locally
                mergedItems.add(
                    GroceryItem(
                        id = remoteItem.id,
                        listId = listId,
                        name = remoteItem.name,
                        quantity = remoteItem.quantity,
                        category = remoteItem.category,
                        notes = remoteItem.notes,
                        isAddedToCart = remoteItem.isAddedToCart,
                        addedAt = remoteItem.addedAt,
                        lastUpdated = remoteItem.lastUpdated,
                        updatedBy = remoteItem.updatedBy
                    )
                )
            } else {
                // Item exists in both, resolve conflicts by timestamp
                if (remoteItem.lastUpdated >= localItem.lastUpdated) {
                    mergedItems.add(
                        localItem.copy(
                            name = remoteItem.name,
                            quantity = remoteItem.quantity,
                            category = remoteItem.category,
                            notes = remoteItem.notes,
                            isAddedToCart = remoteItem.isAddedToCart,
                            lastUpdated = remoteItem.lastUpdated,
                            updatedBy = remoteItem.updatedBy
                        )
                    )
                } else {
                    // Local is newer, keep local
                    mergedItems.add(localItem)
                }
                // Remove from map to keep track of local-only items
                localItemsMap.remove(itemId)
            }
        }

        // Remaining local items were not on remote
        for (localItem in localItemsMap.values) {
            // Keep local item
            mergedItems.add(localItem)
        }

        // 4. Save Margins Back Locally
        groceryDao.syncListAndItems(listOf(updatedLocalList), mergedItems)

        // 5. Upload Merged Balance Back online so everyone is instantly refreshed!
        val uploadSuccessful = SyncEngine.uploadListState(updatedLocalList, mergedItems, roomCode)
        return uploadSuccessful
    }
}
