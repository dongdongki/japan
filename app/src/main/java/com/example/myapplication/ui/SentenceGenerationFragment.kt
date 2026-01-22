package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSentenceGenerationBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.model.Sentence
import com.example.myapplication.repository.DailyWordRepository
import com.example.myapplication.repository.SentenceBatchInfo
import com.example.myapplication.repository.SentenceGeneratorRepository
import com.example.myapplication.repository.SentenceRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SentenceGenerationFragment : Fragment() {

    private var _binding: FragmentSentenceGenerationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    @Inject
    lateinit var sentenceRepository: SentenceRepository

    private lateinit var sentenceGeneratorRepo: SentenceGeneratorRepository
    private lateinit var dailyWordRepo: DailyWordRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSentenceGenerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sentenceGeneratorRepo = SentenceGeneratorRepository(requireContext())
        dailyWordRepo = DailyWordRepository(requireContext())

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        updateUI()

        binding.btnGenerateSentences.setOnClickListener {
            generateSentences()
        }
    }

    private fun updateUI() {
        val batches = sentenceRepository.getAllBatches()

        android.util.Log.d("SentenceGenerationFragment", "updateUI: ${batches.size}개 배치 로드됨")

        if (batches.isEmpty()) {
            binding.tvInfo.isVisible = true
            binding.recyclerView.isVisible = false
        } else {
            binding.tvInfo.isVisible = false
            binding.recyclerView.isVisible = true
            binding.recyclerView.adapter = BatchAdapter(
                batches,
                onViewClick = { batch -> navigateToSentenceList(batch) },
                onDeleteClick = { batch -> deleteBatch(batch) },
                onUnknownWordsClick = { batch -> navigateToUnknownWordList(batch) }
            )
        }
    }

    private fun generateSentences() {
        // 전체 일차 수 가져오기
        val totalDays = dailyWordRepo.getTotalDays()

        // 일차 목록 생성 (1일차, 2일차, ...)
        val dayItems = (1..totalDays).map { "${it}일차" }.toTypedArray()
        // 초기 선택 상태는 모두 false (빈 상태로 시작)
        val selectedDays = BooleanArray(totalDays) { false }

        android.util.Log.d("SentenceGenerationFragment", "문장 생성 다이얼로그 열림: 총 ${totalDays}일차")

        // 일차 선택 다이얼로그
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("문장 생성할 일차 선택")
            .setMultiChoiceItems(dayItems, selectedDays) { _, which, isChecked ->
                selectedDays[which] = isChecked
            }
            .setPositiveButton("생성") { _, _ ->
                // 선택된 일차 확인
                val checkedDays = mutableListOf<Int>()
                selectedDays.forEachIndexed { index, isChecked ->
                    if (isChecked) {
                        checkedDays.add(index + 1)
                    }
                }

                android.util.Log.d("SentenceGenerationFragment", "생성 버튼 클릭: 선택된 일차 = ${checkedDays.joinToString(", ")}")

                if (checkedDays.isEmpty()) {
                    Toast.makeText(context, "일차를 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // 문장 생성 시작
                startGeneratingSentences(checkedDays)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun startGeneratingSentences(checkedDays: List<Int>) {
        // 프로그레스 다이얼로그 생성
        val progressBar = android.widget.ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            isIndeterminate = false
        }
        val textView = android.widget.TextView(requireContext()).apply {
            text = "문장 생성 준비 중..."
            textSize = 16f
            setPadding(50, 40, 50, 20)
        }
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            addView(textView)
            addView(progressBar)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("문장 생성 중")
            .setView(layout)
            .setCancelable(false)
            .create()
        dialog.show()

        lifecycleScope.launch {
            try {
                // 선택된 일차의 모든 단어 가져오기
                val allWords = mutableListOf<DailyWord>()

                android.util.Log.d("SentenceGenerationFragment", "선택된 일차: ${checkedDays.joinToString(", ")}")

                checkedDays.forEach { day ->
                    val wordsForDay = dailyWordRepo.getWordsForDay(day)
                    android.util.Log.d("SentenceGenerationFragment", "${day}일차: ${wordsForDay.size}개 단어")
                    allWords.addAll(wordsForDay)
                }

                android.util.Log.d("SentenceGenerationFragment", "전체 단어: ${allWords.size}개")

                if (allWords.isEmpty()) {
                    dialog.dismiss()
                    showError("선택한 일차에 단어가 없습니다")
                    return@launch
                }

                // 기존에 추출된 모르는 단어 목록 가져오기 (중복 방지용)
                val existingUnknownWords = sentenceRepository.getAllUnknownWords()
                android.util.Log.d("SentenceGenerationFragment", "기존 모르는 단어: ${existingUnknownWords.size}개")

                // 문장 생성 (프로그레스 콜백 포함)
                val result = sentenceGeneratorRepo.generateSentences(allWords, existingUnknownWords) { current, total ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        progressBar.max = total
                        progressBar.progress = current
                        textView.text = "배치 $current/$total 생성 중..."
                    }
                }

                dialog.dismiss()

                result.onSuccess { generationResult ->
                    if (generationResult.sentences.isEmpty()) {
                        showError("문장 생성에 실패했습니다 (생성된 문장이 없음)\n\n로그를 확인하세요.")
                        android.util.Log.w("SentenceGenerationFragment", "API 호출은 성공했지만 생성된 문장이 0개입니다")
                    } else {
                        // Sentence로 변환 (ID는 saveBatch에서 자동 부여)
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

                        // 새로운 배치로 저장 (모르는 단어 포함)
                        val batchInfo = sentenceRepository.saveBatch(
                            sentences,
                            usedWords,
                            generationResult.unknownWords
                        )

                        android.util.Log.d("SentenceGenerationFragment", "배치 저장 완료: ${batchInfo.batchNumber}회차, ${batchInfo.sentenceCount}개 문장, ${batchInfo.unknownWordCount}개 모르는 단어")

                        // UI 업데이트
                        updateUI()

                        // 결과 다이얼로그 표시
                        showGenerationResult(generationResult, sentences.size)
                    }
                }.onFailure { error ->
                    showError("오류: ${error.message}")
                }
            } catch (e: Exception) {
                dialog.dismiss()
                showError("예외 발생: ${e.message}")
            }
        }
    }

    private fun navigateToSentenceList(batch: SentenceBatchInfo) {
        // 배치 ID를 전달하여 해당 배치의 문장만 표시
        val bundle = Bundle().apply {
            putString("batchId", batch.batchId)
            putInt("batchNumber", batch.batchNumber)
        }
        findNavController().navigate(R.id.action_sentence_generation_to_sentence_list, bundle)
    }

    private fun deleteBatch(batch: SentenceBatchInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("회차 삭제")
            .setMessage("${batch.batchNumber}회차를 삭제하시겠습니까?\n\n(${batch.sentenceCount}개 문장이 삭제됩니다)")
            .setPositiveButton("삭제") { _, _ ->
                val deleted = sentenceRepository.deleteBatch(batch.batchId)

                if (deleted) {
                    // 삭제된 문장 ID들을 weak sentences에서도 제거
                    val deletedIds = sentenceRepository.getLastDeletedSentenceIds()
                    if (deletedIds.isNotEmpty()) {
                        viewModel.removeDeletedSentenceIds(deletedIds)
                        android.util.Log.d("SentenceGenerationFragment", "삭제된 문장 ${deletedIds.size}개를 선택 모음집에서 제거")
                    }

                    android.util.Log.d("SentenceGenerationFragment", "${batch.batchNumber}회차 삭제 성공")
                    updateUI()
                    Toast.makeText(context, "${batch.batchNumber}회차가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.e("SentenceGenerationFragment", "${batch.batchNumber}회차 삭제 실패")
                    Toast.makeText(context, "삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showGenerationResult(result: com.example.myapplication.model.SentenceGenerationResult, sentenceCount: Int) {
        val messageBuilder = StringBuilder()
        messageBuilder.append("문장 생성 완료\n\n")
        messageBuilder.append("생성 결과:\n")
        messageBuilder.append("  - 생성된 문장: ${sentenceCount}개\n")
        messageBuilder.append("  - 그룹 수: ${result.groupCount}개\n\n")
        messageBuilder.append("단어 사용 현황:\n")
        messageBuilder.append("  - 전체 단어: ${result.totalWords}개\n")
        messageBuilder.append("  - 사용된 단어: ${result.usedWords}개\n")

        if (result.unusedWords > 0) {
            messageBuilder.append("  - 미사용 단어: ${result.unusedWords}개\n\n")
            messageBuilder.append("미사용 단어 목록:\n")
            result.unusedWordDetails.take(5).forEach { (_, word) ->
                messageBuilder.append("  - $word\n")
            }
            if (result.unusedWordDetails.size > 5) {
                messageBuilder.append("  ... 외 ${result.unusedWordDetails.size - 5}개\n")
            }
        } else {
            messageBuilder.append("  - 미사용 단어: 0개\n\n")
            messageBuilder.append("모든 단어가 문장에 사용되었습니다!")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("문장 생성 완료")
            .setMessage(messageBuilder.toString())
            .setPositiveButton("확인", null)
            .show()
    }

    private fun showError(message: String) {
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

    private fun navigateToUnknownWordList(batch: SentenceBatchInfo) {
        val bundle = Bundle().apply {
            putString("batchId", batch.batchId)
            putInt("batchNumber", batch.batchNumber)
        }
        findNavController().navigate(R.id.action_sentence_generation_to_unknown_word_list, bundle)
    }

    // 배치 어댑터
    private inner class BatchAdapter(
        private val batches: List<SentenceBatchInfo>,
        private val onViewClick: (SentenceBatchInfo) -> Unit,
        private val onDeleteClick: (SentenceBatchInfo) -> Unit,
        private val onUnknownWordsClick: (SentenceBatchInfo) -> Unit
    ) : RecyclerView.Adapter<BatchAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardBatch: View = itemView.findViewById(R.id.card_batch)
            val tvBatchTitle: TextView = itemView.findViewById(R.id.tv_batch_title)
            val tvBatchInfo: TextView = itemView.findViewById(R.id.tv_batch_info)
            val btnUnknownWords: View = itemView.findViewById(R.id.btn_unknown_words)
            val btnDelete: TextView = itemView.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sentence_batch, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val batch = batches[position]
            holder.tvBatchTitle.text = "${batch.batchNumber}회차"
            holder.tvBatchInfo.text = "${batch.sentenceCount}개 문장 · 일일 단어 ${batch.wordCount}개"

            // 모르는 단어 버튼 (있을 때만 표시)
            if (batch.unknownWordCount > 0) {
                holder.btnUnknownWords.visibility = View.VISIBLE
                holder.btnUnknownWords.setOnClickListener {
                    onUnknownWordsClick(batch)
                }
            } else {
                holder.btnUnknownWords.visibility = View.GONE
            }

            // 클릭 시 보기
            holder.cardBatch.setOnClickListener {
                onViewClick(batch)
            }

            // 삭제 버튼
            holder.btnDelete.setOnClickListener {
                onDeleteClick(batch)
            }
        }

        override fun getItemCount(): Int = batches.size
    }
}
