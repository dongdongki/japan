package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSongSelectionBinding
import com.example.myapplication.databinding.ItemSongBinding
import com.example.myapplication.model.SongInfo
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for selecting a song to study
 * Displays all available songs from assets directory
 */
@AndroidEntryPoint
class SongSelectionFragment : Fragment() {

    private var _binding: FragmentSongSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        val songs = viewModel.getSongList()
        val adapter = SongAdapter(
            songs = songs,
            onVocabClick = { song -> onSongSelected(song) },
            onLyricsClick = { song -> onLyricsSelected(song) }
        )

        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongs.adapter = adapter

        android.util.Log.d("SongSelectionFragment", "Displaying ${songs.size} songs")
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onSongSelected(song: SongInfo) {
        // Navigate to song word list with song directory as argument
        val bundle = Bundle().apply {
            putString("songDirectory", song.directoryName)
        }
        findNavController().navigate(R.id.action_song_selection_to_song_list, bundle)
    }

    private fun onLyricsSelected(song: SongInfo) {
        // Navigate to song lyrics view
        val bundle = Bundle().apply {
            putString("songDirectory", song.directoryName)
            putString("songTitle", song.displayTitle)
        }
        findNavController().navigate(R.id.action_song_selection_to_song_lyrics, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Adapter for displaying songs
     */
    private class SongAdapter(
        private val songs: List<SongInfo>,
        private val onVocabClick: (SongInfo) -> Unit,
        private val onLyricsClick: (SongInfo) -> Unit
    ) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
            val binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SongViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            holder.bind(songs[position])
        }

        override fun getItemCount() = songs.size

        inner class SongViewHolder(
            private val binding: ItemSongBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(song: SongInfo) {
                binding.tvSongTitle.text = song.displayTitle
                binding.tvVocabCount.text = "단어 수: ${song.vocabularyCount}개"

                // Show artist if available
                if (song.artist.isNotEmpty()) {
                    binding.tvArtist.text = song.artist
                    binding.tvArtist.visibility = View.VISIBLE
                } else {
                    binding.tvArtist.visibility = View.GONE
                }

                // Set button click listeners
                binding.btnViewVocab.setOnClickListener {
                    onVocabClick(song)
                }

                binding.btnViewLyrics.setOnClickListener {
                    onLyricsClick(song)
                }
            }
        }
    }
}
