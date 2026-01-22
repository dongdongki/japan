package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for weak word quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizWeakWordModeFragment : BaseQuizModeFragment() {

    override val titleText = "선택 단어 퀴즈"
    override val option1Text = "뜻 보고 글자 맞추기"
    override val option2Text = "글자 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_quiz_weak_word_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_weak_word_mode_to_writing_test

    override fun onOption1Click() {
        // Meaning to word (multiple choice, reverse mode)
        viewModel.startWordQuiz(quizMode = "reverse", onlyWeakWords = true, isMultiple = true)
    }

    override fun onOption2Click() {
        // Word to meaning (subjective, normal mode)
        viewModel.startWordQuiz(quizMode = "meaning", onlyWeakWords = true, isMultiple = false)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "weak_words"
    }
}
