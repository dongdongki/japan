package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemWordBinding
import com.example.myapplication.model.Word

class WordListAdapter(
    private var wordList: List<Word>,
    private val viewModel: QuizViewModel
) :
    RecyclerView.Adapter<WordListAdapter.WordViewHolder>(), MeaningToggleable {

    private var showMeaning = true

    override fun toggleMeaning(): Boolean {
        showMeaning = !showMeaning
        notifyItemRangeChanged(0, itemCount, "MEANING_VISIBILITY")
        return showMeaning
    }

    override fun setMeaningVisibility(show: Boolean) {
        if (showMeaning != show) {
            showMeaning = show
            // Use notifyItemRangeChanged with payload to animate only the meaning TextView
            notifyItemRangeChanged(0, itemCount, "MEANING_VISIBILITY")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(wordList[position])
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Handle partial update with animation
            if (payloads.contains("MEANING_VISIBILITY")) {
                holder.updateMeaningVisibility(showMeaning)
            }
            if (payloads.contains("WEAK_WORD_CHANGED")) {
                holder.updateWeakWordState(wordList[position])
            }
        }
    }

    override fun getItemCount() = wordList.size

    fun updateList(newList: List<Word>) {
        val diffCallback = WordDiffCallback(wordList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        wordList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class WordViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        // Cache the current word to avoid re-setting listeners unnecessarily
        private var currentWord: Word? = null

        fun bind(word: Word) {
            binding.tvWord.text = word.kanji
            binding.tvMeaning.text = word.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.tvHiragana.text = word.hiragana

            // Only update checkbox if word changed or first bind
            if (currentWord?.id != word.id) {
                binding.cbWeakWord.setOnCheckedChangeListener(null)
                binding.cbWeakWord.isChecked = viewModel.isWeakWord(word)
                binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                    viewModel.toggleWeakWord(word)
                }

                binding.btnSpeak.setOnClickListener {
                    viewModel.speak(word.kanji)
                }

                itemView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt("wordId", word.id)
                        // Pass part of speech if viewing a filtered list
                        val quizType = viewModel.quizType.value
                        if (quizType in listOf("verb", "particle", "adjective", "adverb", "conjunction", "noun",
                                              "verbs", "particles", "adjectives", "adverbs", "conjunctions")) {
                            putString("partOfSpeech", word.partOfSpeech)
                        }
                    }
                    it.findNavController().navigate(R.id.action_word_list_to_writing_practice, bundle)
                }
                currentWord = word
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

        fun updateWeakWordState(word: Word) {
            binding.cbWeakWord.setOnCheckedChangeListener(null)
            binding.cbWeakWord.isChecked = viewModel.isWeakWord(word)
            binding.cbWeakWord.setOnCheckedChangeListener { _, _ ->
                viewModel.toggleWeakWord(word)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class WordDiffCallback(
        private val oldList: List<Word>,
        private val newList: List<Word>
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
            // Return a payload to enable partial binding
            return "PARTIAL_UPDATE"
        }
    }
}
