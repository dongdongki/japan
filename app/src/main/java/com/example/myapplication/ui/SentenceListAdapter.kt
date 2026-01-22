package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemWordBinding
import com.example.myapplication.model.Sentence

class SentenceListAdapter(
    private var sentenceList: List<Sentence>,
    private val viewModel: QuizViewModel,
    private val batchId: String? = null
) :
    RecyclerView.Adapter<SentenceListAdapter.SentenceViewHolder>(), MeaningToggleable {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentenceViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SentenceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SentenceViewHolder, position: Int) {
        holder.bind(sentenceList[position])
    }

    override fun onBindViewHolder(holder: SentenceViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads.contains("MEANING_VISIBILITY")) {
                holder.updateMeaningVisibility(showMeaning)
            }
        }
    }

    override fun getItemCount() = sentenceList.size

    fun updateList(newList: List<Sentence>) {
        val diffCallback = SentenceDiffCallback(sentenceList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        sentenceList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class SentenceDiffCallback(
        private val oldList: List<Sentence>,
        private val newList: List<Sentence>
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

    inner class SentenceViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sentence: Sentence) {
            binding.tvWord.text = sentence.kanji
            binding.tvMeaning.text = sentence.meaning
            binding.tvMeaning.visibility = if (showMeaning) android.view.View.VISIBLE else android.view.View.GONE
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
                val navController = it.findNavController()
                val isWeak = navController.currentDestination?.id == R.id.weakSentenceListFragment

                val bundle = Bundle().apply {
                    putInt("sentenceId", sentence.id)
                    putBoolean("isSentence", true)
                    putBoolean("isWeakWords", isWeak)
                    batchId?.let { id -> putString("batchId", id) }
                }

                val action = if (isWeak) {
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
                    if (!show) binding.tvMeaning.visibility = android.view.View.GONE
                }
                .start()
        }
    }
}
