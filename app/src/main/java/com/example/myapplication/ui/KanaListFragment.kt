package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentKanaListBinding
import com.example.myapplication.databinding.ItemKanaBinding
import com.example.myapplication.model.KanaCharacter

class KanaListFragment : Fragment() {

    private var _binding: FragmentKanaListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupedKana = viewModel.getGroupedKanaList()

        // Create views for each group
        groupedKana.forEach { (rowName, kanaList) ->
            // Add row header
            val headerView = TextView(context).apply {
                text = rowName
                textSize = 18f
                setTextColor(resources.getColor(R.color.white, null))
                setPadding(16, 24, 16, 8)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.kanaContainer.addView(headerView)

            // Add grid for kana characters
            val gridLayout = GridLayout(context).apply {
                columnCount = if (kanaList.size == 3) 3 else 5
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            kanaList.forEach { kana ->
                val kanaBinding = ItemKanaBinding.inflate(layoutInflater, gridLayout, false)
                kanaBinding.tvKana.text = kana.kana
                kanaBinding.tvRomanization.text = kana.romaji
                kanaBinding.tvKorean.text = kana.kor

                kanaBinding.btnSpeakKana.setOnClickListener {
                    viewModel.speak(kana.kana)
                }

                kanaBinding.root.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("kana", kana.kana)
                        putString("romanization", kana.romaji)
                        putString("korean", kana.kor)
                    }
                    findNavController().navigate(R.id.action_kana_list_to_kana_writing, bundle)
                }

                val layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                kanaBinding.root.layoutParams = layoutParams

                gridLayout.addView(kanaBinding.root)
            }

            binding.kanaContainer.addView(gridLayout)
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
