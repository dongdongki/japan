package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.JapaneseStudyApp
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGeneratedSentencesBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.model.GeneratedSentence
import com.example.myapplication.model.Sentence
import com.example.myapplication.repository.DailyWordRepository
import com.example.myapplication.repository.SentenceGeneratorRepository
import com.example.myapplication.repository.SentenceRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeneratedSentencesFragment : Fragment() {

    private var _binding: FragmentGeneratedSentencesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sentenceGeneratorRepo: SentenceGeneratorRepository
    private lateinit var dailyWordRepo: DailyWordRepository

    @Inject
    lateinit var sentenceRepository: SentenceRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratedSentencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sentenceGeneratorRepo = SentenceGeneratorRepository(requireContext())
        dailyWordRepo = DailyWordRepository(requireContext())

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val checkedDays = arguments?.getIntArray("checkedDays")?.toList() ?: emptyList()
        if (checkedDays.isEmpty()) {
            showError("체크된 일차가 없습니다")
            return
        }

        generateSentences(checkedDays)
    }

    private fun generateSentences(checkedDays: List<Int>) {
        // Show loading
        binding.progressBar.isVisible = true
        binding.tvStatus.isVisible = true
        binding.tvStatus.text = "문장 생성 중..."

        lifecycleScope.launch {
            try {
                // Get all words from checked days
                val allWords = mutableListOf<DailyWord>()
                checkedDays.forEach { day ->
                    allWords.addAll(dailyWordRepo.getWordsForDay(day))
                }

                if (allWords.isEmpty()) {
                    showError("선택한 일차에 단어가 없습니다")
                    return@launch
                }

                // Generate sentences
                val result = sentenceGeneratorRepo.generateSentences(allWords)

                binding.progressBar.isVisible = false
                binding.tvStatus.isVisible = false

                result.onSuccess { generationResult ->
                    if (generationResult.sentences.isEmpty()) {
                        showError("문장 생성에 실패했습니다")
                    } else {
                        // Convert GeneratedSentence to Sentence
                        val sentences = generationResult.sentences.map { gen ->
                            Sentence(
                                id = 0, // ID는 saveBatch에서 자동 부여
                                kanji = gen.japanese,
                                meaning = gen.korean,
                                hiragana = gen.reading
                            )
                        }

                        // 사용된 단어 목록
                        val usedWords = allWords.map { it.word }

                        // Save to repository using new batch API
                        sentenceRepository.saveBatch(sentences, usedWords)

                        // Show confirmation and display
                        showSaveConfirmation(generationResult.sentences, sentences.size)
                    }
                }.onFailure { error ->
                    showError("오류: ${error.message}")
                }
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                binding.tvStatus.isVisible = false
                showError("예외 발생: ${e.message}")
            }
        }
    }

    private fun displaySentences(sentences: List<GeneratedSentence>) {
        binding.recyclerView.adapter = SentenceAdapter(sentences) { sentence ->
            JapaneseStudyApp.speak(sentence.reading)
        }
    }

    private fun showSaveConfirmation(sentences: List<GeneratedSentence>, count: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("문장 저장 완료")
            .setMessage("${count}개의 문장이 생성되어 저장되었습니다.\n\n이제 '문장 학습 시작' 메뉴에서 생성된 문장으로 학습할 수 있습니다.")
            .setPositiveButton("확인") { _, _ ->
                displaySentences(sentences)
            }
            .show()
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.tvStatus.isVisible = true
        binding.tvStatus.text = message

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("오류")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SentenceAdapter(
        private val sentences: List<GeneratedSentence>,
        private val onSpeakClick: (GeneratedSentence) -> Unit
    ) : RecyclerView.Adapter<SentenceAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvJapanese: TextView = itemView.findViewById(R.id.tv_japanese)
            val tvReading: TextView = itemView.findViewById(R.id.tv_reading)
            val tvKorean: TextView = itemView.findViewById(R.id.tv_korean)
            val btnSpeak: ImageButton = itemView.findViewById(R.id.btn_speak)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_generated_sentence, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sentence = sentences[position]
            holder.tvJapanese.text = sentence.japanese
            holder.tvReading.text = sentence.reading
            holder.tvKorean.text = sentence.korean

            holder.btnSpeak.setOnClickListener {
                onSpeakClick(sentence)
            }
        }

        override fun getItemCount(): Int = sentences.size
    }
}
