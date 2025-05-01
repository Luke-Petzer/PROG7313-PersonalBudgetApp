package com.example.st10298850_prog7313_p2_lp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.st10298850_prog7313_p2_lp.databinding.ItemTransactionBinding
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvCategory.text = transaction.category // Use description as category for now
            binding.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.date))
            binding.tvAmount.text = String.format("%.2f", transaction.amount)

            if (transaction.type == "Expense") {
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.expense_color))
                binding.tvAmount.text = "-${binding.tvAmount.text}"
            } else {
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.income_color))
                binding.tvAmount.text = "+${binding.tvAmount.text}"
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.transactionId == newItem.transactionId
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}