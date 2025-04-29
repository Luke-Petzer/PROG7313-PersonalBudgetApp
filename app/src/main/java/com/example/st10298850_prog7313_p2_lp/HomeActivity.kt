package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityHomeBinding
import android.content.Intent
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupUI()
//        loadData()
    }

    private fun setupUI() {
        // Set current date
        val currentDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        binding.currentDateText.text = currentDate

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.navigation_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.navigation_budget -> {
                    startActivity(Intent(this, TransactionHistoryActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
//
//    private fun loadData() {
//        lifecycleScope.launch {
//            // Load available amount
//            val availableAmount = database.accountDao().getTotalBalance()
//            binding.availableAmountText.text = String.format("R%.2f", availableAmount)
//
//            // Load spend today amount
//            val spendToday = calculateSpendToday()
//            binding.spendTodayText.text = String.format("R%.2f left to spend today", spendToday)
//
//            // Setup pie chart
//            setupPieChart()
//
//            // Load budget goals
//            val totalBudget = database.budgetGoalDao().getTotalBudget()
//            val spentAmount = database.transactionDao().getTotalSpent()
//            val progress = (spentAmount / totalBudget * 100).toInt()
//            binding.budgetGoalsProgress.progress = progress
//            binding.budgetGoalsAssigned.text = String.format("R%.2f assigned", totalBudget)
//            binding.budgetGoalsRemaining.text = String.format("R%.2f remaining", totalBudget - spentAmount)
//
//            // Load categories
//            val categories = database.categoryDao().getAllCategories()
//            // Setup RecyclerView with categories
//            // You'll need to