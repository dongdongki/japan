package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentQuizGrammarCategoryBinding

class QuizGrammarCategoryFragment : Fragment() {

    private var _binding: FragmentQuizGrammarCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizGrammarCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 동사 카드 클릭 -> 동사 단어장으로 이동
        binding.btnVerbs.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("showOnlyWeakWords", false)
                putString("grammarType", "verbs")
            }
            findNavController().navigate(R.id.action_quiz_grammar_category_to_word_list, bundle)
        }

        // 조사 카드 클릭 -> 조사 단어장으로 이동
        binding.btnParticles.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("showOnlyWeakWords", false)
                putString("grammarType", "particles")
            }
            findNavController().navigate(R.id.action_quiz_grammar_category_to_word_list, bundle)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
