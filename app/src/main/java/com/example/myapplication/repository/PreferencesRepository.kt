package com.example.myapplication.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for managing user preferences (weak words, pen/eraser settings)
 */
class PreferencesRepository(context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val mainHandler = Handler(Looper.getMainLooper())
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Daily Word weak words용 SharedPreferences (별도 파일)
    private val dailyWordPrefs: SharedPreferences =
        context.getSharedPreferences(DAILY_WORD_PREFS_NAME, Context.MODE_PRIVATE)

    // LiveData for observing weak words changes
    private val _weakWordsLiveData = MutableLiveData<Set<Int>>()
    val weakWordsLiveData: LiveData<Set<Int>> = _weakWordsLiveData

    // LiveData for observing daily word weak words changes
    private val _weakDailyWordsLiveData = MutableLiveData<Set<Int>>()
    val weakDailyWordsLiveData: LiveData<Set<Int>> = _weakDailyWordsLiveData

    init {
        // Initialize LiveData with current weak words
        _weakWordsLiveData.value = getWeakWords()
        _weakDailyWordsLiveData.value = getWeakDailyWords()
    }

    companion object {
        private const val KEY_WEAK_WORDS = "weak_words"
        private const val KEY_SELECTED_KANA = "selected_kana"
        private const val KEY_PEN_WIDTH = "pen_width"
        private const val KEY_ERASER_WIDTH = "eraser_width"
        private const val KEY_SHOW_MEANING = "show_meaning"
        private const val DEFAULT_PEN_WIDTH = 12f
        private const val DEFAULT_ERASER_WIDTH = 40f

        // Daily Word용 상수
        private const val DAILY_WORD_PREFS_NAME = "daily_word_prefs"
        private const val KEY_WEAK_DAILY_WORDS = "weak_daily_words"
    }

    /**
     * Get weak words (word IDs that user got wrong)
     */
    fun getWeakWords(): Set<Int> {
        val stringSet = sharedPrefs.getStringSet(KEY_WEAK_WORDS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    /**
     * Save weak words (async)
     */
    fun saveWeakWords(wordIds: Set<Int>) {
        // Update LiveData immediately on main thread for UI responsiveness
        mainHandler.post {
            _weakWordsLiveData.value = wordIds
        }

        // Perform SharedPreferences write on IO thread
        repositoryScope.launch {
            val stringSet = wordIds.map { it.toString() }.toSet()
            sharedPrefs.edit()
                .putStringSet(KEY_WEAK_WORDS, stringSet)
                .apply()
        }
    }

    /**
     * Add a word to weak words
     */
    fun addWeakWord(wordId: Int) {
        val currentWeakWords = getWeakWords().toMutableSet()
        currentWeakWords.add(wordId)
        saveWeakWords(currentWeakWords)
    }

    /**
     * Remove a word from weak words
     */
    fun removeWeakWord(wordId: Int) {
        val currentWeakWords = getWeakWords().toMutableSet()
        currentWeakWords.remove(wordId)
        saveWeakWords(currentWeakWords)
    }

    /**
     * Toggle weak word status
     */
    fun toggleWeakWord(wordId: Int) {
        val currentWeakWords = getWeakWords().toMutableSet()
        if (currentWeakWords.contains(wordId)) {
            currentWeakWords.remove(wordId)
        } else {
            currentWeakWords.add(wordId)
        }
        saveWeakWords(currentWeakWords)
    }

    /**
     * Check if a word is marked as weak
     */
    fun isWeakWord(wordId: Int): Boolean {
        return getWeakWords().contains(wordId)
    }

    /**
     * Clear all weak words (IDs 0-999 and 10000+, excluding sentences 20000+)
     */
    fun clearAllWeakWords() {
        val currentWeakWords = getWeakWords()
        // Keep only sentence IDs (20000+)
        val sentencesOnly = currentWeakWords.filter { it >= 20000 }.toSet()
        saveWeakWords(sentencesOnly)
    }

    /**
     * Clear all weak sentences (IDs 20000+)
     */
    fun clearAllWeakSentences() {
        val currentWeakWords = getWeakWords()
        // Keep only word and song IDs (0-999 and 10000+)
        val wordsOnly = currentWeakWords.filter { it < 20000 }.toSet()
        saveWeakWords(wordsOnly)
    }

    /**
     * Get pen width setting
     */
    fun getPenWidth(): Float {
        return sharedPrefs.getFloat(KEY_PEN_WIDTH, DEFAULT_PEN_WIDTH)
    }

    /**
     * Save pen width setting (async)
     */
    fun savePenWidth(width: Float) {
        repositoryScope.launch {
            sharedPrefs.edit()
                .putFloat(KEY_PEN_WIDTH, width)
                .apply()
        }
    }

    /**
     * Get eraser width setting
     */
    fun getEraserWidth(): Float {
        return sharedPrefs.getFloat(KEY_ERASER_WIDTH, DEFAULT_ERASER_WIDTH)
    }

    /**
     * Save eraser width setting (async)
     */
    fun saveEraserWidth(width: Float) {
        repositoryScope.launch {
            sharedPrefs.edit()
                .putFloat(KEY_ERASER_WIDTH, width)
                .apply()
        }
    }

    /**
     * Get selected kana (kana characters saved as strings)
     */
    fun getSelectedKana(): Set<String> {
        return sharedPrefs.getStringSet(KEY_SELECTED_KANA, emptySet()) ?: emptySet()
    }

    /**
     * Save selected kana (async)
     */
    fun saveSelectedKana(kanaSet: Set<String>) {
        repositoryScope.launch {
            sharedPrefs.edit()
                .putStringSet(KEY_SELECTED_KANA, kanaSet)
                .apply()
        }
    }

    /**
     * Get show meaning preference (default: true)
     */
    fun getShowMeaning(): Boolean {
        return sharedPrefs.getBoolean(KEY_SHOW_MEANING, true)
    }

    /**
     * Save show meaning preference (async)
     */
    fun saveShowMeaning(show: Boolean) {
        repositoryScope.launch {
            sharedPrefs.edit()
                .putBoolean(KEY_SHOW_MEANING, show)
                .apply()
        }
    }

    // ==================== Daily Word Weak Words ====================

    /**
     * Get weak daily words (daily word IDs that user marked as weak)
     */
    fun getWeakDailyWords(): Set<Int> {
        val stringSet = dailyWordPrefs.getStringSet(KEY_WEAK_DAILY_WORDS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    /**
     * Save weak daily words
     */
    fun saveWeakDailyWords(wordIds: Set<Int>) {
        with(dailyWordPrefs.edit()) {
            val stringSet = wordIds.map { it.toString() }.toSet()
            putStringSet(KEY_WEAK_DAILY_WORDS, stringSet)
            apply()
        }

        // Update LiveData on main thread
        mainHandler.post {
            _weakDailyWordsLiveData.value = wordIds
        }
    }

    /**
     * Add a daily word to weak words
     */
    fun addWeakDailyWord(wordId: Int) {
        val currentWeakWords = getWeakDailyWords().toMutableSet()
        currentWeakWords.add(wordId)
        saveWeakDailyWords(currentWeakWords)
    }

    /**
     * Remove a daily word from weak words
     */
    fun removeWeakDailyWord(wordId: Int) {
        val currentWeakWords = getWeakDailyWords().toMutableSet()
        currentWeakWords.remove(wordId)
        saveWeakDailyWords(currentWeakWords)
    }

    /**
     * Toggle weak daily word status
     */
    fun toggleWeakDailyWord(wordId: Int) {
        val currentWeakWords = getWeakDailyWords().toMutableSet()
        if (currentWeakWords.contains(wordId)) {
            currentWeakWords.remove(wordId)
        } else {
            currentWeakWords.add(wordId)
        }
        saveWeakDailyWords(currentWeakWords)
    }

    /**
     * Check if a daily word is marked as weak
     */
    fun isWeakDailyWord(wordId: Int): Boolean {
        return getWeakDailyWords().contains(wordId)
    }

    /**
     * Clear all weak daily words
     */
    fun clearAllWeakDailyWords() {
        saveWeakDailyWords(emptySet())
    }
}
