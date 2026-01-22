package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSelectedKanaDisplayBinding
import com.example.myapplication.model.KanaCharacter

class SelectedKanaDisplayAdapter(
    private var kanaList: List<KanaCharacter>,
    private val onRemove: (KanaCharacter) -> Unit
) : RecyclerView.Adapter<SelectedKanaDisplayAdapter.KanaViewHolder>() {

    inner class KanaViewHolder(private val binding: ItemSelectedKanaDisplayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(kana: KanaCharacter) {
            binding.tvKana.text = kana.kana
            binding.tvRomaji.text = kana.romaji

            binding.btnRemove.setOnClickListener {
                onRemove(kana)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanaViewHolder {
        val binding = ItemSelectedKanaDisplayBinding.inflate(
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
