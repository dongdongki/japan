package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemRowSelectionBinding

class RowSelectionAdapter(
    private val rows: List<String>,
    private val onRowSelected: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RowSelectionAdapter.RowViewHolder>() {

    private val selectedRows = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemRowSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val rowName = rows[position]
        holder.bind(rowName, selectedRows.contains(rowName)) {
            if (it) {
                selectedRows.add(rowName)
            } else {
                selectedRows.remove(rowName)
            }
            onRowSelected(rowName, it)
        }
    }

    override fun getItemCount() = rows.size

    inner class RowViewHolder(private val binding: ItemRowSelectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rowName: String, isChecked: Boolean, onChecked: (Boolean) -> Unit) {
            binding.checkboxRow.text = rowName
            binding.checkboxRow.isChecked = isChecked
            binding.checkboxRow.setOnCheckedChangeListener { _, newCheckedState ->
                onChecked(newCheckedState)
            }
        }
    }
}
