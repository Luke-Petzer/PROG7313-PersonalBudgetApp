package com.example.st10298850_prog7313_p2_lp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.st10298850_prog7313_p2_lp.data.CategoryTotal
import com.example.st10298850_prog7313_p2_lp.databinding.ItemCategoryTotalBinding

class CategoryTotalAdapter : ListAdapter<CategoryTotal, CategoryTotalAdapter.ViewHolder>(CategoryTotalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTotalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemCategoryTotalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryTotal: CategoryTotal) {
            binding.tvCategoryName.text = categoryTotal.categoryName
            binding.tvTransactionCount.text = "${categoryTotal.transactionCount} transactions"
            binding.tvTotalAmount.text = String.format("R%.2f", categoryTotal.totalAmount)
        }
    }

    class CategoryTotalDiffCallback : DiffUtil.ItemCallback<CategoryTotal>() {
        override fun areItemsTheSame(oldItem: CategoryTotal, newItem: CategoryTotal): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

        override fun areContentsTheSame(oldItem: CategoryTotal, newItem: CategoryTotal): Boolean {
            return oldItem == newItem
        }
    }
}