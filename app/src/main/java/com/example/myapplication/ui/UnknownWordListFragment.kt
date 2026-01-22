package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.JapaneseStudyApp
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUnknownWordListBinding
import com.example.myapplication.model.UnknownWord
import com.example.myapplication.repository.SentenceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UnknownWordListFragment : Fragment() {

    private var _binding: FragmentUnknownWordListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sentenceRepository: SentenceRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnknownWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val batchId = arguments?.getString("batchId")
        val batchNumber = arguments?.getInt("batchNumber") ?: -1

        binding.tvTitle.text = "${batchNumber}회차 없는 단어"

        if (batchId != null) {
            val unknownWords = sentenceRepository.getUnknownWordsByBatchId(batchId)

            if (unknownWords.isEmpty()) {
                binding.recyclerView.isVisible = false
                binding.tvEmpty.isVisible = true
            } else {
                binding.recyclerView.isVisible = true
                binding.tvEmpty.isVisible = false

                val spanCount = if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 2
                binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
                binding.recyclerView.adapter = UnknownWordAdapter(unknownWords) { word ->
                    JapaneseStudyApp.speak(word.reading)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 어댑터 (일일 단어와 동일한 UI 사용)
    private class UnknownWordAdapter(
        private val words: List<UnknownWord>,
        private val onSpeakClick: (UnknownWord) -> Unit
    ) : RecyclerView.Adapter<UnknownWordAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWord: TextView = itemView.findViewById(R.id.tv_word)
            val tvReading: TextView = itemView.findViewById(R.id.tv_reading)
            val tvMeaning: TextView = itemView.findViewById(R.id.tv_meaning)
            val btnSpeak: ImageButton = itemView.findViewById(R.id.btn_speak)
            val cbWeak: View = itemView.findViewById(R.id.cb_weak)
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

            // 체크박스는 숨김 (선택 모음집 기능 불필요)
            holder.cbWeak.visibility = View.GONE

            holder.btnSpeak.setOnClickListener {
                onSpeakClick(word)
            }
        }

        override fun getItemCount(): Int = words.size
    }
}
