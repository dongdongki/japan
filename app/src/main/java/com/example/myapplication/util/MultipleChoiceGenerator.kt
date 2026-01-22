package com.example.myapplication.util

/**
 * Utility class for generating multiple choice options
 * Eliminates code duplication across quiz ViewModels
 */
object MultipleChoiceGenerator {

    /**
     * Generate 4 multiple choice options from a list
     *
     * @param correctAnswer The correct answer to include
     * @param allItems All available items to choose wrong answers from
     * @param answerExtractor Function to extract the answer string from an item
     * @return List of 4 shuffled choices including the correct answer
     */
    fun <T> generateChoices(
        correctAnswer: String,
        allItems: List<T>,
        answerExtractor: (T) -> String
    ): List<String> {
        // Get up to 3 random wrong answers
        val wrongAnswers = allItems
            .filter { answerExtractor(it) != correctAnswer }
            .shuffled()
            .take(3)
            .map { answerExtractor(it) }

        // Always ensure correct answer is included, then fill with wrong answers
        val choices = mutableListOf(correctAnswer)
        choices.addAll(wrongAnswers)

        // If we don't have enough choices (less than 4), pad with remaining wrong answers
        if (choices.size < 4) {
            val additionalWrong = allItems
                .filter {
                    val answer = answerExtractor(it)
                    answer != correctAnswer && !choices.contains(answer)
                }
                .shuffled()
                .take(4 - choices.size)
                .map { answerExtractor(it) }
            choices.addAll(additionalWrong)
        }

        // Shuffle all choices and return
        return choices.shuffled()
    }

    /**
     * Generate multiple choice options for a quiz problem
     * Supports both normal and reverse modes
     *
     * @param problem The current problem item
     * @param allItems All available items for generating wrong answers
     * @param isReverse Whether in reverse mode (meaning -> kanji) or normal (kanji -> meaning)
     * @param kanjiExtractor Function to extract kanji from item
     * @param meaningExtractor Function to extract meaning from item
     * @return List of 4 shuffled choices
     */
    fun <T> generateQuizChoices(
        problem: T,
        allItems: List<T>,
        isReverse: Boolean,
        kanjiExtractor: (T) -> String,
        meaningExtractor: (T) -> String
    ): List<String> {
        val correctAnswer = if (isReverse) kanjiExtractor(problem) else meaningExtractor(problem)
        val answerExtractor: (T) -> String = if (isReverse) kanjiExtractor else meaningExtractor

        return generateChoices(correctAnswer, allItems, answerExtractor)
    }
}
