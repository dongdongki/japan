package com.example.myapplication.ui

import com.example.myapplication.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for song quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
@AndroidEntryPoint
class SongQuizModeFragment : BaseQuizModeFragment() {

    override val titleText = "노래 퀴즈 모드"
    override val option1Text = "뜻 보고 글자 맞추기"
    override val option2Text = "글자 보고 뜻 맞추기"

    override val navActionToQuiz = R.id.action_song_quiz_mode_to_quiz
    override val navActionToWritingTest = R.id.action_song_quiz_mode_to_writing_test

    override fun onOption1Click() {
        // 뜻 보고 글자 맞추기 (객관식, reverse mode)
        viewModel.startSongQuiz(mode = "reverse")
    }

    override fun onOption2Click() {
        // 글자 보고 뜻 맞추기 (객관식, normal mode)
        viewModel.startSongQuiz(mode = "meaning")
    }

    override fun onWritingTestClick() {
        // 쓰기 시험 (뜻 보고 손으로 쓰기)
        viewModel.quizType.value = "song"
    }
}
