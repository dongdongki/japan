package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDailyWordDaySelectionBinding
import com.example.myapplication.repository.DailyWordRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyWordDaySelectionFragment : Fragment() {

    private var _binding: FragmentDailyWordDaySelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: DailyWordRepository
    private val checkedDays = mutableSetOf<Int>()
    private val viewModel: QuizViewModel by activityViewModels()

    companion object {
        const val PREFS_NAME = "daily_word_prefs"
        const val KEY_CHECKED_DAYS = "checked_days"
        const val KEY_WEAK_WORDS = "weak_daily_words"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyWordDaySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = DailyWordRepository(requireContext())
        loadCheckedDays()

        val totalDays = repository.getTotalDays()
        val totalWords = repository.getAllWords().size

        binding.tvTotalInfo.text = "총 ${totalWords}개 단어 / ${totalDays}일차"

        // Use 2 columns for phones, 4 for tablets (sw600dp)
        val spanCount = if (resources.configuration.smallestScreenWidthDp >= 600) 4 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.adapter = DaySelectionAdapter(
            totalDays = totalDays,
            onDayClick = { day ->
                val bundle = Bundle().apply {
                    putInt("day", day)
                }
                findNavController().navigate(R.id.action_daily_word_day_selection_to_daily_word_list, bundle)
            },
            onQuizClick = { day ->
                showQuizTypeDialog(setOf(day))
            },
            onCheckChanged = { day, isChecked ->
                if (isChecked) {
                    checkedDays.add(day)
                } else {
                    checkedDays.remove(day)
                }
                saveCheckedDays()
            }
        )

        // Quiz buttons
        binding.btnWordQuiz.setOnClickListener {
            if (checkedDays.isEmpty()) {
                Toast.makeText(context, "학습할 일차를 먼저 체크해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showQuizTypeDialog(checkedDays)
        }

        binding.btnWeakWordQuiz.setOnClickListener {
            val weakWordIds = viewModel.weakDailyWords.value.orEmpty()
            if (weakWordIds.isEmpty()) {
                Toast.makeText(context, "취약 단어를 먼저 체크해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showWeakQuizTypeDialog(weakWordIds)
        }

        binding.btnWeakWordList.setOnClickListener {
            findNavController().navigate(R.id.action_daily_word_day_selection_to_weak_list)
        }

        binding.btnGenerateSentences?.setOnClickListener {
            if (checkedDays.isEmpty()) {
                Toast.makeText(context, "학습할 일차를 먼저 체크해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bundle = Bundle().apply {
                putIntArray("checkedDays", checkedDays.toIntArray())
            }
            findNavController().navigate(R.id.action_daily_word_day_selection_to_generated_sentences, bundle)
        }

        binding.btnRandomQuiz?.setOnClickListener {
            if (checkedDays.isEmpty()) {
                Toast.makeText(context, "학습할 일차를 먼저 체크해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startRandomQuiz(checkedDays)
        }
    }

    private fun showQuizTypeDialog(days: Set<Int>) {
        // Navigate to quiz mode selection fragment
        val bundle = Bundle().apply {
            putIntArray("selectedDays", days.toIntArray())
            putBoolean("isWeakWordMode", false)
        }
        findNavController().navigate(R.id.action_daily_word_day_selection_to_quiz_mode, bundle)
    }

    private fun showWeakQuizTypeDialog(weakWordIds: Set<Int>) {
        // Navigate to quiz mode selection fragment
        val bundle = Bundle().apply {
            putIntArray("weakWordIds", weakWordIds.toIntArray())
            putBoolean("isWeakWordMode", true)
        }
        findNavController().navigate(R.id.action_daily_word_day_selection_to_quiz_mode, bundle)
    }

    private fun loadCheckedDays() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDays = prefs.getStringSet(KEY_CHECKED_DAYS, emptySet()) ?: emptySet()
        checkedDays.clear()
        checkedDays.addAll(savedDays.map { it.toInt() })
    }

    private fun saveCheckedDays() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CHECKED_DAYS, checkedDays.map { it.toString() }.toSet()).apply()
    }

    private fun startRandomQuiz(days: Set<Int>) {
        // 선택된 일차에서 모든 단어 가져오기
        val allWords = mutableListOf<com.example.myapplication.model.DailyWord>()
        days.forEach { day ->
            allWords.addAll(repository.getWordsForDay(day))
        }

        if (allWords.isEmpty()) {
            Toast.makeText(context, "선택한 일차에 단어가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 랜덤으로 섞기
        val shuffledWords = allWords.shuffled()

        // 선택한 일차 수 × 5개 문제
        val questionCount = days.size * 5
        val quizWords = shuffledWords.take(questionCount)

        if (quizWords.size < questionCount) {
            Toast.makeText(
                context,
                "단어가 부족합니다 (필요: ${questionCount}개, 실제: ${quizWords.size}개)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        android.util.Log.d("RandomQuiz", "랜덤 퀴즈 시작: ${days.size}일차 선택, ${questionCount}문제")

        // ViewModel에 퀴즈 시작
        viewModel.startRandomQuiz(quizWords)

        // 퀴즈 화면으로 이동
        findNavController().navigate(R.id.action_daily_word_day_selection_to_quiz)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for day selection
    private inner class DaySelectionAdapter(
        private val totalDays: Int,
        private val onDayClick: (Int) -> Unit,
        private val onQuizClick: (Int) -> Unit,
        private val onCheckChanged: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<DaySelectionAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
            val tvWordRange: TextView = itemView.findViewById(R.id.tv_word_range)
            val btnQuiz: android.widget.Button = itemView.findViewById(R.id.btn_quiz)
            val cbCompleted: CheckBox = itemView.findViewById(R.id.cb_completed)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_word_day, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val day = position + 1
            holder.tvDayNumber.text = "${day}일차"

            val startWord = (day - 1) * 20 + 1
            val endWord = minOf(day * 20, repository.getAllWords().size)
            holder.tvWordRange.text = "${startWord}-${endWord}번"

            holder.cbCompleted.setOnCheckedChangeListener(null)
            holder.cbCompleted.isChecked = checkedDays.contains(day)
            holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(day, isChecked)
            }

            holder.btnQuiz.setOnClickListener {
                onQuizClick(day)
            }

            holder.itemView.setOnClickListener {
                onDayClick(day)
            }
        }

        override fun getItemCount(): Int = totalDays
    }
}
