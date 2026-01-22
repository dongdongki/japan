package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSelectedKanaBinding
import com.example.myapplication.model.KanaCharacter

class SelectedKanaAdapter(
    private var kanaList: List<KanaCharacter>,
    private val isKanaSelected: (KanaCharacter) -> Boolean,
    private val onToggle: (KanaCharacter) -> Unit
) : RecyclerView.Adapter<SelectedKanaAdapter.KanaViewHolder>() {

    inner class KanaViewHolder(private val binding: ItemSelectedKanaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(kana: KanaCharacter) {
            binding.tvKana.text = kana.kana
            binding.tvRomaji.text = kana.romaji

            // Update checkbox state
            binding.checkboxKana.setOnCheckedChangeListener(null)
            binding.checkboxKana.isChecked = isKanaSelected(kana)

            // Handle checkbox click
            binding.checkboxKana.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != isKanaSelected(kana)) {
                    onToggle(kana)
                }
            }

            // Handle card click to toggle selection
            binding.cardKana.setOnClickListener {
                onToggle(kana)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanaViewHolder {
        val binding = ItemSelectedKanaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KanaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KanaViewHolder, position: Int) {
        holder.bind(kanaList[position])
    }

    override fun getItemCount(): Int = kanaList.size

    fun updateList(newList: List<KanaCharacter>) {
        kanaList = newList
        notifyDataSetChanged()
    }
}
