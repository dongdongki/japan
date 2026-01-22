package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentKanaWritingPracticeBinding
import com.example.myapplication.model.KanaCharacter

class KanaWritingPracticeFragment : Fragment() {

    private var _binding: FragmentKanaWritingPracticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()
    private var currentKanaIndex: Int = 0
    private lateinit var kanaList: List<KanaCharacter>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaWritingPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get full kana list
        kanaList = viewModel.getAllKanaList()

        // Find current kana index from arguments
        val kana = arguments?.getString("kana")
        if (kana != null) {
            currentKanaIndex = kanaList.indexOfFirst { it.kana == kana }
            if (currentKanaIndex == -1) currentKanaIndex = 0
        }

        // Display current kana
        displayKana(currentKanaIndex)

        // Setup navigation buttons
        binding.btnPrevious.setOnClickListener {
            currentKanaIndex = if (currentKanaIndex > 0) {
                currentKanaIndex - 1
            } else {
                kanaList.size - 1
            }
            displayKana(currentKanaIndex)
            binding.writingView.clear()
        }

        binding.btnNext.setOnClickListener {
            currentKanaIndex = if (currentKanaIndex < kanaList.size - 1) {
                currentKanaIndex + 1
            } else {
                0
            }
            displayKana(currentKanaIndex)
            binding.writingView.clear()
        }

        // Observe pen and eraser width changes
        viewModel.penWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setPenWidth(width)
        }
        viewModel.eraserWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setEraserWidth(width)
        }

        binding.clearButton.setOnClickListener {
            binding.writingView.clear()
        }

        binding.toggleEraser.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.writingView.setToolType(WritingView.ToolType.ERASER)
            } else {
                binding.writingView.setToolType(WritingView.ToolType.PEN)
            }
        }

        binding.btnSpeak.setOnClickListener {
            if (currentKanaIndex in kanaList.indices) {
                val kana = kanaList[currentKanaIndex]
                viewModel.speak(kana.kana)
            }
        }
    }

    private fun displayKana(index: Int) {
        if (index in kanaList.indices) {
            val kana = kanaList[index]
            binding.kanaToPractice.text = kana.kana
            binding.tvRomanization.text = kana.romaji
            binding.tvKorean.text = kana.kor

            // Buttons are always enabled for circular navigation
            binding.btnPrevious.isEnabled = true
            binding.btnNext.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
