package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemWordBinding
import com.example.myapplication.model.Song
import com.example.myapplication.util.Constants

class SongListAdapter(
    private var songList: List<Song>,
    private val viewModel: QuizViewModel
) :
    RecyclerView.Adapter<SongListAdapter.SongViewHolder>(), MeaningToggleable {

    private var showMeaning = true

    override fun toggleMeaning(): Boolean {
        showMeaning = !showMeaning
        notifyDataSetChanged()
        return showMeaning
    }

    override fun setMeaningVisibility(show: Boolean) {
        if (showMeaning != show) {
            showMeaning = show
            notifyItemRangeChanged(0, itemCount, "MEANING_VISIBILITY")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position])
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads.contains("MEANING_VISIBILITY")) {
                holder.updateMeaningVisibility(showMeaning)
            }
        }
    }

    override fun getItemCount() = songList.size

    fun updateList(newList: List<Song>) {
        val diffCallback = SongDiffCallback(songList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        songList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class SongDiffCallback(
        private val oldList: List<Song>,
        private val newList: List<Song>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return old.kanji == new.kanji &&
                   old.meaning == new.meaning &&
                   old.hiragana == new.hiragana
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return "PARTIAL_UPDATE"
        }
    }

    inner class SongViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.tvWord.text = song.kanji
            binding.tvMeaning.text = song.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.GONE
            binding.tvHiragana.text = song.hiragana

            // Show weak word checkbox for song vocabulary
            // Use a unique ID offset to distinguish song words from regular words
            val uniqueId = Constants.SONG_ID_OFFSET + song.id

            binding.cbWeakWord.setOnCheckedChangeListener(null)
            binding.cbWeakWord.isChecked = viewModel.isWeakWord(uniqueId)

            binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                viewModel.toggleWeakWord(uniqueId)
            }

            binding.btnSpeak.setOnClickListener {
                viewModel.speak(song.kanji)
            }

            // Navigate to writing practice when clicking on song word
            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("songId", song.id)
                    putBoolean("isSong", true)
                }
                it.findNavController().navigate(R.id.action_song_list_to_song_writing_practice, bundle)
            }
        }

        fun updateMeaningVisibility(show: Boolean) {
            binding.tvMeaning.animate()
                .alpha(if (show) 1f else 0f)
                .setDuration(200)
                .withStartAction {
                    if (show) binding.tvMeaning.visibility = android.view.View.VISIBLE
                }
                .withEndAction {
                    if (!show) binding.tvMeaning.visibility = android.view.View.GONE
                }
                .start()
        }
    }
}
