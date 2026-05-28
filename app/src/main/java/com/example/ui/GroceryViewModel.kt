package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GroceryDatabase
import com.example.data.GroceryItem
import com.example.data.GroceryList
import com.example.data.GroceryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortBy {
    ALPHABETICAL,
    TIME_ADDED
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class CollabNotification(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository
    init {
        val db = GroceryDatabase.getDatabase(application)
        repository = GroceryRepository(db.groceryDao())
    }

    // --- State Streams ---
    val allLists: StateFlow<List<GroceryList>> = repository.allListsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedListId = MutableStateFlow<String?>(null)
    val selectedListId: StateFlow<String?> = _selectedListId.asStateFlow()

    private val _selectedList = MutableStateFlow<GroceryList?>(null)
    val selectedList: StateFlow<GroceryList?> = _selectedList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.TIME_ADDED)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _collabNotification = MutableStateFlow<CollabNotification?>(null)
    val collabNotification: StateFlow<CollabNotification?> = _collabNotification.asStateFlow()

    private val _simulationActive = MutableStateFlow(false)
    val simulationActive: StateFlow<Boolean> = _simulationActive.asStateFlow()

    private var simulationJob: Job? = null
    private var syncJob: Job? = null

    // Combined Items flow based on currently selected list, search query, sorting, and category filter
    val currentItems: StateFlow<List<GroceryItem>> = _selectedListId
        .flatMapLatest { listId ->
            if (listId != null) {
                repository.getItemsForList(listId)
            } else {
                flowOf(emptyList())
            }
        }
        .combine(_searchQuery) { items, query ->
            if (query.isBlank()) items else items.filter {
                it.name.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedCategory) { items, category ->
            if (category == "All") items else items.filter { it.category == category }
        }
        .combine(_sortBy) { items, sortType ->
            when (sortType) {
                SortBy.ALPHABETICAL -> items.sortedBy { it.name.lowercase() }
                SortBy.TIME_ADDED -> items.sortedByDescending { it.addedAt }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Automatically populate standard default list if none exist
        viewModelScope.launch {
            allLists.collect { lists ->
                if (lists.isEmpty()) {
                    val defaultList = repository.createList("Weekly Groceries")
                    // Pre-fill some delightful, standard grocery notebook items
                    repository.addItem(defaultList.id, "Apples", "6 pcs", "Vegetables", "Red organic", "System")
                    repository.addItem(defaultList.id, "Low-Fat Milk", "2 Litres", "Dairy", "Low-fat version only", "System")
                    repository.addItem(defaultList.id, "Sourdough Bread", "1 loaf", "Snacks", "Freshly baked", "System")
                    repository.addItem(defaultList.id, "Sparkling Water", "6 cans", "Beverages", "Lime flavor", "System")
                }
            }
        }
    }

    // --- Action Methods ---
    fun selectList(listId: String?) {
        _selectedListId.value = listId
        if (listId == null) {
            _selectedList.value = null
            stopAutomaticSync()
        } else {
            viewModelScope.launch {
                val list = repository.getListById(listId)
                _selectedList.value = list
                if (list?.syncCode != null) {
                    startAutomaticSync(listId)
                }
            }
        }
    }

    fun createList(name: String, syncCode: String? = null) {
        viewModelScope.launch {
            val list = repository.createList(name, syncCode)
            selectList(list.id)
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            if (_selectedListId.value == listId) {
                selectList(null)
            }
            repository.deleteListAndItems(listId)
        }
    }

    fun renameList(listId: String, newName: String) {
        viewModelScope.launch {
            repository.renameList(listId, newName)
            // Refresh selected list info
            if (_selectedListId.value == listId) {
                _selectedList.value = repository.getListById(listId)
            }
        }
    }

    fun addItem(name: String, quantity: String, category: String, notes: String) {
        val listId = _selectedListId.value ?: return
        viewModelScope.launch {
            repository.addItem(listId, name, quantity, category, notes, "Me")
            triggerSyncUpload()
        }
    }

    fun toggleItemCart(itemId: String, isAdded: Boolean) {
        val listId = _selectedListId.value ?: return
        viewModelScope.launch {
            repository.toggleItemCart(itemId, listId, isAdded, "Me")
            triggerSyncUpload()
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItemById(itemId)
            triggerSyncUpload()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(sort: SortBy) {
        _sortBy.value = sort
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun clearNotification() {
        _collabNotification.value = null
    }

    // --- Live Collaborative Room Setup ---
    fun joinListSyncRoom(listId: String, roomCode: String) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            val cleanCode = roomCode.trim().uppercase()
            repository.updateListSyncCode(listId, cleanCode)
            _selectedList.value = repository.getListById(listId)

            // Trigger immediate sync round
            val success = repository.performCloudSync(listId)
            if (success) {
                _syncStatus.value = SyncStatus.SUCCESS
                _collabNotification.value = CollabNotification("Joined Live Sync Room: $cleanCode! 🛒")
                startAutomaticSync(listId)
            } else {
                // If remote upload fails but code is set, we still maintain code and wait for future pools,
                // but flag error.
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    fun leaveSyncRoom(listId: String) {
        viewModelScope.launch {
            repository.updateListSyncCode(listId, null)
            _selectedList.value = repository.getListById(listId)
            stopAutomaticSync()
            _collabNotification.value = CollabNotification("Disconnecting from live room")
        }
    }

    // --- Automatic Sync Pooling ---
    private fun startAutomaticSync(listId: String) {
        stopAutomaticSync()
        syncJob = viewModelScope.launch {
            while (true) {
                delay(4000) // Poll every 4 seconds for real-time responsiveness
                try {
                    _syncStatus.value = SyncStatus.SYNCING
                    val success = repository.performCloudSync(listId)
                    _syncStatus.value = if (success) SyncStatus.SUCCESS else SyncStatus.ERROR
                } catch (e: Exception) {
                    _syncStatus.value = SyncStatus.ERROR
                }
            }
        }
    }

    private fun stopAutomaticSync() {
        syncJob?.cancel()
        syncJob = null
        _syncStatus.value = SyncStatus.IDLE
    }

    private fun triggerSyncUpload() {
        val listId = _selectedListId.value ?: return
        val list = _selectedList.value ?: return
        if (list.syncCode != null) {
            viewModelScope.launch {
                try {
                    _syncStatus.value = SyncStatus.SYNCING
                    val success = repository.performCloudSync(listId)
                    _syncStatus.value = if (success) SyncStatus.SUCCESS else SyncStatus.ERROR
                } catch (e: Exception) {
                    _syncStatus.value = SyncStatus.ERROR
                }
            }
        }
    }

    // --- Collaborative Simulation PlayHub ---
    fun toggleSimulation() {
        val active = !_simulationActive.value
        _simulationActive.value = active
        if (active) {
            startSimulationJob()
            _collabNotification.value = CollabNotification("Emily & Dad joined the list collaboration! 🌕🛒")
        } else {
            stopSimulationJob()
            _collabNotification.value = CollabNotification("Live collaboration simulation stopped")
        }
    }

    private fun startSimulationJob() {
        simulationJob?.cancel()
        val listId = _selectedListId.value ?: return

        simulationJob = viewModelScope.launch {
            // Steps of family action simulations
            val actions = listOf(
                suspend {
                    repository.addItem(listId, "Fresh Strawberries", "2 punnets", "Vegetables", "Emily: Get the ripe sweet ones! 🍓", "Emily")
                    _collabNotification.value = CollabNotification("Emily added Fresh Strawberries: 'ripe & sweet ones!'")
                },
                suspend {
                    // Try to toggle low-fat milk
                    val items = currentItems.value
                    val milk = items.find { it.name.contains("Milk", ignoreCase = true) }
                    if (milk != null) {
                        repository.toggleItemCart(milk.id, listId, true, "Dad")
                        _collabNotification.value = CollabNotification("Dad put 'Low-Fat Milk' in the supermarket cart! 🥛")
                    } else {
                        repository.addItem(listId, "Whole Milk", "1 Gallon", "Dairy", "Dad: Grabbed whole milk", "Dad")
                        _collabNotification.value = CollabNotification("Dad added Whole Milk to the list!")
                    }
                },
                suspend {
                    repository.addItem(listId, "Choco Chip Cookies", "1 bag", "Snacks", "Emily: For midnight movie snacking! 🍪", "Emily")
                    _collabNotification.value = CollabNotification("Emily added Choco Chip Cookies: 'Midnight snacking! 🍿'")
                },
                suspend {
                    val items = currentItems.value
                    val cookies = items.find { it.name.contains("Cookies", ignoreCase = true) }
                    if (cookies != null) {
                        repository.toggleItemCart(cookies.id, listId, true, "Emily")
                        _collabNotification.value = CollabNotification("Emily checked off Choco Chip Cookies! 🍪")
                    }
                },
                suspend {
                    repository.addItem(listId, "Greek Yogurt", "500g", "Dairy", "Dad: Honey flavor", "Dad")
                    _collabNotification.value = CollabNotification("Dad added Greek Yogurt: 'Honey flavor'")
                }
            )

            var actionIndex = 0
            while (_simulationActive.value) {
                delay(7000) // Family members do an action every 7 seconds
                if (actionIndex < actions.size) {
                    actions[actionIndex].invoke()
                    actionIndex++
                    triggerSyncUpload()
                } else {
                    // Reset to loop simulation
                    actionIndex = 0
                }
            }
        }
    }

    private fun stopSimulationJob() {
        simulationJob?.cancel()
        simulationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutomaticSync()
        stopSimulationJob()
    }
}
