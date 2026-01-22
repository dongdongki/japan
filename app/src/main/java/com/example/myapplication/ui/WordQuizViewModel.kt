package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Word
import com.example.myapplication.repository.PreferencesRepository
import com.example.myapplication.repository.WordRepository
import com.example.myapplication.util.MultipleChoiceGenerator

/**
 * Helper class for managing Word quiz-specific logic
 * Handles verbs, particles, adjectives, adverbs, conjunctions
 */
class WordQuizViewModel(
    private val wordRepository: WordRepository,
    private val preferencesRepository: PreferencesRepository
) {

    // Word quiz settings
    val quizMode = MutableLiveData("meaning")
    val isMultipleChoice = MutableLiveData(false)
    val multipleChoices = MutableLiveData<List<String>>(emptyList())

    var quizList: List<Word> = emptyList()  // Changed from private to public for direct access
        internal set  // Only allow setting from same module

    /**
     * Generate word quiz list based on part of speech
     */
    fun generateWordQuizList(partOfSpeech: String): List<Word> {
        val list = when (partOfSpeech) {
            "verb" -> wordRepository.getWordsByPartOfSpeech("verb")
            "particle" -> wordRepository.getWordsByPartOfSpeech("particle")
            "adjective" -> wordRepository.getWordsByPartOfSpeech("adjective")
            "adverb" -> wordRepository.getWordsByPartOfSpeech("adverb")
            "conjunction" -> wordRepository.getWordsByPartOfSpeech("conjunction")
            else -> wordRepository.getAllWords()
        }

        // Store quiz list for multiple choice generation
        quizList = list

        android.util.Log.d("WordQuizViewModel", "Starting quiz with ${list.size} $partOfSpeech words")
        return list
    }

    /**
     * Generate multiple choice options for the current problem
     * Uses common MultipleChoiceGenerator utility
     */
    fun generateMultipleChoices(problem: Word) {
        val isReverse = quizMode.value == "reverse"

        val choices = MultipleChoiceGenerator.generateQuizChoices(
            problem = problem,
            allItems = quizList,
            isReverse = isReverse,
            kanjiExtractor = { it.kanji },
            meaningExtractor = { it.meaning }
        )

        multipleChoices.value = choices

        val correctAnswer = if (isReverse) problem.kanji else problem.meaning
        android.util.Log.d("WordQuizViewModel", "Generated choices (${if (isReverse) "reverse" else "normal"}): $choices, correct: $correctAnswer")
    }

    /**
     * Get correct answer for a word problem
     */
    fun getCorrectAnswer(problem: Word): String {
        return if (quizMode.value == "reverse") {
            problem.kanji
        } else {
            problem.meaning
        }
    }
}
