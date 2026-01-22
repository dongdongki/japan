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
import com.example.myapplication.databinding.FragmentWeakSentenceListBinding
import com.example.myapplication.model.Sentence

class WeakSentenceListFragment : Fragment() {

    private var _binding: FragmentWeakSentenceListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeakSentenceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the span count from resources
        val spanCount = resources.getInteger(R.integer.word_list_span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)

        // Get only weak sentences
        val weakSentences = viewModel.getWeakSentenceList()
        android.util.Log.d("WeakSentenceListFragment", "Weak sentences list size: ${weakSentences.size}")
        binding.recyclerView.adapter = SentenceListAdapter(weakSentences, viewModel)

        // Toggle meaning visibility button
        binding.btnToggleMeaning.setOnClickListener {
            val showing = viewModel.toggleMeaningVisibility()
            updateMeaningButtonText(showing)
        }

        // Clear all weak sentences button
        binding.btnClearAll.setOnClickListener {
            viewModel.clearAllWeakSentences()
        }

        // Observe ViewModel state and update adapter
        viewModel.showMeaning.observe(viewLifecycleOwner) { show ->
            (binding.recyclerView.adapter as? MeaningToggleable)?.setMeaningVisibility(show)
            updateMeaningButtonText(show)
        }

        // Start quiz button
        binding.btnStartQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_weak_sentence_list_to_weak_sentence_mode)
        }

        // Observe changes in weak sentences to refresh the list
        viewModel.weakSentences.observe(viewLifecycleOwner) {
            val updatedList = viewModel.getWeakSentenceList()
            (binding.recyclerView.adapter as? SentenceListAdapter)?.let {
                it.updateList(updatedList)
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
