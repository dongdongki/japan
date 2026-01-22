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
import com.example.myapplication.databinding.FragmentWeakWordListBinding

class WeakWordListFragment : Fragment() {

    private var _binding: FragmentWeakWordListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeakWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the span count from resources
        val spanCount = resources.getInteger(R.integer.word_list_span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)

        // Get only weak words (Word and Song types, excluding Sentence)
        val weakWords = viewModel.getAllWeakWords().filter {
            it is com.example.myapplication.model.Word || it is com.example.myapplication.model.Song
        }
        android.util.Log.d("WeakWordListFragment", "Weak words list size: ${weakWords.size}")
        binding.recyclerView.adapter = MixedWordListAdapter(weakWords, viewModel)

        // Toggle meaning visibility button
        binding.btnToggleMeaning.setOnClickListener {
            val showing = viewModel.toggleMeaningVisibility()
            updateMeaningButtonText(showing)
        }

        // Clear all weak words button
        binding.btnClearAll.setOnClickListener {
            viewModel.clearAllWeakWords()
        }

        // Observe ViewModel state and update adapter
        viewModel.showMeaning.observe(viewLifecycleOwner) { show ->
            (binding.recyclerView.adapter as? MeaningToggleable)?.setMeaningVisibility(show)
            updateMeaningButtonText(show)
        }

        // Start quiz button
        binding.btnStartQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_weak_word_list_to_weak_word_mode)
        }

        // Observe changes in weak words to refresh the list
        viewModel.weakWords.observe(viewLifecycleOwner) {
            val updatedList = viewModel.getAllWeakWords().filter {
                it is com.example.myapplication.model.Word || it is com.example.myapplication.model.Song
            }
            (binding.recyclerView.adapter as? MixedWordListAdapter)?.let {
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
