package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.KanaCharacter
import com.example.myapplication.repository.KanaRepository
import com.example.myapplication.repository.PreferencesRepository

/**
 * Helper class for managing Kana quiz-specific logic
 */
class KanaQuizViewModel(
    private val kanaRepository: KanaRepository,
    private val preferencesRepository: PreferencesRepository
) {

    // Kana quiz settings
    val kanaType = MutableLiveData("hiragana")
    val rangeType = MutableLiveData("all")
    val selectedRows = MutableLiveData<List<String>>(emptyList())

    // Selected kana for custom quiz
    val selectedKanaList = MutableLiveData<MutableList<KanaCharacter>>(mutableListOf())

    init {
        // Load saved selected kana from SharedPreferences
        loadSelectedKana()
    }

    /**
     * Load selected kana from SharedPreferences
     */
    private fun loadSelectedKana() {
        val savedKanaStrings = preferencesRepository.getSelectedKana()
        if (savedKanaStrings.isNotEmpty()) {
            val allKana = kanaRepository.getAllKanaList()
            val selectedList = allKana.filter { savedKanaStrings.contains(it.kana) }.toMutableList()
            selectedKanaList.value = selectedList
        }
    }

    fun getAllKanaList(): List<KanaCharacter> {
        return kanaRepository.getAllKanaList()
    }

    fun getGroupedKanaList(): Map<String, List<KanaCharacter>> {
        return kanaRepository.getGroupedKanaList()
    }

    /**
     * Generate kana quiz list based on current settings
     */
    fun generateKanaQuizList(): List<KanaCharacter> {
        val tempList = mutableListOf<KanaCharacter>()

        when (kanaType.value) {
            "hiragana" -> {
                val characters = if (rangeType.value == "row") {
                    kanaRepository.getSelectedCharacters("hiragana", selectedRows.value ?: emptyList())
                } else {
                    kanaRepository.getSelectedCharacters("hiragana", emptyList()).takeIf { it.isNotEmpty() }
                        ?: kanaRepository.getAllKanaList().filter { it.kana.isNotEmpty() && it.kana[0] in '\u3040'..'\u309F' }
                }
                tempList.addAll(characters)
            }
            "katakana" -> {
                val characters = if (rangeType.value == "row") {
                    kanaRepository.getSelectedCharacters("katakana", selectedRows.value ?: emptyList())
                } else {
                    kanaRepository.getSelectedCharacters("katakana", emptyList()).takeIf { it.isNotEmpty() }
                        ?: kanaRepository.getAllKanaList().filter { it.kana.isNotEmpty() && it.kana[0] in '\u30A0'..'\u30FF' }
                }
                tempList.addAll(characters)
            }
            "both", "mixed" -> {
                if (rangeType.value == "row") {
                    tempList.addAll(kanaRepository.getSelectedCharacters("hiragana", selectedRows.value ?: emptyList()))
                    tempList.addAll(kanaRepository.getSelectedCharacters("katakana", selectedRows.value ?: emptyList()))
                } else {
                    tempList.addAll(kanaRepository.getAllKanaList())
                }
            }
        }

        return tempList
    }

    /**
     * Get correct answer for a kana problem
     */
    fun getCorrectAnswer(problem: KanaCharacter): String {
        return problem.kor
    }

    /**
     * Toggle kana selection for custom quiz
     */
    fun toggleKanaSelection(kana: KanaCharacter) {
        val currentList = selectedKanaList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.kana == kana.kana }

        if (index >= 0) {
            currentList.removeAt(index)
        } else {
            currentList.add(kana)
        }

        selectedKanaList.value = currentList

        // Persist to SharedPreferences
        val kanaSet = currentList.map { it.kana }.toSet()
        preferencesRepository.saveSelectedKana(kanaSet)
    }

    /**
     * Check if a kana is selected
     */
    fun isKanaSelected(kana: KanaCharacter): Boolean {
        val currentList = selectedKanaList.value ?: return false
        return currentList.any { it.kana == kana.kana }
    }

    /**
     * Get selected kana list
     */
    fun getSelectedKanaList(): List<KanaCharacter> {
        return selectedKanaList.value ?: emptyList()
    }

    /**
     * Clear selected kana list
     */
    fun clearSelectedKana() {
        selectedKanaList.value = mutableListOf()

        // Clear from SharedPreferences
        preferencesRepository.saveSelectedKana(emptySet())
    }
}
