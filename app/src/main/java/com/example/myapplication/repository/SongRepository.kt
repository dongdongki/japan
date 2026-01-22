package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.Song
import com.example.myapplication.model.SongInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Song data (song lyrics vocabulary)
 * Automatically scans assets directory for song folders
 */
@Singleton
class SongRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val songDataCache = mutableMapOf<String, List<Song>>()
    private var availableSongs: List<SongInfo>? = null

    init {
        scanAvailableSongs()
    }

    /**
     * Scan assets directory to find all available songs
     * Each song should be in its own directory with a JSON file of the same name
     * Example: pretender/pretender.json
     *
     * OPTIMIZED: Don't load vocabulary during scan - defer to first access
     */
    private fun scanAvailableSongs() {
        try {
            val assetsList = context.assets.list("") ?: emptyArray()
            val songs = mutableListOf<SongInfo>()

            for (item in assetsList) {
                try {
                    // Check if this is a directory by trying to list its contents
                    val subItems = context.assets.list(item)
                    if (subItems != null && subItems.isNotEmpty()) {
                        // This is a directory, check if it has a JSON file with the same name
                        // Check both exact match and lowercase match for flexibility
                        val exactFileName = "$item.json"
                        val lowerFileName = "${item.lowercase()}.json"
                        val jsonFileName = when {
                            subItems.contains(exactFileName) -> exactFileName
                            subItems.contains(lowerFileName) -> lowerFileName
                            else -> null
                        }

                        if (jsonFileName != null) {
                            // OPTIMIZATION: Don't load vocabulary during scan
                            // Count will be loaded lazily when needed
                            songs.add(
                                SongInfo(
                                    directoryName = item,
                                    displayTitle = formatSongTitle(item),
                                    artist = "",
                                    vocabularyCount = -1 // Lazy load count
                                )
                            )
                            android.util.Log.d("SongRepository", "Found song directory: $item")
                        }
                    }
                } catch (e: Exception) {
                    // Not a directory or error accessing it, skip
                    continue
                }
            }

            availableSongs = songs
            android.util.Log.d("SongRepository", "Total songs found: ${songs.size}")

        } catch (e: Exception) {
            android.util.Log.e("SongRepository", "Error scanning songs", e)
            availableSongs = emptyList()
        }
    }

    /**
     * Format directory name to display title
     * Example: "pretender" -> "Pretender"
     */
    private fun formatSongTitle(directoryName: String): String {
        return directoryName.replaceFirstChar { it.uppercase() }
    }

    /**
     * Load vocabulary for a specific song
     * Priority: 1) Internal storage (edited), 2) Assets (original)
     */
    private fun loadSongVocabulary(songDirectory: String): List<Song> {
        // Check cache first
        songDataCache[songDirectory]?.let { return it }

        try {
            val gson = Gson()
            val songListType = object : TypeToken<List<Map<String, String>>>() {}.type

            // Try internal storage first (edited files)
            val internalFile = java.io.File(context.filesDir, "$songDirectory.json")

            val vocabularyListAsMap: List<Map<String, String>> = if (internalFile.exists()) {
                android.util.Log.d("SongRepository", "Loading edited file from internal storage: $songDirectory")
                internalFile.inputStream().use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        gson.fromJson(reader, songListType)
                    }
                }
            } else {
                // Fall back to assets
                val jsonFileName = try {
                    val exactFileName = "$songDirectory.json"
                    context.assets.open("$songDirectory/$exactFileName").close()
                    exactFileName
                } catch (e: Exception) {
                    try {
                        val lowerFileName = "${songDirectory.lowercase()}.json"
                        context.assets.open("$songDirectory/$lowerFileName").close()
                        lowerFileName
                    } catch (e: Exception) {
                        "$songDirectory.json" // Fallback to exact match
                    }
                }
                val jsonPath = "$songDirectory/$jsonFileName"
                context.assets.open(jsonPath).use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        gson.fromJson(reader, songListType)
                    }
                }
            }

            val vocabularyList = vocabularyListAsMap
                .filter { map ->
                    // Filter out items that don't have required fields (like note-only items)
                    map.containsKey("kanji") && map.containsKey("hiragana") && map.containsKey("meaning")
                }
                .mapIndexed { index, map ->
                    Song(
                        id = index,
                        kanji = map["kanji"] ?: "",
                        meaning = map["meaning"] ?: "",
                        hiragana = map["hiragana"] ?: "",
                        songTitle = songDirectory,
                        time = map["time"],
                        endTime = map["endTime"]  // endTime 지원
                    )
                }

            // Cache the data
            songDataCache[songDirectory] = vocabularyList

            android.util.Log.d(
                "SongRepository",
                "Loaded ${vocabularyList.size} vocabulary from $songDirectory"
            )

            return vocabularyList

        } catch (e: Exception) {
            android.util.Log.e("SongRepository", "Error loading song: $songDirectory", e)
            return emptyList()
        }
    }

    /**
     * Get list of all available songs
     * Lazily loads vocabulary count if not yet loaded
     */
    fun getAvailableSongs(): List<SongInfo> {
        val songs = availableSongs ?: return emptyList()

        // Update vocabulary counts lazily for songs that haven't been loaded yet
        return songs.map { song ->
            if (song.vocabularyCount < 0) {
                // Load vocabulary to get count (will be cached)
                val vocabList = loadSongVocabulary(song.directoryName)
                song.copy(vocabularyCount = vocabList.size)
            } else {
                song
            }
        }.also { updatedSongs ->
            // Update the cache with the counts
            availableSongs = updatedSongs
        }
    }

    /**
     * Get vocabulary for a specific song
     */
    fun getSongVocabulary(songDirectory: String): List<Song> {
        return loadSongVocabulary(songDirectory)
    }

    /**
     * Get all pretender vocabulary (legacy compatibility)
     */
    fun getPretenderVocabulary(): List<Song> {
        return loadSongVocabulary("pretender")
    }

    /**
     * Clear cache for a specific song (for reload after editing)
     */
    fun clearCache(songDirectory: String) {
        songDataCache.remove(songDirectory)
        android.util.Log.d("SongRepository", "Cache cleared for: $songDirectory")
    }
}
