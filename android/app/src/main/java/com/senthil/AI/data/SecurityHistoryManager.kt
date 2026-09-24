package com.senthil.AI.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class SecurityHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val scanType: String,
    val target: String,
    val verdict: String,
    val score: Int = 100,
    val severity: String = "Low", // "Safe", "Low", "Medium", "High", "Critical"
    val timestamp: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
    val createdAt: String = ""
)

object SecurityHistoryManager {
    private const val PREFS_NAME = "sentinel_security_history"
    private const val KEY_HISTORY = "history_records"
    private const val KEY_USER_EMAIL = "cached_user_email"
    private const val KEY_USER_TOKEN = "cached_user_token"
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun saveUserSession(context: Context, email: String, token: String) {
        val cleanEmail = email.trim().lowercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_EMAIL, cleanEmail)
            .putString(KEY_USER_TOKEN, token)
            .apply()
    }

    fun clearUserSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_TOKEN)
            .remove(KEY_HISTORY)
            .apply()
    }

    fun getCachedEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getString(KEY_USER_EMAIL, null)
        if (!cached.isNullOrBlank()) return cached.trim().lowercase()
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
        } catch (e: Exception) {
            null
        }
    }

    fun getCachedToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    @Synchronized
    fun recordScan(
        context: Context,
        scanType: String,
        target: String,
        verdict: String,
        score: Int = 100,
        severity: String = "Low"
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentList = getHistory(context).toMutableList()
        val nowIso = try { java.time.Instant.now().toString() } catch (e: Exception) { "" }
        val newItem = SecurityHistoryItem(
            scanType = scanType,
            target = target,
            verdict = verdict,
            score = score,
            severity = severity,
            createdAt = nowIso
        )
        currentList.add(0, newItem) // Most recent first
        // Retain last 50 entries locally
        val trimmed = if (currentList.size > 50) currentList.take(50) else currentList
        prefs.edit().putString(KEY_HISTORY, gson.toJson(trimmed)).apply()

        // Asynchronously post to backend MongoDB so it is immediately visible on Web
        scope.launch {
            try {
                val email = getCachedEmail(context)?.trim()?.lowercase()
                val token = getCachedToken(context)
                val req = RecordHistoryRequest(
                    scan_type = scanType,
                    target = target,
                    verdict = verdict,
                    score = score,
                    severity = severity,
                    timestamp = newItem.timestamp
                )
                val response = SentinelApiClient.instance.recordScanHistory(
                    token = token,
                    email = email,
                    req = req
                )
                if (response.id.isNotBlank()) {
                    // Update the local item with the MongoDB ObjectId
                    updateItemId(context, newItem.id, response.id)
                }
            } catch (e: Exception) {
                // If offline or network error, locally recorded scan is preserved
            }
        }
    }

    @Synchronized
    private fun updateItemId(context: Context, localId: String, mongoId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentList = getHistory(context).toMutableList()
        val idx = currentList.indexOfFirst { it.id == localId }
        if (idx != -1) {
            val old = currentList[idx]
            currentList[idx] = old.copy(id = mongoId)
            prefs.edit().putString(KEY_HISTORY, gson.toJson(currentList)).apply()
        }
    }

    fun formatScanTimestamp(rawTimestamp: String?, createdAt: String?): String {
        val source = if (!createdAt.isNullOrBlank()) createdAt else rawTimestamp
        if (!source.isNullOrBlank()) {
            val clean = source.trim()
            if (clean.contains("T") || (clean.contains("-") && clean.contains(":"))) {
                try {
                    val isoString = if (!clean.endsWith("Z") && !clean.contains("+") && !clean.matches(Regex(".*-\\d{2}:\\d{2}$"))) {
                        clean.replace(" ", "T") + "Z"
                    } else {
                        clean.replace(" ", "T")
                    }
                    val instant = java.time.Instant.parse(isoString)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())
                        .withZone(java.time.ZoneId.systemDefault())
                    return formatter.format(instant)
                } catch (ignored: Exception) {}

                val isoFormats = arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss"
                )
                val withoutZ = clean.replace("Z", "")
                for (pattern in isoFormats) {
                    try {
                        val sdf = SimpleDateFormat(pattern, Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        val date = sdf.parse(withoutZ)
                        if (date != null) {
                            val localFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            localFormat.timeZone = TimeZone.getDefault()
                            return localFormat.format(date)
                        }
                    } catch (ignored: Exception) {}
                }
            }
        }

        if (!rawTimestamp.isNullOrBlank() && rawTimestamp != "Recent") {
            return rawTimestamp
        }
        return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
    }

    @Synchronized
    fun getHistory(context: Context): List<SecurityHistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SecurityHistoryItem>>() {}.type
            val list: List<SecurityHistoryItem> = gson.fromJson(json, type) ?: emptyList()
            list.map {
                if (it.createdAt.isNotBlank()) {
                    it.copy(timestamp = formatScanTimestamp(it.timestamp, it.createdAt))
                } else {
                    it
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    private fun saveHistoryList(context: Context, list: List<SecurityHistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    suspend fun fetchAndSyncWithBackend(
        context: Context,
        token: String? = null,
        email: String? = null
    ): List<SecurityHistoryItem> = withContext(Dispatchers.IO) {
        val effectiveEmail = (email ?: getCachedEmail(context))?.trim()?.lowercase()
        val effectiveToken = token ?: getCachedToken(context)

        try {
            val res = SentinelApiClient.instance.getScanHistory(
                token = effectiveToken,
                email = effectiveEmail
            )
            val remoteItems = res.history.map {
                SecurityHistoryItem(
                    id = it.id,
                    scanType = it.scan_type,
                    target = it.target,
                    verdict = it.verdict,
                    score = it.score,
                    severity = it.severity,
                    timestamp = formatScanTimestamp(it.timestamp, it.created_at),
                    createdAt = it.created_at
                )
            }
            saveHistoryList(context, remoteItems)
            remoteItems
        } catch (e: Exception) {
            // Return cached local history if remote fails
            getHistory(context)
        }
    }

    suspend fun deleteScanItem(
        context: Context,
        itemId: String,
        token: String? = null,
        email: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        // Optimistically remove from local cache
        val currentList = getHistory(context).toMutableList()
        currentList.removeAll { it.id == itemId }
        saveHistoryList(context, currentList)

        val effectiveEmail = (email ?: getCachedEmail(context))?.trim()?.lowercase()
        val effectiveToken = token ?: getCachedToken(context)

        try {
            SentinelApiClient.instance.deleteScanHistoryItem(
                itemId = itemId,
                token = effectiveToken,
                email = effectiveEmail
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearAllHistory(
        context: Context,
        token: String? = null,
        email: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        // Clear locally
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()

        val effectiveEmail = (email ?: getCachedEmail(context))?.trim()?.lowercase()
        val effectiveToken = token ?: getCachedToken(context)

        try {
            SentinelApiClient.instance.clearScanHistory(
                token = effectiveToken,
                email = effectiveEmail
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
