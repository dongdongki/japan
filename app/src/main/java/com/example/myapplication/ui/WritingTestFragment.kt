package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentWritingTestBinding
import com.example.myapplication.model.Sentence
import com.example.myapplication.model.Song
import com.example.myapplication.model.Word
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WritingTestFragment : Fragment() {

    private var _binding: FragmentWritingTestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private var currentQuizList: MutableList<Any> = mutableListOf()
    private var currentIndex: Int = 0
    private var savedUserDrawing: android.graphics.Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get quiz list from QuizViewModel current problem list
        val quizType = viewModel.quizType.value ?: "word"

        // Initialize quiz list based on type
        currentQuizList = when (quizType) {
            "word" -> viewModel.getWordList().toMutableList()
            "song" -> viewModel.getSongVocabulary(viewModel.currentSongDirectory).toMutableList()
            "sentence" -> viewModel.getSentenceList().toMutableList()
            "weak_words" -> viewModel.getWeakWordList().toMutableList()  // Only words, not songs/sentences
            "weak_sentences" -> viewModel.getWeakSentenceList().toMutableList()
            "verb", "particle", "adjective", "adverb", "conjunction", "noun" -> {
                // For part-of-speech specific quizzes
                viewModel.getWordListByPartOfSpeech(quizType).toMutableList()
            }
            else -> viewModel.getWordList().toMutableList()
        }

        if (currentQuizList.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        // Shuffle the list
        currentQuizList.shuffle()
        currentIndex = 0

        // Display first problem
        displayProblem()

        // Setup buttons
        binding.btnShowAnswer.setOnClickListener {
            if (binding.answerCard.visibility == View.GONE) {
                // Save user's drawing before showing answer (backup in case view gets cleared)
                savedUserDrawing = binding.writingView.saveBitmap()

                // Hide empty space and show answer card
                binding.emptySpace.visibility = View.GONE
                binding.answerCard.visibility = View.VISIBLE
                binding.btnShowAnswer.text = "다음"

                // Keep writing enabled so user can continue writing
                android.util.Log.d("WritingTest", "Answer shown, drawing preserved. Empty: ${binding.writingView.isEmpty()}")
            } else {
                // Move to next problem
                currentIndex++
                if (currentIndex >= currentQuizList.size) {
                    // Quiz finished
                    findNavController().popBackStack()
                } else {
                    // Show next problem
                    binding.emptySpace.visibility = View.VISIBLE
                    binding.answerCard.visibility = View.GONE
                    binding.btnShowAnswer.text = "정답 보기"
                    binding.writingView.clear()
                    savedUserDrawing?.recycle()
                    savedUserDrawing = null
                    displayProblem()
                }
            }
        }

        binding.btnSpeakAnswer.setOnClickListener {
            when (val item = currentQuizList[currentIndex]) {
                is Word -> viewModel.speak(item.kanji)
                is Song -> viewModel.speak(item.kanji)
                is Sentence -> viewModel.speak(item.kanji)
            }
        }

        binding.btnClear.setOnClickListener {
            binding.writingView.clear()
        }

        // Observe pen and eraser width changes
        viewModel.penWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setPenWidth(width)
        }
        viewModel.eraserWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setEraserWidth(width)
        }
    }

    private fun displayProblem() {
        val item = currentQuizList[currentIndex]

        // Update problem count
        binding.tvProblemCount.text = "${currentIndex + 1}/${currentQuizList.size}"

        when (item) {
            is Word -> {
                binding.tvMeaning.text = item.meaning
                binding.tvAnswerKanji.text = item.kanji
                binding.tvAnswerHiragana.text = item.hiragana
            }
            is Song -> {
                binding.tvMeaning.text = item.meaning
                binding.tvAnswerKanji.text = item.kanji
                binding.tvAnswerHiragana.text = item.hiragana
            }
            is Sentence -> {
                binding.tvMeaning.text = item.meaning
                binding.tvAnswerKanji.text = item.kanji
                binding.tvAnswerHiragana.text = item.hiragana
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savedUserDrawing?.recycle()
        savedUserDrawing = null
        _binding = null
    }
}
