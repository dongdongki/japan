package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentWritingPracticeBinding
import com.example.myapplication.model.Word
import com.example.myapplication.model.Song

class WritingPracticeFragment : Fragment() {

    private var _binding: FragmentWritingPracticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    private val navigator = WritingPracticeNavigator()
    private var currentWordId: Int = -1
    private var isTextHidden: Boolean = false
    private var isMeaningHidden: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeNavigator()
        updateUI(currentWordId)
        setupObservers()
        setupClickListeners()
    }

    private fun initializeNavigator() {
        val isSongMode = arguments?.getBoolean("isSong") ?: false
        val isSentenceMode = arguments?.getBoolean("isSentence") ?: false
        val isWeakWords = arguments?.getBoolean("isWeakWords") ?: false
        val partOfSpeech = arguments?.getString("partOfSpeech")
        val batchId = arguments?.getString("batchId")

        android.util.Log.d("WritingPractice", "Arguments: isSong=$isSongMode, isSentence=$isSentenceMode, isWeakWords=$isWeakWords, partOfSpeech=$partOfSpeech, batchId=$batchId")

        when {
            isSongMode && isWeakWords -> {
                val songId = arguments?.getInt("songId") ?: -1
                currentWordId = 10000 + songId
                val mixedList = viewModel.getAllWeakWords().filter { it is Word || it is Song }
                navigator.setMixedMode(mixedList)
                android.util.Log.d("WritingPractice", "Song from weak words: mixedList.size=${mixedList.size}")
            }
            isSongMode -> {
                val songId = arguments?.getInt("songId") ?: -1
                currentWordId = 10000 + songId
                val songList = viewModel.getSongVocabulary(viewModel.currentSongDirectory)
                navigator.setSongMode(songList)
                android.util.Log.d("WritingPractice", "Song mode: songList.size=${songList.size}")
            }
            isSentenceMode -> {
                currentWordId = arguments?.getInt("sentenceId") ?: -1
                val sentenceList = when {
                    batchId != null -> viewModel.getSentencesByBatchId(batchId)
                    isWeakWords -> viewModel.getWeakSentenceList()
                    else -> viewModel.getSentenceList()
                }
                navigator.setSentenceMode(sentenceList)
                android.util.Log.d("WritingPractice", "Sentence mode: sentenceList.size=${sentenceList.size}, batchId=$batchId")
            }
            isWeakWords -> {
                currentWordId = arguments?.getInt("wordId") ?: -1
                val mixedList = viewModel.getAllWeakWords().filter { it is Word || it is Song }
                navigator.setMixedMode(mixedList)
                android.util.Log.d("WritingPractice", "Weak words mode: mixedList.size=${mixedList.size}")
            }
            else -> {
                currentWordId = arguments?.getInt("wordId") ?: -1
                val wordList = if (partOfSpeech != null) {
                    viewModel.getWordListForPartOfSpeech(partOfSpeech)
                } else {
                    viewModel.getWordListForPartOfSpeech(viewModel.quizType.value)
                }
                navigator.setWordMode(wordList)
                android.util.Log.d("WritingPractice", "Word mode: wordList.size=${wordList.size}")
            }
        }
    }

    private fun setupObservers() {
        viewModel.penWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setPenWidth(width)
        }
        viewModel.eraserWidth.observe(viewLifecycleOwner) { width ->
            binding.writingView.setEraserWidth(width)
        }
    }

    private fun setupClickListeners() {
        binding.clearButton.setOnClickListener {
            binding.writingView.clear()
        }

        binding.toggleEraser.setOnCheckedChangeListener { _, isChecked ->
            binding.writingView.setToolType(if (isChecked) WritingView.ToolType.ERASER else WritingView.ToolType.PEN)
        }

        binding.btnSpeakWriting.setOnClickListener {
            val textToSpeak = binding.wordToPractice.text.toString()
            if (textToSpeak.isNotEmpty()) {
                viewModel.speak(textToSpeak)
            }
        }

        binding.btnToggleText.setOnClickListener {
            isTextHidden = !isTextHidden
            updateVisibility()
        }

        binding.btnToggleMeaning.setOnClickListener {
            isMeaningHidden = !isMeaningHidden
            updateVisibility()
        }

        binding.btnPrevious.setOnClickListener {
            navigator.getPreviousId(currentWordId)?.let {
                currentWordId = it
                updateUI(currentWordId)
            }
        }

        binding.btnNext.setOnClickListener {
            navigator.getNextId(currentWordId)?.let {
                currentWordId = it
                updateUI(currentWordId)
            }
        }
    }

    private fun updateUI(id: Int) {
        if (id == -1) return

        navigator.getItemData(id)?.let { (kanji, meaning, hiragana) ->
            binding.wordToPractice.text = kanji
            binding.tvMeaning.text = meaning
            binding.tvHiragana.text = hiragana
            binding.writingView.clear()
        }

        binding.btnPrevious.isEnabled = true
        binding.btnNext.isEnabled = true

        updateVisibility()
    }

    private fun updateVisibility() {
        if (isTextHidden) {
            binding.wordToPractice.visibility = View.INVISIBLE
            binding.tvHiragana.visibility = View.INVISIBLE
        } else {
            binding.wordToPractice.visibility = View.VISIBLE
            binding.tvHiragana.visibility = View.VISIBLE
        }

        if (isMeaningHidden) {
            binding.tvMeaning.visibility = View.INVISIBLE
        } else {
            binding.tvMeaning.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
