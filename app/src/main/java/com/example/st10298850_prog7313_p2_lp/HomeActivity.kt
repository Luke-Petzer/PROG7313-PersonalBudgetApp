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
import com.example.st10298850_prog7313_p2_lp.data.CategoryTotal
import com.example.st10298850_prog7313_p2_lp.viewmodels.HomeViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat


/**
 * HomeActivity is the main screen of the application.
 * It displays category totals, budget goals, and provides navigation to other features.
 */
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoryTotalAdapter: CategoryTotalAdapter
    // ViewModel initialization using viewModels delegate
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

        // Check if user is logged in, redirect to login if not
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
            viewModel.loadCategoryTotals() // Load all category totals when activity starts
            viewModel.loadBudgetGoals() // Load budget goals when activity starts
            observeBudgetGoals()
        }
    }
    /**
     * Sets up the UI components including bottom navigation and click listeners.
     */
    private fun setupUI() {
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

        binding.btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }
    /**
     * Sets up the RecyclerView for displaying category totals.
     */
    private fun setupRecyclerView() {
        categoryTotalAdapter = CategoryTotalAdapter()
        binding.rvCategoryTotals.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = categoryTotalAdapter
        }
    }
    /**
     * Observes changes in category totals and updates the UI accordingly.
     */
    private fun observeCategoryTotals() {
        viewModel.categoryTotals.observe(this) { categoryTotals ->
            categoryTotalAdapter.submitList(categoryTotals)
            updateTotalSpending(categoryTotals)
        }
    }
    /**
     * Sets up the date range picker functionality.
     */
    private fun setupDateRangePicker() {
        binding.btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }
    /**
     * Shows the date range picker dialog and handles the selected date range.
     */
    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second
            viewModel.loadCategoryTotalsForDateRange(startDate, endDate)

            // Update UI to show selected date range
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDateRange.text = "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}"
        }

        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }
    /**
     * Shows a dialog for setting budget goals.
     */
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
    /**
     * Observes changes in budget goals and updates the UI accordingly.
     */
    private fun observeBudgetGoals() {
        viewModel.budgetGoals.observe(this) { goals ->
            val shortTermGoal = goals.find { it.name == "Short Term Goal" }
            val longTermGoal = goals.find { it.name == "Long Term Goal" }

            binding.minGoalText.text = "Short Term Goal: R${shortTermGoal?.goalAmount ?: 0}"
            binding.maxGoalText.text = "Long Term Goal: R${longTermGoal?.goalAmount ?: 0}"
        }
    }
    /**
     * Updates the total spending display based on category totals.
     */
    private fun updateTotalSpending(categoryTotals: List<CategoryTotal>) {
        val totalSpending = categoryTotals.sumOf { it.totalAmount }
        binding.tvTotalSpending.text = "Total Spending: R%.2f".format(totalSpending)
    }
}