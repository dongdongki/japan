package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWordCategoryBinding

class WordCategoryFragment : Fragment() {

    private var _binding: FragmentWordCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simplified navigation with helper function
        binding.btnNouns.setOnClickListener { navigateToWordList("noun") }
        binding.btnAdjectives.setOnClickListener { navigateToWordList("adjective") }
        binding.btnVerbs.setOnClickListener { navigateToWordList("verb") }
        binding.btnParticles.setOnClickListener { navigateToWordList("particle") }
        binding.btnAdverbs.setOnClickListener { navigateToWordList("adverb") }
        binding.btnConjunctions.setOnClickListener { navigateToWordList("conjunction") }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Helper function to navigate to word list with part of speech
     */
    private fun navigateToWordList(partOfSpeech: String) {
        viewModel.setQuizType(partOfSpeech)
        val bundle = Bundle().apply {
            putString("partOfSpeech", partOfSpeech)
        }
        findNavController().navigate(R.id.action_word_category_to_word_list, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
