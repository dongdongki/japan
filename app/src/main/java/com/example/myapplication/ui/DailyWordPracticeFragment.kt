package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.JapaneseStudyApp
import com.example.myapplication.databinding.FragmentDailyWordPracticeBinding
import com.example.myapplication.model.DailyWord
import com.example.myapplication.repository.DailyWordRepository

class DailyWordPracticeFragment : Fragment() {

    private var _binding: FragmentDailyWordPracticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private lateinit var repository: DailyWordRepository

    private var wordList: List<DailyWord> = emptyList()
    private var currentIndex = 0

    private var isTextHidden = false
    private var isReadingHidden = false
    private var isMeaningHidden = false
    private val weakWords = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyWordPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = DailyWordRepository(requireContext())
        loadWeakWords()

        // Get arguments - support both day/wordIndex and wordId
        val wordId = arguments?.getInt("wordId", -1) ?: -1

        if (wordId != -1) {
            // Coming from weak word list - show all weak words
            val allWords = repository.getAllWords()
            wordList = allWords.filter { it.id in weakWords }
            // Find index of clicked word
            currentIndex = wordList.indexOfFirst { it.id == wordId }.coerceAtLeast(0)
        } else {
            // Coming from day list
            val day = arguments?.getInt("day") ?: 1
            val wordIndex = arguments?.getInt("wordIndex") ?: 0
            wordList = repository.getWordsForDay(day)
            currentIndex = wordIndex
        }

        // Setup pen/eraser width from ViewModel
        viewModel.penWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setPenWidth(width)
        }
        viewModel.eraserWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setEraserWidth(width)
        }

        setupListeners()
        displayCurrentWord()
    }

    private fun setupListeners() {
        binding.btnSpeak.setOnClickListener {
            if (wordList.isNotEmpty()) {
                JapaneseStudyApp.speak(wordList[currentIndex].reading)
            }
        }

        binding.btnPrevious.setOnClickListener {
            currentIndex = if (currentIndex > 0) {
                currentIndex - 1
            } else {
                wordList.size - 1  // Wrap to last
            }
            displayCurrentWord()
            binding.writingView.clear()
        }

        binding.btnNext.setOnClickListener {
            currentIndex = if (currentIndex < wordList.size - 1) {
                currentIndex + 1
            } else {
                0  // Wrap to first
            }
            displayCurrentWord()
            binding.writingView.clear()
        }

        binding.btnClear.setOnClickListener {
            binding.writingView.clear()
        }

        // Toggle text (word + example_jp)
        binding.btnToggleText.setOnClickListener {
            isTextHidden = !isTextHidden
            updateVisibility()
        }

        // Toggle reading
        binding.btnToggleReading.setOnClickListener {
            isReadingHidden = !isReadingHidden
            updateVisibility()
        }

        // Toggle meaning (meaning + example_kr)
        binding.btnToggleMeaning.setOnClickListener {
            isMeaningHidden = !isMeaningHidden
            updateVisibility()
        }

        // Weak word checkbox
        binding.cbWeak.setOnCheckedChangeListener { _, isChecked ->
            if (wordList.isEmpty()) return@setOnCheckedChangeListener
            val wordId = wordList[currentIndex].id
            if (isChecked) {
                weakWords.add(wordId)
            } else {
                weakWords.remove(wordId)
            }
            saveWeakWords()
        }
    }

    private fun loadWeakWords() {
        val prefs = requireContext().getSharedPreferences(DailyWordDaySelectionFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val savedWeakWords = prefs.getStringSet(DailyWordDaySelectionFragment.KEY_WEAK_WORDS, emptySet()) ?: emptySet()
        weakWords.clear()
        weakWords.addAll(savedWeakWords.map { it.toInt() })
    }

    private fun saveWeakWords() {
        val prefs = requireContext().getSharedPreferences(DailyWordDaySelectionFragment.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(DailyWordDaySelectionFragment.KEY_WEAK_WORDS, weakWords.map { it.toString() }.toSet()).apply()
    }

    private fun displayCurrentWord() {
        if (wordList.isEmpty()) return

        val word = wordList[currentIndex]

        binding.tvWord.text = word.word
        binding.tvReading.text = word.reading
        binding.tvMeaning.text = word.meaning
        binding.tvExampleJp.text = word.exampleJp
        binding.tvExampleKr.text = word.exampleKr

        binding.tvWordCount.text = "${currentIndex + 1} / ${wordList.size}"

        // Update weak checkbox
        binding.cbWeak.setOnCheckedChangeListener(null)
        binding.cbWeak.isChecked = weakWords.contains(word.id)
        binding.cbWeak.setOnCheckedChangeListener { _, isChecked ->
            val wordId = wordList[currentIndex].id
            if (isChecked) {
                weakWords.add(wordId)
            } else {
                weakWords.remove(wordId)
            }
            saveWeakWords()
        }

        // Buttons always enabled for circular navigation
        binding.btnPrevious.isEnabled = true
        binding.btnNext.isEnabled = true

        updateVisibility()
    }

    private fun updateVisibility() {
        // Use alpha instead of visibility to keep positions fixed
        val hiddenAlpha = 0f
        val visibleAlpha = 1f

        // Text hidden: hide word and example_jp (Japanese)
        val textAlpha = if (isTextHidden) hiddenAlpha else visibleAlpha
        binding.tvWord.alpha = textAlpha
        binding.tvExampleJp.alpha = textAlpha

        // Reading hidden
        val readingAlpha = if (isReadingHidden) hiddenAlpha else visibleAlpha
        binding.tvReading.alpha = readingAlpha

        // Meaning hidden: hide meaning and example_kr (Korean)
        val meaningAlpha = if (isMeaningHidden) hiddenAlpha else visibleAlpha
        binding.tvMeaning.alpha = meaningAlpha
        binding.tvExampleKr.alpha = meaningAlpha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
