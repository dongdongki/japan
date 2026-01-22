package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Word
import com.example.myapplication.repository.PreferencesRepository
import com.example.myapplication.service.TtsService

/**
 * Helper class for managing quiz state and common functionality
 */
class QuizStateViewModel(
    private val ttsService: TtsService,
    private val preferencesRepository: PreferencesRepository
) {

    // Quiz state
    val currentProblem = MutableLiveData<Any?>(null)
    val problemCount = MutableLiveData(0)
    val remainingProblems = MutableLiveData(0)
    val sessionCorrect = MutableLiveData(0)
    val sessionTotal = MutableLiveData(0)
    val wrongAnswers = MutableLiveData<MutableList<Any>>(mutableListOf())

    // Weak words
    val weakWords = MutableLiveData<Set<Int>>(emptySet())

    // Drawing tools
    val penWidth = MutableLiveData(12f)
    val eraserWidth = MutableLiveData(40f)

    // Internal quiz list
    private var currentQuizList: MutableList<Any> = mutableListOf()

    init {
        ttsService.acquire()
        loadWeakWords()
        loadPenWidth()
        loadEraserWidth()
    }

    private fun loadPenWidth() {
        penWidth.value = preferencesRepository.getPenWidth()
    }

    fun savePenWidth(width: Float) {
        penWidth.value = width
        preferencesRepository.savePenWidth(width)
    }

    private fun loadEraserWidth() {
        eraserWidth.value = preferencesRepository.getEraserWidth()
    }

    fun saveEraserWidth(width: Float) {
        eraserWidth.value = width
        preferencesRepository.saveEraserWidth(width)
    }

    private fun loadWeakWords() {
        weakWords.value = preferencesRepository.getWeakWords()
    }

    fun toggleWeakWord(word: Word) {
        preferencesRepository.toggleWeakWord(word.id)
        weakWords.value = preferencesRepository.getWeakWords()
    }

    fun toggleWeakWord(wordId: Int) {
        preferencesRepository.toggleWeakWord(wordId)
        weakWords.value = preferencesRepository.getWeakWords()
    }

    fun isWeakWord(word: Word): Boolean {
        return preferencesRepository.isWeakWord(word.id)
    }

    fun isWeakWord(wordId: Int): Boolean {
        return preferencesRepository.isWeakWord(wordId)
    }

    fun clearAllWeakWords() {
        preferencesRepository.clearAllWeakWords()
        weakWords.value = emptySet()
    }

    fun clearAllWeakSentences() {
        preferencesRepository.clearAllWeakSentences()
        weakWords.value = preferencesRepository.getWeakWords()
    }

    fun speak(text: String) {
        ttsService.speak(text)
    }

    /**
     * Initialize quiz with a shuffled list
     */
    fun initializeQuiz(list: List<Any>) {
        currentQuizList = list.shuffled().toMutableList()
        sessionCorrect.value = 0
        sessionTotal.value = 0
        problemCount.value = 0
        wrongAnswers.value = mutableListOf()
    }

    /**
     * Move to next problem in the quiz
     */
    fun nextProblem() {
        if (currentQuizList.isEmpty()) {
            // Quiz cycle completed
            currentProblem.value = null
            remainingProblems.value = 0
            return
        }
        currentProblem.value = currentQuizList.removeFirst()
        problemCount.value = (problemCount.value ?: 0) + 1
        remainingProblems.value = currentQuizList.size
    }

    /**
     * Check answer and update statistics
     */
    fun checkAnswer(userAnswer: String, correctAnswer: String, isMultipleChoice: Boolean): Boolean {
        val problem = currentProblem.value ?: return false

        // For multiple choice, check exact match. For text input, check comma-separated alternatives
        val isCorrect = if (isMultipleChoice) {
            // Multiple choice: exact match only
            userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = false)
        } else {
            // Text input: check all comma-separated alternatives
            correctAnswer.split(",").map { it.trim() }.any { userAnswer.trim().equals(it, ignoreCase = true) }
        }

        android.util.Log.d("QuizStateViewModel", "checkAnswer: user='$userAnswer', correct='$correctAnswer', isMultipleChoice=$isMultipleChoice, result=$isCorrect")

        if (isCorrect) {
            sessionCorrect.value = (sessionCorrect.value ?: 0) + 1
        } else {
            // Add to wrong answers list
            val currentWrong = wrongAnswers.value ?: mutableListOf()
            currentWrong.add(problem)
            wrongAnswers.value = currentWrong
        }
        sessionTotal.value = (sessionTotal.value ?: 0) + 1
        return isCorrect
    }

    fun shutdown() {
        ttsService.release()
    }
}
