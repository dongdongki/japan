package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R

/**
 * Fragment for daily word quiz mode selection
 * Uses BaseQuizModeFragment to eliminate code duplication
 */
class DailyWordQuizModeFragment : BaseQuizModeFragment() {

    override val titleText = "일일단어 퀴즈 모드 선택"
    override val option1Text = "단어 퀴즈"
    override val option2Text = "듣기 퀴즈"

    override val navActionToQuiz = R.id.action_daily_word_quiz_mode_to_quiz
    override val navActionToWritingTest = -1 // 쓰기 시험 없음

    private var selectedDays: Set<Int> = emptySet()
    private var isWeakWordMode: Boolean = false
    private var weakWordIds: Set<Int> = emptySet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Get arguments before calling super
        selectedDays = arguments?.getIntArray("selectedDays")?.toSet() ?: emptySet()
        isWeakWordMode = arguments?.getBoolean("isWeakWordMode", false) ?: false

        if (isWeakWordMode) {
            weakWordIds = arguments?.getIntArray("weakWordIds")?.toSet() ?: emptySet()
        }

        super.onViewCreated(view, savedInstanceState)

        // 쓰기 시험 버튼 숨기기 (일일단어는 쓰기 시험이 없음)
        view.findViewById<View>(R.id.btn_writing_test)?.visibility = View.GONE
    }

    override fun onOption1Click() {
        // 단어 퀴즈 (한글 → 일본어)
        if (isWeakWordMode) {
            viewModel.startWeakDailyWordQuiz(weakWordIds)
        } else {
            viewModel.startDailyWordQuiz(selectedDays)
        }
    }

    override fun onOption2Click() {
        // 듣기 퀴즈 (소리 → 일본어)
        if (isWeakWordMode) {
            viewModel.startWeakDailyWordListeningQuiz(weakWordIds)
        } else {
            viewModel.startDailyWordListeningQuiz(selectedDays)
        }
    }

    override fun onWritingTestClick() {
        // 일일단어는 쓰기 시험이 없으므로 아무것도 하지 않음
    }
}
