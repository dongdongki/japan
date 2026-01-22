package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Song
import com.example.myapplication.repository.SongRepository
import com.example.myapplication.util.MultipleChoiceGenerator

/**
 * Helper class for managing Song quiz-specific logic
 */
class SongQuizViewModel(
    private val songRepository: SongRepository
) {

    // Song quiz settings
    val quizMode = MutableLiveData("meaning")
    val isMultipleChoice = MutableLiveData(true)
    val multipleChoices = MutableLiveData<List<String>>(emptyList())

    private var quizList: List<Song> = emptyList()

    /**
     * Get song vocabulary list for a specific song directory
     */
    fun getSongList(songDirectory: String = "pretender"): List<Song> {
        return songRepository.getSongVocabulary(songDirectory)
    }

    /**
     * Generate song quiz list for a specific song directory
     */
    fun generateSongQuizList(songDirectory: String = "pretender"): List<Song> {
        val list = songRepository.getSongVocabulary(songDirectory)
        quizList = list
        android.util.Log.d("SongQuizViewModel", "Starting song quiz with ${list.size} words from $songDirectory")
        return list
    }

    /**
     * Generate multiple choice options for the current problem
     * Uses common MultipleChoiceGenerator utility
     */
    fun generateMultipleChoices(problem: Song) {
        val isReverse = quizMode.value == "reverse"

        val choices = MultipleChoiceGenerator.generateQuizChoices(
            problem = problem,
            allItems = quizList,
            isReverse = isReverse,
            kanjiExtractor = { it.kanji },
            meaningExtractor = { it.meaning }
        )

        multipleChoices.value = choices
    }
}
