package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.JapaneseStudyApp
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDailyWordListBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.repository.DailyWordRepository

class DailyWordListFragment : Fragment() {

    private var _binding: FragmentDailyWordListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: DailyWordRepository
    private val weakWords = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = DailyWordRepository(requireContext())
        loadWeakWords()

        val day = arguments?.getInt("day") ?: 1
        val words = repository.getWordsForDay(day)

        binding.tvDayTitle.text = "${day}일차\n(${words.size}단어)"

        // Use 2 columns for phones, 5 for tablets (sw600dp)
        val spanCount = if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.adapter = DailyWordAdapter(
            words = words,
            day = day,
            onSpeakClick = { word -> speak(word) },
            onItemClick = { index ->
                val bundle = Bundle().apply {
                    putInt("day", day)
                    putInt("wordIndex", index)
                }
                findNavController().navigate(R.id.action_daily_word_list_to_practice, bundle)
            },
            onWeakCheckChanged = { wordId, isChecked ->
                if (isChecked) {
                    weakWords.add(wordId)
                } else {
                    weakWords.remove(wordId)
                }
                saveWeakWords()
            }
        )
    }

    private fun loadWeakWords() {
        val prefs = requireContext().getSharedPreferences(DailyWordDaySelectionFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val savedWeakWords = prefs.getStringSet(DailyWordDaySelectionFragment.KEY_WEAK_WORDS, emptySet()) ?: emptySet()
        weakWords.clear()
        weakWords.addAll(savedWeakWords.map { it.toInt() })
    }

    private fun saveWeakWords() {
        val prefs = requireContext().getSharedPreferences(DailyWordDaySelectionFragment.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(DailyWordDaySelectionFragment.KEY_WEAK_WORDS, weakWords.map { it.toString() }.toSet()).apply()
    }

    private fun speak(text: String) {
        JapaneseStudyApp.speak(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for daily word list (compact grid view)
    private inner class DailyWordAdapter(
        private val words: List<DailyWord>,
        private val day: Int,
        private val onSpeakClick: (String) -> Unit,
        private val onItemClick: (Int) -> Unit,
        private val onWeakCheckChanged: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<DailyWordAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWord: TextView = itemView.findViewById(R.id.tv_word)
            val tvReading: TextView = itemView.findViewById(R.id.tv_reading)
            val tvMeaning: TextView = itemView.findViewById(R.id.tv_meaning)
            val btnSpeak: ImageButton = itemView.findViewById(R.id.btn_speak)
            val cbWeak: CheckBox = itemView.findViewById(R.id.cb_weak)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_word_compact, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val word = words[position]
            holder.tvWord.text = word.word
            holder.tvReading.text = word.reading
            holder.tvMeaning.text = word.meaning

            holder.cbWeak.setOnCheckedChangeListener(null)
            holder.cbWeak.isChecked = weakWords.contains(word.id)
            holder.cbWeak.setOnCheckedChangeListener { _, isChecked ->
                onWeakCheckChanged(word.id, isChecked)
            }

            holder.btnSpeak.setOnClickListener {
                onSpeakClick(word.reading)
            }

            holder.itemView.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount(): Int = words.size
    }
}
