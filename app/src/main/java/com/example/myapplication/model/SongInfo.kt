package com.example.myapplication.model

/**
 * Metadata about a song
 * Used for displaying song list and loading song vocabulary
 */
data class SongInfo(
    val directoryName: String,      // Directory name in assets (e.g., "pretender")
    val displayTitle: String,        // Display title (e.g., "Pretender")
    val artist: String = "",         // Artist name
    val vocabularyCount: Int = 0     // Number of vocabulary items
) {
    /**
     * Get the path to the vocabulary JSON file
     */
    fun getVocabularyPath(): String = "$directoryName/$directoryName.json"
}
