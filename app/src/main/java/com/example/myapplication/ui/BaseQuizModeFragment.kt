package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R

/**
 * Base class for quiz mode selection fragments
 * Eliminates code duplication across QuizWordModeFragment, SongQuizModeFragment, QuizSentenceModeFragment
 */
abstract class BaseQuizModeFragment : Fragment() {

    protected val viewModel: QuizViewModel by activityViewModels()

    // Abstract properties to be implemented by subclasses
    abstract val titleText: String
    abstract val option1Text: String
    abstract val option2Text: String

    // Navigation actions - to be overridden by subclasses
    abstract val navActionToQuiz: Int
    abstract val navActionToWritingTest: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_mode_common, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set text labels
        view.findViewById<android.widget.TextView>(R.id.title)?.text = titleText
        view.findViewById<android.widget.TextView>(R.id.option1_title)?.text = option1Text
        view.findViewById<android.widget.TextView>(R.id.option2_title)?.text = option2Text

        val btnOption1 = view.findViewById<View>(R.id.btn_option1)
        val btnOption2 = view.findViewById<View>(R.id.btn_option2)
        val btnWritingTest = view.findViewById<View>(R.id.btn_writing_test)
        val btnBack = view.findViewById<View>(R.id.btn_back)

        // Option 1 - reverse mode (meaning to kanji/word, multiple choice)
        btnOption1?.setOnClickListener {
            onOption1Click()
            findNavController().navigate(navActionToQuiz)
        }

        // Option 2 - normal mode (kanji/word to meaning, subjective)
        btnOption2?.setOnClickListener {
            onOption2Click()
            findNavController().navigate(navActionToQuiz)
        }

        // Writing test
        btnWritingTest?.setOnClickListener {
            onWritingTestClick()
            findNavController().navigate(navActionToWritingTest)
        }

        // Back button
        btnBack?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Abstract methods to be implemented by subclasses
    abstract fun onOption1Click()
    abstract fun onOption2Click()
    abstract fun onWritingTestClick()
}
