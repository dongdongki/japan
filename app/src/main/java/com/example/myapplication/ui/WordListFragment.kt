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
import com.example.myapplication.databinding.FragmentWordListBinding
import com.example.myapplication.model.Word

class WordListFragment : Fragment() {

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showOnlyWeakWords = arguments?.getBoolean("showOnlyWeakWords") ?: false
        val grammarType = arguments?.getString("grammarType")
        val partOfSpeech = arguments?.getString("partOfSpeech")

        // Determine which part of speech to show - simplified logic
        val finalPartOfSpeech = partOfSpeech
            ?: convertGrammarTypeToPartOfSpeech(grammarType)
            ?: viewModel.quizType.value

        // Set in ViewModel (unified method)
        finalPartOfSpeech?.let { viewModel.setQuizType(it) }

        // Get the span count from resources
        val spanCount = resources.getInteger(R.integer.word_list_span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)

        // Load word list once (store for later reuse)
        val currentWordList: List<Word>
        if (showOnlyWeakWords) {
            val allWeakWords = viewModel.getAllWeakWords()
            binding.recyclerView.adapter = MixedWordListAdapter(allWeakWords, viewModel)
            currentWordList = emptyList()
        } else {
            currentWordList = viewModel.getWordListForPartOfSpeech(finalPartOfSpeech)
            binding.recyclerView.adapter = WordListAdapter(currentWordList, viewModel)
        }

        // Toggle meaning visibility button
        binding.btnToggleMeaning.setOnClickListener {
            val showing = viewModel.toggleMeaningVisibility()
            binding.btnToggleMeaning.text = getString(if (showing) R.string.word_list_toggle_meaning_hide else R.string.word_list_toggle_meaning_show)
            binding.btnToggleMeaning.contentDescription = getString(if (showing) {
                R.string.word_list_toggle_meaning_desc_hide
            } else {
                R.string.word_list_toggle_meaning_desc_show
            })
        }

        // Observe ViewModel state and update adapter
        viewModel.showMeaning.observe(viewLifecycleOwner) { show ->
            (binding.recyclerView.adapter as? MeaningToggleable)?.setMeaningVisibility(show)
            binding.btnToggleMeaning.text = getString(if (show) R.string.word_list_toggle_meaning_hide else R.string.word_list_toggle_meaning_show)
            binding.btnToggleMeaning.contentDescription = getString(if (show) {
                R.string.word_list_toggle_meaning_desc_hide
            } else {
                R.string.word_list_toggle_meaning_desc_show
            })
        }

        // Show quiz buttons for word lists (not weak words)
        if (!showOnlyWeakWords && currentWordList.isNotEmpty()) {
            // Reuse the already loaded currentWordList
            binding.quizButtonContainer.visibility = View.VISIBLE

            // For part-of-speech based lists (noun, adjective, verb, particle)
            // we show a generic quiz button
            if (grammarType == null && finalPartOfSpeech != null) {
                    // Came from WordCategoryFragment (part of speech selection)
                    binding.btnStartQuiz.text = getString(R.string.btn_start_quiz)
                    binding.btnListeningQuiz.visibility = View.GONE

                    binding.btnStartQuiz.setOnClickListener {
                        // Navigate to appropriate mode selection based on part of speech
                        when (finalPartOfSpeech) {
                            "verb" -> findNavController().navigate(R.id.action_word_list_to_verb_mode)
                            "particle" -> findNavController().navigate(R.id.action_word_list_to_particle_mode)
                            else -> findNavController().navigate(R.id.action_word_list_to_word_mode)
                        }
                    }
                } else {
                    // Legacy grammar type navigation (from QuizGrammarCategoryFragment)
                    when (grammarType) {
                        "words" -> {
                            // Regular words
                            binding.btnStartQuiz.text = getString(R.string.btn_start_quiz)
                            binding.btnListeningQuiz.visibility = View.VISIBLE

                            binding.btnStartQuiz.setOnClickListener {
                                findNavController().navigate(R.id.action_word_list_to_word_mode)
                            }

                            binding.btnListeningQuiz.setOnClickListener {
                                viewModel.startWordQuiz(quizMode = "listening", onlyWeakWords = false)
                                findNavController().navigate(R.id.action_word_list_to_quiz)
                            }
                        }
                        "verbs" -> {
                            // Verbs
                            binding.btnStartQuiz.text = getString(R.string.btn_start_quiz)
                            binding.btnListeningQuiz.visibility = View.GONE

                            binding.btnStartQuiz.setOnClickListener {
                                findNavController().navigate(R.id.action_word_list_to_verb_mode)
                            }
                        }
                        "particles" -> {
                            // Particles
                            binding.btnStartQuiz.text = getString(R.string.btn_start_quiz)
                            binding.btnListeningQuiz.visibility = View.GONE

                            binding.btnStartQuiz.setOnClickListener {
                                findNavController().navigate(R.id.action_word_list_to_particle_mode)
                            }
                        }
                    }
                }
        } else if (showOnlyWeakWords) {
            // For weak words list, show only one quiz button
            binding.quizButtonContainer.visibility = View.VISIBLE
            binding.btnListeningQuiz.visibility = View.GONE
            binding.btnStartQuiz.text = getString(R.string.word_list_quiz_selected)

            binding.btnStartQuiz.setOnClickListener {
                findNavController().navigate(R.id.action_word_list_to_weak_word_mode)
            }
        }

        // Observe changes in weak words to refresh the list if needed
        viewModel.weakWords.observe(viewLifecycleOwner) {
            if (showOnlyWeakWords) {
                val updatedList = viewModel.getAllWeakWords()
                (binding.recyclerView.adapter as? MixedWordListAdapter)?.let {
                    it.updateList(updatedList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Helper function to convert legacy grammarType to partOfSpeech
     */
    private fun convertGrammarTypeToPartOfSpeech(grammarType: String?): String? {
        return when (grammarType) {
            "verbs" -> "verb"
            "particles" -> "particle"
            "adjectives" -> "adjective"
            "adverbs" -> "adverb"
            "conjunctions" -> "conjunction"
            "words" -> "noun"
            else -> null
        }
    }
}