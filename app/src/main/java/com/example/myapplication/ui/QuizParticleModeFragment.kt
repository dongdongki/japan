package com.example.myapplication.ui

import com.example.myapplication.R

/**
 * Fragment for particle quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class QuizParticleModeFragment : BaseQuizModeFragment() {

    override val titleText = "조사 퀴즈 모드"
    override val option1Text = "뜻 보고 조사 맞추기"
    override val option2Text = "조사 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_quiz_particle_mode_to_quiz
    override val navActionToWritingTest = R.id.action_quiz_particle_mode_to_writing_test

    override fun onOption1Click() {
        // Meaning to particle (multiple choice, reverse mode)
        viewModel.startWordQuizByType(type = "particle", mode = "reverse", isMultiple = true)
    }

    override fun onOption2Click() {
        // Particle to meaning (subjective, normal mode)
        viewModel.startWordQuizByType(type = "particle", mode = "meaning", isMultiple = false)
    }

    override fun onWritingTestClick() {
        viewModel.quizType.value = "particle"
    }
}
