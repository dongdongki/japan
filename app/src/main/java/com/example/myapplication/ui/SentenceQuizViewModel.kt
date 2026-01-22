package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Sentence
import com.example.myapplication.repository.PreferencesRepository
import com.example.myapplication.repository.SentenceRepository
import com.example.myapplication.util.MultipleChoiceGenerator

/**
 * Helper class for managing Sentence quiz-specific logic
 */
class SentenceQuizViewModel(
    private val sentenceRepository: SentenceRepository,
    private val preferencesRepository: PreferencesRepository
) {

    // Sentence quiz settings
    val quizMode = MutableLiveData("meaning")
    val isMultipleChoice = MutableLiveData(false)
    val multipleChoices = MutableLiveData<List<String>>(emptyList())

    private var quizList: List<Sentence> = emptyList()

    fun getSentenceList(): List<Sentence> {
        return sentenceRepository.getSentences()
    }

    /**
     * Generate sentence quiz list based on settings
     */
    fun generateSentenceQuizList(onlyWeakSentences: Boolean = false): List<Sentence> {
        // Get all sentences from repository
        val allSentences = sentenceRepository.getSentences()

        val list = if (onlyWeakSentences) {
            allSentences.filter { preferencesRepository.isWeakWord(it.id) }
        } else {
            allSentences
        }

        // Store quiz list for multiple choice generation
        quizList = list

        android.util.Log.d("SentenceQuizViewModel", "Starting quiz with ${list.size} sentences")
        return list
    }

    /**
     * Generate multiple choice options for the current problem
     * Uses common MultipleChoiceGenerator utility
     */
    fun generateMultipleChoices(problem: Sentence) {
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
        android.util.Log.d("SentenceQuizViewModel", "Generated choices (${if (isReverse) "reverse" else "normal"}): $choices, correct: $correctAnswer")
    }

    /**
     * Get correct answer for a sentence problem
     */
    fun getCorrectAnswer(problem: Sentence): String {
        return if (quizMode.value == "reverse") {
            problem.kanji
        } else {
            problem.meaning
        }
    }
}
