package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentKanaWritingTestBinding
import com.example.myapplication.model.KanaCharacter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedKanaQuizFragment : Fragment() {

    private var _binding: FragmentKanaWritingTestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private var currentQuizList: MutableList<KanaCharacter> = mutableListOf()
    private var currentIndex: Int = 0
    private var savedUserDrawing: android.graphics.Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaWritingTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get selected kana characters and shuffle
        currentQuizList = viewModel.getSelectedKanaList().toMutableList()
        currentQuizList.shuffle()

        if (currentQuizList.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        currentIndex = 0

        // Hide checkbox for quiz mode
        binding.checkboxSelect.visibility = View.GONE

        // Display first problem
        displayProblem()

        // Setup buttons
        binding.btnShowAnswer.setOnClickListener {
            if (binding.answerCard.visibility == View.GONE) {
                // Save user's drawing before showing answer
                savedUserDrawing = binding.writingView.saveBitmap()

                // Hide empty space and show answer card
                binding.emptySpace.visibility = View.GONE
                binding.answerCard.visibility = View.VISIBLE
                binding.btnShowAnswer.text = "다음"

                // Keep writing enabled so user can continue writing
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
            val kana = currentQuizList[currentIndex]
            viewModel.speak(kana.kana)
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
        val kana = currentQuizList[currentIndex]

        // Update problem count
        binding.tvProblemCount.text = "${currentIndex + 1}/${currentQuizList.size}"

        // Determine if hiragana or katakana based on unicode range
        val kanaType = when {
            kana.kana.first() in '\u3040'..'\u309F' -> "ひ"
            kana.kana.first() in '\u30A0'..'\u30FF' -> "カ"
            else -> "?"
        }
        binding.tvKanaType.text = kanaType

        // Display romaji and korean
        binding.tvRomaji.text = kana.romaji
        binding.tvKorean.text = kana.kor

        // Display answer (hidden initially)
        binding.tvAnswerKana.text = kana.kana
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savedUserDrawing?.recycle()
        savedUserDrawing = null
        _binding = null
    }
}
