package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for word quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizWordModeFragment : BaseQuizModeFragment() {

    override val titleText = "단어 퀴즈 모드 선택"
    override val option1Text = "뜻 보고 단어 맞추기"
    override val option2Text = "단어 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_quiz_word_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_word_mode_to_writing_test

    override fun onOption1Click() {
        // Meaning to word (multiple choice, reverse mode)
        viewModel.startWordQuiz(quizMode = "reverse", onlyWeakWords = false, isMultiple = true)
    }

    override fun onOption2Click() {
        // Word to meaning (subjective, normal mode)
        viewModel.startWordQuiz(quizMode = "meaning", onlyWeakWords = false, isMultiple = false)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "word"
    }
}
