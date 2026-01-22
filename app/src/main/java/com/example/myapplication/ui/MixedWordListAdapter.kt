package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemWordBinding
import com.example.myapplication.model.Sentence
import com.example.myapplication.model.Song
import com.example.myapplication.model.Word

class MixedWordListAdapter(
    private var itemList: List<Any>,
    private val viewModel: QuizViewModel
) :
    RecyclerView.Adapter<MixedWordListAdapter.ItemViewHolder>(), MeaningToggleable {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads.contains("MEANING_VISIBILITY")) {
                holder.updateMeaningVisibility(showMeaning)
            }
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<Any>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Any) {
            when (item) {
                is Word -> bindWord(item)
                is Song -> bindSong(item)
                is Sentence -> bindSentence(item)
                else -> {
                    android.util.Log.w("MixedWordListAdapter", "Unknown item type: ${item::class.java.simpleName}")
                    binding.tvWord.text = "???"
                    binding.tvMeaning.text = "Unknown type"
                    binding.tvHiragana.text = ""
                }
            }
        }

        private fun bindWord(word: Word) {
            binding.tvWord.text = word.kanji
            binding.tvMeaning.text = word.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.tvHiragana.text = word.hiragana

            binding.cbWeakWord.setOnCheckedChangeListener(null)
            binding.cbWeakWord.isChecked = viewModel.isWeakWord(word)

            binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                viewModel.toggleWeakWord(word)
            }

            binding.btnSpeak.setOnClickListener {
                viewModel.speak(word.kanji)
            }

            itemView.setOnClickListener {
                val navController = it.findNavController()
                val isWeak = navController.currentDestination?.id == R.id.weakWordListFragment

                val bundle = Bundle().apply {
                    putInt("wordId", word.id)
                    putBoolean("isWeakWords", isWeak)
                    // Don't pass partOfSpeech for weak words - it's a unified list
                    // Only pass partOfSpeech when NOT from weak words list
                    if (!isWeak) {
                        putString("partOfSpeech", word.partOfSpeech)
                    }
                }

                val action = if (isWeak) {
                    R.id.action_weak_word_list_to_writing_practice
                } else {
                    R.id.action_word_list_to_writing_practice
                }
                navController.navigate(action, bundle)
            }
        }

        private fun bindSong(song: Song) {
            binding.tvWord.text = song.kanji
            binding.tvMeaning.text = song.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.tvHiragana.text = song.hiragana

            val uniqueId = 10000 + song.id

            binding.cbWeakWord.setOnCheckedChangeListener(null)
            binding.cbWeakWord.isChecked = viewModel.isWeakWord(uniqueId)

            binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                viewModel.toggleWeakWord(uniqueId)
            }

            binding.btnSpeak.setOnClickListener {
                viewModel.speak(song.kanji)
            }

            itemView.setOnClickListener {
                val navController = it.findNavController()
                val isWeak = navController.currentDestination?.id == R.id.weakWordListFragment

                val bundle = Bundle().apply {
                    putInt("songId", song.id)
                    putBoolean("isSong", true)
                    putBoolean("isWeakWords", isWeak)
                }

                val action = if (isWeak) {
                    R.id.action_weak_word_list_to_writing_practice
                } else {
                    R.id.action_word_list_to_writing_practice
                }
                navController.navigate(action, bundle)
            }
        }

        private fun bindSentence(sentence: Sentence) {
            binding.tvWord.text = sentence.kanji
            binding.tvMeaning.text = sentence.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.tvHiragana.text = sentence.hiragana

            binding.cbWeakWord.setOnCheckedChangeListener(null)
            binding.cbWeakWord.isChecked = viewModel.isWeakSentence(sentence)

            binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                viewModel.toggleWeakSentence(sentence)
            }

            binding.btnSpeak.setOnClickListener {
                viewModel.speak(sentence.kanji)
            }

            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("sentenceId", sentence.id)
                    putBoolean("isSentence", true)
                }
                val navController = it.findNavController()
                val action = if (navController.currentDestination?.id == R.id.weakSentenceListFragment) {
                    R.id.action_weak_sentence_list_to_writing_practice
                } else {
                    R.id.action_sentence_list_to_writing_practice
                }
                navController.navigate(action, bundle)
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
                    if (!show) binding.tvMeaning.visibility = android.view.View.INVISIBLE
                }
                .start()
        }
    }
}
