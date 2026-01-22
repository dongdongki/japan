package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.JapaneseStudyApp
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWeakDailyWordListBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.repository.DailyWordRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeakDailyWordListFragment : Fragment() {

    private var _binding: FragmentWeakDailyWordListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: DailyWordRepository
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeakDailyWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = DailyWordRepository(requireContext())

        setupList()

        // Observe weak daily words changes to refresh list
        viewModel.weakDailyWords.observe(viewLifecycleOwner) {
            refreshList()
        }
    }

    private fun setupList() {
        val allWords = repository.getAllWords()
        val weakWordIds = viewModel.weakDailyWords.value.orEmpty()
        val weakWords = allWords.filter { it.id in weakWordIds }

        binding.tvCount.text = getString(R.string.daily_word_count, weakWords.size)

        // Use 2 columns for phones, 5 for tablets (sw600dp)
        val spanCount = if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.adapter = WeakWordAdapter(
            words = weakWords,
            onSpeakClick = { word -> speak(word) },
            onItemClick = { wordId ->
                val bundle = Bundle().apply {
                    putInt("wordId", wordId)
                }
                findNavController().navigate(R.id.action_weak_list_to_practice, bundle)
            },
            onWeakCheckChanged = { wordId, isChecked ->
                if (isChecked) {
                    viewModel.addWeakDailyWord(wordId)
                } else {
                    viewModel.removeWeakDailyWord(wordId)
                }
            }
        )
    }

    private fun refreshList() {
        val allWords = repository.getAllWords()
        val weakWordIds = viewModel.weakDailyWords.value.orEmpty()
        val weakWords = allWords.filter { it.id in weakWordIds }
        binding.tvCount.text = getString(R.string.daily_word_count, weakWords.size)

        val spanCount = if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.adapter = WeakWordAdapter(
            words = weakWords,
            onSpeakClick = { word -> speak(word) },
            onItemClick = { wordId ->
                val bundle = Bundle().apply {
                    putInt("wordId", wordId)
                }
                findNavController().navigate(R.id.action_weak_list_to_practice, bundle)
            },
            onWeakCheckChanged = { wordId, isChecked ->
                if (isChecked) {
                    viewModel.addWeakDailyWord(wordId)
                } else {
                    viewModel.removeWeakDailyWord(wordId)
                }
            }
        )
    }

    private fun speak(text: String) {
        JapaneseStudyApp.speak(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for weak word list
    private inner class WeakWordAdapter(
        private val words: List<DailyWord>,
        private val onSpeakClick: (String) -> Unit,
        private val onItemClick: (Int) -> Unit,
        private val onWeakCheckChanged: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<WeakWordAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWord: TextView = itemView.findViewById(R.id.tv_word)
            val tvReading: TextView = itemView.findViewById(R.id.tv_reading)
            val tvMeaning: TextView = itemView.findViewById(R.id.tv_meaning)
            val tvExampleJp: TextView = itemView.findViewById(R.id.tv_example_jp)
            val tvExampleKr: TextView = itemView.findViewById(R.id.tv_example_kr)
            val btnSpeak: ImageButton = itemView.findViewById(R.id.btn_speak)
            val cbWeak: CheckBox = itemView.findViewById(R.id.cb_weak)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_word, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val word = words[position]
            holder.tvWord.text = word.word
            holder.tvReading.text = word.reading
            holder.tvMeaning.text = word.meaning
            holder.tvExampleJp.text = word.exampleJp
            holder.tvExampleKr.text = word.exampleKr

            holder.cbWeak.setOnCheckedChangeListener(null)
            holder.cbWeak.isChecked = true // Always checked in this list
            holder.cbWeak.setOnCheckedChangeListener { _, isChecked ->
                onWeakCheckChanged(word.id, isChecked)
            }

            holder.btnSpeak.setOnClickListener {
                onSpeakClick(word.reading)
            }

            holder.itemView.setOnClickListener {
                onItemClick(word.id)
            }
        }

        override fun getItemCount(): Int = words.size
    }
}
