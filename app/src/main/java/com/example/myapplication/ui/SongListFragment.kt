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
import com.example.myapplication.databinding.FragmentSongListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongListFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get song directory from arguments (defaults to "pretender" for backward compatibility)
        val songDirectory = arguments?.getString("songDirectory") ?: "pretender"

        // Store current song directory in ViewModel for quiz/writing modes
        viewModel.currentSongDirectory = songDirectory

        val songList = viewModel.getSongVocabulary(songDirectory)

        android.util.Log.d("SongListFragment", "Song list size: ${songList.size}")

        // Get the span count from resources
        val spanCount = resources.getInteger(R.integer.word_list_span_count)

        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.adapter = SongListAdapter(songList, viewModel)

        // Toggle meaning visibility button
        binding.btnToggleMeaning.setOnClickListener {
            val showing = viewModel.toggleMeaningVisibility()
            binding.btnToggleMeaning.text = if (showing) "한국어 뜻 숨기기" else "한국어 뜻 보이기"
            binding.btnToggleMeaning.contentDescription = if (showing) {
                "한국어 뜻을 숨깁니다"
            } else {
                "한국어 뜻을 보이게 합니다"
            }
        }

        // Observe ViewModel state and update adapter
        viewModel.showMeaning.observe(viewLifecycleOwner) { show ->
            (binding.recyclerView.adapter as? MeaningToggleable)?.setMeaningVisibility(show)
            binding.btnToggleMeaning.text = if (show) "한국어 뜻 숨기기" else "한국어 뜻 보이기"
            binding.btnToggleMeaning.contentDescription = if (show) {
                "한국어 뜻을 숨깁니다"
            } else {
                "한국어 뜻을 보이게 합니다"
            }
        }

        binding.btnStartQuiz.setOnClickListener {
            // Navigate to song quiz mode selection screen
            findNavController().navigate(R.id.action_song_list_to_song_quiz_mode)
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
