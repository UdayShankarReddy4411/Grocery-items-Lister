package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "grocery_lists")
data class GroceryList(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncCode: String? = null, // e.g. "BANANA" if live shared
    val lastSyncedAt: Long = 0L
)

@Entity(
    tableName = "grocery_items",
    indices = [Index(value = ["listId"])]
)
data class GroceryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val name: String,
    val quantity: String, // e.g. "2 packs", "5 kg"
    val category: String = "Other", // Vegetables, Dairy, Snacks, Beverages, etc.
    val notes: String = "",
    val isAddedToCart: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val updatedBy: String = "Me" // Who changed it (useful for collab logs)
)
