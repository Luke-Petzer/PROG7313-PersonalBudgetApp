package com.example.st10298850_prog7313_p2_lp

import android.util.Log
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

class TransactionAdapter(private val onItemClick: (Transaction) -> Unit) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        Log.d("TransactionAdapter", "Binding transaction with receipt path: ${transaction.receiptPath}")
        holder.bind(transaction)
        holder.itemView.setOnClickListener { onItemClick(transaction) }
    }

    class ViewHolder(
        private val binding: ItemTransactionBinding,
        private val onItemClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvCategory.text = transaction.category

            // Format both start and end dates
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val startDateStr = dateFormat.format(Date(transaction.startDate))
            val endDateStr = dateFormat.format(Date(transaction.endDate))

            // Combine start and end dates in the date text view
            binding.tvDate.text = "$startDateStr - $endDateStr"

            binding.tvAmount.text = String.format("%.2f", transaction.amount)

            if (transaction.type == "Expense") {
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.expense_color))
                binding.tvAmount.text = "-${binding.tvAmount.text}"
            } else {
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.income_color))
                binding.tvAmount.text = "+${binding.tvAmount.text}"
            }

            // Show or hide the receipt indicator based on whether a receipt path is available
            if (!transaction.receiptPath.isNullOrEmpty()) {
                binding.tvReceiptAttached.visibility = View.VISIBLE
                binding.tvReceiptAttached.setOnClickListener {
                    onItemClick(transaction)
                }
            } else {
                binding.tvReceiptAttached.visibility = View.GONE
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