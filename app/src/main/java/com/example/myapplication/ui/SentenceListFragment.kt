package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSentenceListBinding

class SentenceListFragment : Fragment() {

    private var _binding: FragmentSentenceListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSentenceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showOnlyWeakSentences = arguments?.getBoolean("showOnlyWeakSentences") ?: false
        val batchId = arguments?.getString("batchId")
        val batchNumber = arguments?.getInt("batchNumber", -1) ?: -1

        android.util.Log.d("SentenceListFragment", "onViewCreated - showOnlyWeakSentences=$showOnlyWeakSentences, batchId=$batchId, batchNumber=$batchNumber")

        // Get the span count from resources (use sentence-specific span count)
        val spanCount = resources.getInteger(R.integer.sentence_list_span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)

        if (batchId != null) {
            // Show batch-specific sentences by batchId
            val batchSentences = viewModel.getSentencesByBatchId(batchId)
            android.util.Log.d("SentenceListFragment", "Batch sentences list size: ${batchSentences.size} (batchId=$batchId)")
            binding.recyclerView.adapter = SentenceListAdapter(batchSentences, viewModel, batchId)
        } else if (showOnlyWeakSentences) {
            // Show only weak sentences
            val weakSentences = viewModel.getWeakSentenceList()
            android.util.Log.d("SentenceListFragment", "Weak sentences list size: ${weakSentences.size}")
            binding.recyclerView.adapter = SentenceListAdapter(weakSentences, viewModel)
        } else {
            // Show all sentences
            val sentenceList = viewModel.getSentenceList()
            android.util.Log.d("SentenceListFragment", "Sentence list size: ${sentenceList.size}")
            sentenceList.take(3).forEach { sentence ->
                android.util.Log.d("SentenceListFragment", "Sample sentence: ${sentence.kanji} - ${sentence.meaning}")
            }
            binding.recyclerView.adapter = SentenceListAdapter(sentenceList, viewModel)
        }

        // Toggle meaning visibility button
        binding.btnToggleMeaning.setOnClickListener {
            val showing = viewModel.toggleMeaningVisibility()
            updateMeaningButtonText(showing)
        }

        // Observe ViewModel state and update adapter
        viewModel.showMeaning.observe(viewLifecycleOwner) { show ->
            (binding.recyclerView.adapter as? MeaningToggleable)?.setMeaningVisibility(show)
            updateMeaningButtonText(show)
        }

        // Show quiz buttons for sentence lists
        if (!showOnlyWeakSentences) {
            binding.quizButtonContainer.visibility = View.VISIBLE
            binding.btnStartQuiz.text = getString(R.string.btn_start_quiz)
            binding.btnListeningQuiz.visibility = View.VISIBLE

            binding.btnStartQuiz.setOnClickListener {
                findNavController().navigate(R.id.action_sentence_list_to_sentence_mode)
            }

            binding.btnListeningQuiz.setOnClickListener {
                viewModel.startSentenceQuiz(mode = "listening", useWeakSentences = false)
                findNavController().navigate(R.id.action_sentence_list_to_quiz)
            }
        } else {
            // For weak sentences list, show only one quiz button
            binding.quizButtonContainer.visibility = View.VISIBLE
            binding.btnListeningQuiz.visibility = View.GONE
            binding.btnStartQuiz.text = getString(R.string.sentence_list_quiz_selected)

            binding.btnStartQuiz.setOnClickListener {
                findNavController().navigate(R.id.action_sentence_list_to_weak_sentence_mode)
            }
        }

        // Observe changes in weak sentences to refresh the list if needed
        viewModel.weakSentences.observe(viewLifecycleOwner) {
            if (showOnlyWeakSentences) {
                val updatedList = viewModel.getWeakSentenceList()
                (binding.recyclerView.adapter as? SentenceListAdapter)?.updateList(updatedList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateMeaningButtonText(show: Boolean) {
        binding.btnToggleMeaning.text = getString(
            if (show) R.string.word_list_toggle_meaning_hide
            else R.string.word_list_toggle_meaning_show
        )
        binding.btnToggleMeaning.contentDescription = getString(
            if (show) R.string.word_list_toggle_meaning_desc_hide
            else R.string.word_list_toggle_meaning_desc_show
        )
    }
}
