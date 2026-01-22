package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for sentence quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizSentenceModeFragment : BaseQuizModeFragment() {

    override val titleText = "문장 퀴즈 모드 선택"
    override val option1Text = "뜻 보고 문장 맞추기"
    override val option2Text = "문장 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_quiz_sentence_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_sentence_mode_to_writing_test

    override fun onOption1Click() {
        // Get whether we're showing only weak sentences
        val onlyWeakSentences = arguments?.getBoolean("showOnlyWeakSentences") ?: false

        // Meaning to sentence (multiple choice, reverse mode)
        viewModel.startSentenceQuiz(mode = "reverse", isMultiple = true, useWeakSentences = onlyWeakSentences)
    }

    override fun onOption2Click() {
        // Get whether we're showing only weak sentences
        val onlyWeakSentences = arguments?.getBoolean("showOnlyWeakSentences") ?: false

        // Sentence to meaning (subjective, normal mode)
        viewModel.startSentenceQuiz(mode = "meaning", isMultiple = false, useWeakSentences = onlyWeakSentences)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "sentence"
    }
}
