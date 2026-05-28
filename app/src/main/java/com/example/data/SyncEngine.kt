package com.example.data

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SyncItemPayload(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val notes: String,
    val isAddedToCart: Boolean,
    val addedAt: Long,
    val lastUpdated: Long,
    val updatedBy: String
)

@JsonClass(generateAdapter = true)
data class SyncListPayload(
    val id: String,
    val name: String,
    val createdAt: Long,
    val items: List<SyncItemPayload>,
    val lastUpdated: Long
)

object SyncEngine {
    private const val TAG = "SyncEngine"
    private const val KVDB_BUCKET = "grocerynotebook_aistudio_v1_sync"
    private const val BASE_URL = "https://kvdb.io"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listAdapter = moshi.adapter(SyncListPayload::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads a grocery list and its items to kvdb.io for real-time sync.
     */
    suspend fun uploadListState(
        list: GroceryList,
        items: List<GroceryItem>,
        roomCode: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (roomCode.isBlank()) return@withContext false

        try {
            val syncItems = items.map {
                SyncItemPayload(
                    id = it.id,
                    name = it.name,
                    quantity = it.quantity,
                    category = it.category,
                    notes = it.notes,
                    isAddedToCart = it.isAddedToCart,
                    addedAt = it.addedAt,
                    lastUpdated = it.lastUpdated,
                    updatedBy = it.updatedBy
                )
            }

            val payload = SyncListPayload(
                id = list.id,
                name = list.name,
                createdAt = list.createdAt,
                items = syncItems,
                lastUpdated = System.currentTimeMillis()
            )

            val json = listAdapter.toJson(payload)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val finalRoomKey = roomCode.trim().uppercase()

            val request = Request.Builder()
                .url("$BASE_URL/$KVDB_BUCKET/$finalRoomKey")
                .put(json.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to upload list: HTTP code ${response.code}")
                    return@withContext false
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during upload", e)
            return@withContext false
        }
    }

    /**
     * Downloads list state from kvdb.io by room code.
     */
    suspend fun downloadListState(roomCode: String): SyncListPayload? = withContext(Dispatchers.IO) {
        if (roomCode.isBlank()) return@withContext null

        try {
            val finalRoomKey = roomCode.trim().uppercase()
            val request = Request.Builder()
                .url("$BASE_URL/$KVDB_BUCKET/$finalRoomKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 404) {
                    Log.d(TAG, "No remote list found for code $finalRoomKey")
                    return@withContext null
                }
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to download list: HTTP code ${response.code}")
                    return@withContext null
                }
                val json = response.body?.string() ?: return@withContext null
                return@withContext listAdapter.fromJson(json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during download", e)
            return@withContext null
        }
    }
}
