package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentQuizBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.model.KanaCharacter
import com.example.myapplication.model.Song
import com.example.myapplication.model.Word
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private lateinit var uiHelper: QuizUiHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiHelper = QuizUiHelper(binding, viewModel, viewLifecycleOwner)

        observeViewModel()
        setupListeners()
        if (viewModel.currentProblem.value == null) {
             viewModel.nextProblem()
        }
    }

    private fun observeViewModel() {
        uiHelper.setupObservers { problem ->
            if (problem == null) {
                showFinishDialog(true)
                return@setupObservers
            }
            uiHelper.resetUIForNewProblem()
            displayProblem(problem)
            uiHelper.updateInfoText()
        }
    }

    private fun displayProblem(problem: Any) {
        when (viewModel.quizType.value) {
            "kana" -> displayKanaProblem(problem as KanaCharacter)
            "word" -> displayWordProblem(problem as Word)
            "song" -> displaySongProblem(problem as Song)
            "sentence", "weak_sentences" -> displaySentenceProblem(problem as com.example.myapplication.model.Sentence)
            "weak_words",
            "verbs", "particles", "adjectives", "adverbs", "conjunctions",
            "noun", "verb", "particle", "adjective", "adverb", "conjunction" -> displayWordTypeProblem(problem as Word)
            "daily_word" -> displayDailyWordProblem(problem as DailyWord)
            "daily_word_listening" -> displayDailyWordListeningProblem(problem as DailyWord)
        }
    }

    private fun displayKanaProblem(problem: KanaCharacter) {
        binding.tvProblem.text = problem.kana
        binding.tvProblem.visibility = View.VISIBLE
        binding.btnSpeakQuiz.visibility = View.VISIBLE
        binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kana) }
        binding.tvHint.visibility = View.GONE
    }

    private fun displayWordProblem(problem: Word) {
        when (viewModel.quizMode.value) {
            "listening" -> {
                binding.tvProblem.visibility = View.GONE
                binding.tvHint.visibility = View.GONE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                viewModel.speak(problem.kanji)
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
            "reverse" -> {
                binding.tvProblem.text = problem.meaning
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.visibility = View.GONE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
            else -> {
                binding.tvProblem.text = problem.kanji
                binding.tvProblem.visibility = View.VISIBLE
                val isWeakWordsQuiz = viewModel.isWeakWordsQuiz.value ?: false
                if (isWeakWordsQuiz) {
                    binding.tvHint.visibility = View.GONE
                } else {
                    binding.tvHint.text = problem.hiragana
                    binding.tvHint.visibility = View.VISIBLE
                }
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
        }
    }

    private fun displaySongProblem(problem: Song) {
        if (viewModel.songViewModel.isMultipleChoice.value == true && !uiHelper.isGeneratingChoices) {
            uiHelper.isGeneratingChoices = true
            viewModel.songViewModel.generateMultipleChoices(problem)
            uiHelper.isGeneratingChoices = false
        }

        when (viewModel.songViewModel.quizMode.value) {
            "reverse" -> {
                binding.tvProblem.text = problem.meaning
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.visibility = View.GONE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
            else -> {
                binding.tvProblem.text = problem.kanji
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.text = problem.hiragana
                binding.tvHint.visibility = View.VISIBLE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
        }
    }

    private fun displaySentenceProblem(problem: com.example.myapplication.model.Sentence) {
        if (viewModel.sentenceViewModel.isMultipleChoice.value == true && !uiHelper.isGeneratingChoices) {
            uiHelper.isGeneratingChoices = true
            viewModel.sentenceViewModel.generateMultipleChoices(problem)
            uiHelper.isGeneratingChoices = false
        }

        when (viewModel.sentenceViewModel.quizMode.value) {
            "reverse" -> {
                binding.tvProblem.text = problem.meaning
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.visibility = View.GONE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
            else -> {
                binding.tvProblem.text = problem.kanji
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.text = problem.hiragana
                binding.tvHint.visibility = View.VISIBLE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
        }
    }

    private fun displayWordTypeProblem(problem: Word) {
        if (viewModel.wordViewModel.isMultipleChoice.value == true && !uiHelper.isGeneratingChoices) {
            uiHelper.isGeneratingChoices = true
            viewModel.wordViewModel.generateMultipleChoices(problem)
            uiHelper.isGeneratingChoices = false
        }

        when (viewModel.wordViewModel.quizMode.value) {
            "reverse" -> {
                binding.tvProblem.text = problem.meaning
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.visibility = View.GONE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
            else -> {
                binding.tvProblem.text = problem.kanji
                binding.tvProblem.visibility = View.VISIBLE
                binding.tvHint.text = problem.hiragana
                binding.tvHint.visibility = View.VISIBLE
                binding.btnSpeakQuiz.visibility = View.VISIBLE
                binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.kanji) }
            }
        }
    }

    private fun displayDailyWordProblem(problem: DailyWord) {
        binding.tvProblem.text = problem.word
        binding.tvProblem.visibility = View.VISIBLE
        binding.tvHint.text = problem.reading
        binding.tvHint.visibility = View.GONE
        binding.btnSpeakQuiz.visibility = View.VISIBLE
        binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.reading) }

        setupWeakWordCheckbox(problem)
    }

    private fun displayDailyWordListeningProblem(problem: DailyWord) {
        binding.tvProblem.text = "🔊"
        binding.tvProblem.visibility = View.VISIBLE
        binding.tvHint.text = problem.reading
        binding.tvHint.visibility = View.GONE
        binding.btnSpeakQuiz.visibility = View.VISIBLE
        binding.btnSpeakQuiz.setOnClickListener { viewModel.speak(problem.reading) }
        viewModel.speak(problem.reading)

        setupWeakWordCheckbox(problem)
    }

    private fun setupWeakWordCheckbox(problem: DailyWord) {
        binding.cbWeak.visibility = View.VISIBLE
        binding.cbWeak.setOnCheckedChangeListener(null)
        binding.cbWeak.isChecked = viewModel.isWeakDailyWord(problem.id)
        binding.cbWeak.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.addWeakDailyWord(problem.id)
            } else {
                viewModel.removeWeakDailyWord(problem.id)
            }
        }
    }

    private fun setupListeners() {
        binding.btnAction.setOnClickListener { onActionButtonClicked() }
        binding.btnFinish.setOnClickListener { showFinishDialog(false) }
        binding.etAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onActionButtonClicked()
                return@setOnEditorActionListener true
            }
            false
        }

        uiHelper.setChoiceButtonListeners { selectedAnswer ->
            onMultipleChoiceSelected(selectedAnswer)
        }

        binding.clearButton.setOnClickListener { binding.writingView.clear() }
        binding.toggleEraser.setOnCheckedChangeListener { _, isChecked ->
            binding.writingView.setToolType(if (isChecked) WritingView.ToolType.ERASER else WritingView.ToolType.PEN)
        }
    }

    private fun onMultipleChoiceSelected(selectedAnswer: String) {
        uiHelper.setChoiceButtonsEnabled(false)

        val isCorrect = viewModel.checkAnswer(selectedAnswer)
        showPronunciationAfterAnswer()

        if (isCorrect) {
            binding.tvResult.text = getString(R.string.quiz_correct)
            binding.tvResult.setTextColor(resources.getColor(android.R.color.white, null))
            binding.btnAction.text = getString(R.string.quiz_next_problem)
            binding.btnAction.visibility = View.VISIBLE
        } else {
            addToWrongAnswers()
            binding.tvResult.setTextColor(resources.getColor(android.R.color.white, null))
            showIncorrectAnswer()
        }
    }

    private fun onActionButtonClicked() {
        if (binding.btnAction.text == getString(R.string.quiz_next_problem)) {
            viewModel.nextProblem()
            return
        }

        val userAnswer = binding.etAnswer.text.toString()
        val isCorrect = viewModel.checkAnswer(userAnswer)
        showPronunciationAfterAnswer()

        if (isCorrect) {
            if (viewModel.quizType.value == "daily_word" ||
                viewModel.quizType.value == "daily_word_listening" ||
                viewModel.isWeakWordsQuiz.value == true) {
                binding.tvResult.text = getString(R.string.quiz_correct)
                binding.tvResult.setTextColor(resources.getColor(android.R.color.white, null))
                binding.etAnswer.isEnabled = false
                binding.btnAction.text = getString(R.string.quiz_next_problem)
            } else {
                viewModel.nextProblem()
            }
        } else {
            addToWrongAnswers()
            showIncorrectAnswer()
        }
    }

    private fun showPronunciationAfterAnswer() {
        val problem = viewModel.currentProblem.value ?: return
        when (viewModel.quizType.value) {
            "word" -> {
                val word = problem as Word
                val isWeakWordsQuiz = viewModel.isWeakWordsQuiz.value ?: false
                if (isWeakWordsQuiz && binding.tvHint.visibility != View.VISIBLE) {
                    binding.tvHint.text = word.hiragana
                    binding.tvHint.visibility = View.VISIBLE
                }
            }
            "daily_word", "daily_word_listening" -> {
                val dailyWord = problem as DailyWord
                binding.tvHint.text = dailyWord.reading
                binding.tvHint.visibility = View.VISIBLE
                if (viewModel.quizType.value == "daily_word_listening") {
                    binding.tvProblem.text = dailyWord.word
                }
            }
        }
    }

    private fun addToWrongAnswers() {
        viewModel.currentProblem.value?.let {
            val wrongList = viewModel.wrongAnswers.value ?: mutableListOf()
            if (!wrongList.contains(it)) {
                wrongList.add(it)
                viewModel.wrongAnswers.value = wrongList
            }
        }
    }

    private fun showIncorrectAnswer() {
        val problem = viewModel.currentProblem.value ?: return
        val correctAnswer = getCorrectAnswerText(problem)

        binding.tvResult.text = getString(R.string.quiz_incorrect, correctAnswer)
        binding.etAnswer.isEnabled = false
        binding.btnAction.text = getString(R.string.quiz_next_problem)
        binding.btnAction.visibility = View.VISIBLE

        showPronunciationAfterIncorrect(problem)
    }

    private fun getCorrectAnswerText(problem: Any): String {
        return when (viewModel.quizType.value) {
            "kana" -> (problem as KanaCharacter).kor
            "word" -> {
                val word = problem as Word
                if (viewModel.quizMode.value == "reverse") {
                    "${word.kanji} [${word.hiragana}]"
                } else {
                    "${word.meaning} [${word.hiragana}]"
                }
            }
            "song" -> {
                val song = problem as Song
                if (viewModel.songViewModel.quizMode.value == "reverse") {
                    "${song.kanji} [${song.hiragana}]"
                } else {
                    "${song.meaning} [${song.hiragana}]"
                }
            }
            "sentence", "weak_sentences" -> {
                val sentence = problem as com.example.myapplication.model.Sentence
                if (viewModel.sentenceViewModel.quizMode.value == "reverse") {
                    "${sentence.kanji} [${sentence.hiragana}]"
                } else {
                    "${sentence.meaning} [${sentence.hiragana}]"
                }
            }
            "weak_words",
            "verbs", "particles", "adjectives", "adverbs", "conjunctions",
            "noun", "verb", "particle", "adjective", "adverb", "conjunction" -> {
                val word = problem as Word
                val mode = viewModel.wordViewModel.quizMode.value ?: viewModel.quizMode.value
                if (mode == "reverse") {
                    "${word.kanji} [${word.hiragana}]"
                } else {
                    "${word.meaning} [${word.hiragana}]"
                }
            }
            "daily_word", "daily_word_listening" -> {
                val dailyWord = problem as DailyWord
                "${dailyWord.meaning} [${dailyWord.reading}]"
            }
            else -> ""
        }
    }

    private fun showPronunciationAfterIncorrect(problem: Any) {
        when (viewModel.quizType.value) {
            "word" -> {
                val word = problem as Word
                val isWeakWordsQuiz = viewModel.isWeakWordsQuiz.value ?: false
                if (isWeakWordsQuiz) {
                    binding.tvHint.text = word.hiragana
                    binding.tvHint.visibility = View.VISIBLE
                }
            }
            "daily_word", "daily_word_listening" -> {
                val dailyWord = problem as DailyWord
                binding.tvHint.text = dailyWord.reading
                binding.tvHint.visibility = View.VISIBLE
                if (viewModel.quizType.value == "daily_word_listening") {
                    binding.tvProblem.text = dailyWord.word
                }
            }
        }
    }

    private fun showFinishDialog(isQuizOver: Boolean) {
        val total = viewModel.sessionTotal.value ?: 0

        if (total == 0 && !isQuizOver) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.quiz_finish_learning))
                .setMessage(getString(R.string.dialog_confirm_quit))
                .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                    findNavController().popBackStack()
                }
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show()
        } else {
            findNavController().navigate(R.id.action_quiz_to_result)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
