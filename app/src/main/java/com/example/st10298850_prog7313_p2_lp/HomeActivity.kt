package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityHomeBinding
import android.content.Intent
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.adapters.CategoryTotalAdapter
import com.example.st10298850_prog7313_p2_lp.viewmodels.HomeViewModel
import androidx.activity.viewModels
import android.app.Dialog
import android.view.Window
import android.widget.Toast
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoal
import com.example.st10298850_prog7313_p2_lp.databinding.DialogBudgetGoalsBinding
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoalRepository
import com.example.st10298850_prog7313_p2_lp.viewmodels.HomeViewModelFactory

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoryTotalAdapter: CategoryTotalAdapter
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            application,
            BudgetGoalRepository(AppDatabase.getDatabase(this).budgetGoalDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            // Handle user not logged in, e.g., redirect to login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            setupUI()
            setupRecyclerView()
            observeCategoryTotals()
            setupDateRangePicker()
            viewModel.loadBudgetGoals() // Load budget goals when activity starts
            observeBudgetGoals()
        }
    }

    private fun setupUI() {
        // Set current date
        // You can add code here to set the current date if needed

        // Change this line to use the correct button
        binding.setGoalsButton.setOnClickListener {
            showBudgetGoalsDialog()
        }

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true // Already on home
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

    private fun setupRecyclerView() {
        categoryTotalAdapter = CategoryTotalAdapter()
        binding.rvCategoryTotals.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = categoryTotalAdapter
        }
    }

    private fun observeCategoryTotals() {
        viewModel.categoryTotals.observe(this) { categoryTotals ->
            categoryTotalAdapter.submitList(categoryTotals)
        }
    }

    private fun setupDateRangePicker() {
        binding.btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        // Implement your date range picker dialog here
        // After selecting the date range, call:
        // viewModel.loadCategoryTotals(startDate, endDate)
    }

    private fun showBudgetGoalsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogBudgetGoalsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnSaveBudgetGoals.setOnClickListener {
            val shortTermAmount = dialogBinding.etShortTermAmount.text.toString().toDoubleOrNull()
            val longTermAmount = dialogBinding.etLongTermAmount.text.toString().toDoubleOrNull()

            if (shortTermAmount != null && longTermAmount != null) {
                viewModel.saveBudgetGoals(
                    BudgetGoal(name = "Short Term Goal", goalAmount = shortTermAmount, userId = UserSessionManager.getUserId(this)),
                    BudgetGoal(name = "Long Term Goal", goalAmount = longTermAmount, userId = UserSessionManager.getUserId(this))
                )
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields with valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun observeBudgetGoals() {
        viewModel.budgetGoals.observe(this) { goals ->
            val shortTermGoal = goals.find { it.name == "Short Term Goal" }
            val longTermGoal = goals.find { it.name == "Long Term Goal" }

            binding.minGoalText.text = "Short Term Goal: R${shortTermGoal?.goalAmount ?: 0}"
            binding.maxGoalText.text = "Long Term Goal: R${longTermGoal?.goalAmount ?: 0}"
        }
    }
}