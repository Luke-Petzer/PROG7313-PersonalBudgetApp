package com.example.st10298850_prog7313_p2_lp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityTransactionHistoryBinding
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import com.example.st10298850_prog7313_p2_lp.viewmodels.TransactionHistoryViewModelFactory



class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var viewModel: TransactionHistoryViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            // Handle user not logged in, redirect to login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupViewModel(userId)
        setupRecyclerView()
        setupUI()
        observeTransactions()
    }

    private fun setupViewModel(userId: Long) {
        val factory = TransactionHistoryViewModelFactory(application, userId)
        viewModel = ViewModelProvider(this, factory)[TransactionHistoryViewModel::class.java]
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.adapter = transactionAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupBottomNavigation()
        setupDateFilter()
        setupCategoryChips()
        
        binding.btnNotification.setOnClickListener {
            // TODO: Handle notification click
        }

        binding.btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnClearDateFilter.setOnClickListener {
            viewModel.clearDateFilter()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.navigation_stats -> startActivity(Intent(this, StatsActivity::class.java))
                R.id.navigation_add -> startActivity(Intent(this, AddTransactionActivity::class.java))
                R.id.navigation_budget -> startActivity(Intent(this, TransactionHistoryActivity::class.java))
                R.id.navigation_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun setupDateFilter() {
        binding.chipThisMonth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setThisMonthFilter()
            } else {
                viewModel.clearDateFilter()
            }
        }
    }

    private fun setupCategoryChips() {
        viewModel.categories.observe(this) { categories ->
            binding.chipGroupCategories.removeAllViews()

            // Add "All Categories" chip
            val allChip = Chip(this).apply {
                id = View.generateViewId()
                text = "All Categories"
                isCheckable = true
                isChecked = true
            }
            binding.chipGroupCategories.addView(allChip)

            // Add chips for each category
            categories.forEach { category ->
                val chip = Chip(this).apply {
                    id = View.generateViewId()
                    text = category
                    isCheckable = true
                }
                binding.chipGroupCategories.addView(chip)
            }

            binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
                val selectedChip = binding.chipGroupCategories.findViewById<Chip>(checkedId)
                val selectedCategory = selectedChip?.text?.toString()
                viewModel.setCategoryFilter(selectedCategory)
            }
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date range")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second
            viewModel.setDateFilter(startDate, endDate)
            binding.chipThisMonth.isChecked = false
        }

        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun observeTransactions() {
        viewModel.filteredTransactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }
    }
}