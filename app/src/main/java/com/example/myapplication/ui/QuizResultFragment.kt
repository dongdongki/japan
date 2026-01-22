package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentQuizResultBinding
import com.example.myapplication.model.KanaCharacter
import com.example.myapplication.model.Word
import com.example.myapplication.model.DailyWord

class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayResults()

        binding.btnFinish.setOnClickListener {
            // Navigate back to the quiz category/setup screen (pop quiz and result fragments)
            findNavController().popBackStack()
            findNavController().popBackStack()
        }
    }

    private fun displayResults() {
        val total = viewModel.sessionTotal.value ?: 0
        val correct = viewModel.sessionCorrect.value ?: 0
        val accuracy = if (total > 0) (correct * 100) / total else 0
        val wrongList = viewModel.wrongAnswers.value ?: mutableListOf()

        binding.tvSummary.text = getString(R.string.result_summary, total, correct, accuracy)

        if (wrongList.isEmpty()) {
            binding.tvNoWrongAnswers.isVisible = true
            binding.wrongAnswersContainer.findViewById<LinearLayout>(com.example.myapplication.R.id.wrong_answers_list).isVisible = false
        } else {
            binding.tvNoWrongAnswers.isVisible = false
            populateWrongAnswers(wrongList)
        }
    }

    private fun populateWrongAnswers(wrongList: List<Any>) {
        val container = binding.wrongAnswersContainer.findViewById<LinearLayout>(com.example.myapplication.R.id.wrong_answers_list)
        container.removeAllViews()

        wrongList.forEach { problem ->
            val rowView = createTableRow(problem)
            container.addView(rowView)
        }
    }

    private fun createTableRow(problem: Any): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 12, 16, 12)
            setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))
        }

        when (viewModel.quizType.value) {
            "kana" -> {
                val kana = problem as KanaCharacter
                row.addView(createCell(kana.kana, 1f))
                row.addView(createCell("-", 1f, android.view.Gravity.CENTER))
                row.addView(createCell(kana.kor, 1f, android.view.Gravity.END))
            }
            "word" -> {
                val word = problem as Word
                row.addView(createCell(word.kanji, 1f))
                row.addView(createCell(word.hiragana, 1f, android.view.Gravity.CENTER))
                row.addView(createCell(word.meaning, 1f, android.view.Gravity.END))
            }
            "daily_word", "daily_word_listening" -> {
                val dailyWord = problem as DailyWord
                row.addView(createCell(dailyWord.word, 1f))
                row.addView(createCell(dailyWord.reading, 1f, android.view.Gravity.CENTER))
                row.addView(createCell(dailyWord.meaning, 1f, android.view.Gravity.END))
            }
        }

        // Add divider
        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val containerWithDivider = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        containerWithDivider.addView(row)
        containerWithDivider.addView(divider)

        return containerWithDivider
    }

    private fun createCell(text: String, weight: Float, gravity: Int = android.view.Gravity.START): TextView {
        return TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                weight
            )
            this.text = text
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            this.gravity = gravity
            setPadding(8, 0, 8, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
