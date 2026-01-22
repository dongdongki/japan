package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for verb quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizVerbModeFragment : BaseQuizModeFragment() {

    override val titleText = "동사 퀴즈 모드"
    override val option1Text = "뜻 보고 동사 맞추기"
    override val option2Text = "동사 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_quiz_verb_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_verb_mode_to_writing_test

    override fun onOption1Click() {
        // Meaning to verb (multiple choice, reverse mode)
        viewModel.startWordQuizByType(type = "verb", mode = "reverse", isMultiple = true)
    }

    override fun onOption2Click() {
        // Verb to meaning (subjective, normal mode)
        viewModel.startWordQuizByType(type = "verb", mode = "meaning", isMultiple = false)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "verb"
    }
}
