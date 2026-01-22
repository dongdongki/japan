package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.DailyWord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class DailyWordRepository(private val context: Context) {

    private var allWords: List<DailyWord>? = null

    // Reusable Gson instance (avoid creating new instances)
    private val gson = Gson()
    private val wordListType = object : TypeToken<List<DailyWord>>() {}.type

    private fun loadWords(): List<DailyWord> {
        if (allWords == null) {
            try {
                context.assets.open("japanese_words_data.json").use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        allWords = gson.fromJson(reader, wordListType)
                    }
                }
            } catch (e: Exception) {
                allWords = emptyList()
            }
        }
        return allWords ?: emptyList()
    }

    /**
     * Get total number of days (20 words per day)
     */
    fun getTotalDays(): Int {
        val words = loadWords()
        return (words.size + 19) / 20  // Ceiling division
    }

    /**
     * Get words for a specific day (1-indexed)
     */
    fun getWordsForDay(day: Int): List<DailyWord> {
        val words = loadWords()
        val startIndex = (day - 1) * 20
        val endIndex = minOf(startIndex + 20, words.size)

        android.util.Log.d("DailyWordRepository", "getWordsForDay($day): 전체 단어 ${words.size}개, 인덱스 $startIndex~$endIndex")

        if (startIndex >= words.size) {
            android.util.Log.w("DailyWordRepository", "getWordsForDay($day): startIndex($startIndex) >= words.size(${words.size})")
            return emptyList()
        }

        val result = words.subList(startIndex, endIndex)
        if (result.isNotEmpty()) {
            android.util.Log.d("DailyWordRepository", "getWordsForDay($day): 반환 ${result.size}개 - ${result.take(3).map { it.word }.joinToString(", ")}")
        }

        return result
    }

    /**
     * Get all words
     */
    fun getAllWords(): List<DailyWord> {
        return loadWords()
    }
}
