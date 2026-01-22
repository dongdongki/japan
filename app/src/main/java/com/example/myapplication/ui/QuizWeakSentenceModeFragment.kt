package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for weak sentence quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizWeakSentenceModeFragment : BaseQuizModeFragment() {

    override val titleText = "선택 문장 퀴즈"
    override val option1Text = "뜻 → 문장"
    override val option2Text = "문장 → 뜻"

    override val navActionToQuiz = R.id.action_quiz_weak_sentence_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_weak_sentence_mode_to_writing_test

    override fun onOption1Click() {
        // Meaning to sentence (multiple choice, reverse mode)
        viewModel.startSentenceQuiz(mode = "reverse", isMultiple = true, useWeakSentences = true)
    }

    override fun onOption2Click() {
        // Sentence to meaning (subjective, normal mode)
        viewModel.startSentenceQuiz(mode = "meaning", isMultiple = false, useWeakSentences = true)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "weak_sentences"
    }
}
