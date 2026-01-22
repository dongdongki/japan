package com.example.myapplication.ui

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.databinding.FragmentQuizBinding

/**
 * QuizFragment의 UI 업데이트 및 LiveData 관찰 로직을 분리한 헬퍼 클래스
 * Fragment 코드를 간소화하고 관심사 분리를 위해 사용
 */
class QuizUiHelper(
    private val binding: FragmentQuizBinding,
    private val viewModel: QuizViewModel,
    private val lifecycleOwner: LifecycleOwner
) {
    // Track last values to prevent redundant observer updates
    private var lastQuizMode: String? = null
    private var lastIsMultipleChoice: Boolean? = null

    // Flag to prevent re-generating choices when observer fires
    var isGeneratingChoices = false

    // Choice buttons list for unified handling
    private val choiceButtons by lazy {
        listOf(binding.btnChoice1, binding.btnChoice2, binding.btnChoice3, binding.btnChoice4)
    }

    /**
     * 모든 LiveData 옵저버 설정
     */
    fun setupObservers(onProblemChanged: (Any?) -> Unit) {
        observeQuizMode()
        observePenAndEraserWidth()
        observeMultipleChoice()
        observeMultipleChoices()
        observeSongViewModel()
        observeSentenceViewModel()
        observeWordViewModel()
        observeCurrentProblem(onProblemChanged)
    }

    private fun observeQuizMode() {
        viewModel.quizMode.observe(lifecycleOwner) { mode ->
            if (mode == lastQuizMode) return@observe
            lastQuizMode = mode
            binding.writingPracticeContainer.isVisible = mode == "listening"
        }
    }

    private fun observePenAndEraserWidth() {
        viewModel.penWidth.observe(lifecycleOwner) { width ->
            binding.writingView.setPenWidth(width)
        }

        viewModel.eraserWidth.observe(lifecycleOwner) { width ->
            binding.writingView.setEraserWidth(width)
        }
    }

    private fun observeMultipleChoice() {
        viewModel.isMultipleChoice.observe(lifecycleOwner) { isMultipleChoice ->
            if (isGeneratingChoices || isMultipleChoice == lastIsMultipleChoice) return@observe
            lastIsMultipleChoice = isMultipleChoice

            android.util.Log.d("QuizUiHelper", "Observer: isMultipleChoice changed to: $isMultipleChoice, quizType=${viewModel.quizType.value}")
            val quizType = viewModel.quizType.value
            if (quizType == "kana") {
                binding.multipleChoiceContainer.isVisible = false
                binding.etAnswer.isVisible = true
            } else {
                binding.multipleChoiceContainer.isVisible = isMultipleChoice
                binding.etAnswer.isVisible = !isMultipleChoice
            }
        }
    }

    private fun observeMultipleChoices() {
        viewModel.multipleChoices.observe(lifecycleOwner) { choices ->
            if (isGeneratingChoices) return@observe

            val quizType = viewModel.quizType.value
            if (quizType != "kana" && choices.size >= 4) {
                setChoiceButtonTexts(choices)
            }
        }
    }

    private fun observeSongViewModel() {
        viewModel.songViewModel.isMultipleChoice.observe(lifecycleOwner) { isMultipleChoice ->
            if (isGeneratingChoices) return@observe

            if (viewModel.quizType.value == "song") {
                binding.multipleChoiceContainer.isVisible = isMultipleChoice
                binding.etAnswer.isVisible = !isMultipleChoice
            }
        }

        viewModel.songViewModel.multipleChoices.observe(lifecycleOwner) { choices ->
            if (isGeneratingChoices) return@observe

            if (viewModel.quizType.value == "song" && choices.size >= 4) {
                setChoiceButtonTexts(choices)
            }
        }
    }

    private fun observeSentenceViewModel() {
        viewModel.sentenceViewModel.isMultipleChoice.observe(lifecycleOwner) { isMultipleChoice ->
            if (isGeneratingChoices) return@observe

            val quizType = viewModel.quizType.value
            if (quizType == "sentence" || quizType == "weak_sentences") {
                binding.multipleChoiceContainer.isVisible = isMultipleChoice
                binding.etAnswer.isVisible = !isMultipleChoice
            }
        }

        viewModel.sentenceViewModel.multipleChoices.observe(lifecycleOwner) { choices ->
            if (isGeneratingChoices) return@observe

            val quizType = viewModel.quizType.value
            if ((quizType == "sentence" || quizType == "weak_sentences") && choices.size >= 4) {
                setChoiceButtonTexts(choices)
            }
        }
    }

    private fun observeWordViewModel() {
        viewModel.wordViewModel.isMultipleChoice.observe(lifecycleOwner) { isMultipleChoice ->
            if (isGeneratingChoices) return@observe

            val quizType = viewModel.quizType.value
            if (isWordTypeQuiz(quizType) || quizType == "weak_words") {
                binding.multipleChoiceContainer.isVisible = isMultipleChoice
                binding.etAnswer.isVisible = !isMultipleChoice
            }
        }

        viewModel.wordViewModel.multipleChoices.observe(lifecycleOwner) { choices ->
            if (isGeneratingChoices) return@observe

            val quizType = viewModel.quizType.value
            if ((isWordTypeQuiz(quizType) || quizType == "weak_words") && choices.size >= 4) {
                setChoiceButtonTexts(choices)
            }
        }
    }

    private fun observeCurrentProblem(onProblemChanged: (Any?) -> Unit) {
        viewModel.currentProblem.observe(lifecycleOwner) { problem ->
            onProblemChanged(problem)
        }
    }

    /**
     * 객관식 버튼에 텍스트 설정
     */
    fun setChoiceButtonTexts(choices: List<String>) {
        if (choices.size >= choiceButtons.size) {
            choiceButtons.forEachIndexed { index, button ->
                button.text = choices[index]
            }
        }
    }

    /**
     * 새로운 문제를 위한 UI 초기화
     */
    fun resetUIForNewProblem() {
        binding.tvResult.text = ""
        binding.etAnswer.text.clear()
        binding.etAnswer.isEnabled = true
        binding.btnAction.text = binding.root.context.getString(com.example.myapplication.R.string.quiz_confirm)

        binding.cbWeak.visibility = View.GONE

        val quizType = viewModel.quizType.value
        val isMultipleChoice = viewModel.isMultipleChoice.value ?: false

        if (quizType == "kana") {
            binding.etAnswer.isVisible = true
            binding.btnAction.isVisible = true
            binding.multipleChoiceContainer.isVisible = false
        } else {
            binding.etAnswer.isVisible = !isMultipleChoice
            binding.btnAction.isVisible = !isMultipleChoice
            binding.multipleChoiceContainer.isVisible = isMultipleChoice
        }

        // Re-enable multiple choice buttons
        setChoiceButtonsEnabled(true)

        // Clear writing view for the new problem
        if (binding.writingPracticeContainer.isVisible) {
            binding.writingView.clear()
        }
    }

    /**
     * 객관식 버튼 활성화/비활성화
     */
    fun setChoiceButtonsEnabled(enabled: Boolean) {
        choiceButtons.forEach { it.isEnabled = enabled }
    }

    /**
     * 객관식 버튼 리스너 설정
     */
    fun setChoiceButtonListeners(onSelected: (String) -> Unit) {
        choiceButtons.forEach { button ->
            button.setOnClickListener { onSelected(button.text.toString()) }
        }
    }

    /**
     * 정보 텍스트 업데이트
     */
    fun updateInfoText() {
        val total = viewModel.sessionTotal.value ?: 0
        val correct = viewModel.sessionCorrect.value ?: 0
        val remaining = viewModel.remainingProblems.value ?: 0
        binding.tvInfo.text = binding.root.context.getString(
            com.example.myapplication.R.string.quiz_remaining_info,
            remaining,
            correct,
            total
        )
    }

    /**
     * 워드 타입 퀴즈인지 확인
     */
    fun isWordTypeQuiz(quizType: String?): Boolean {
        return quizType in listOf(
            "verbs", "particles", "adjectives", "adverbs", "conjunctions",
            "noun", "verb", "particle", "adjective", "adverb", "conjunction"
        )
    }
}
