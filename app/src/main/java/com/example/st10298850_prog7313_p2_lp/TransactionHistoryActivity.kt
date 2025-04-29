package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityTransactionHistoryBinding
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadTransactions()
    }

    private fun setupUI() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set up RecyclerView
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        // TODO: Set up RecyclerView adapter

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
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
                    // Already on transaction history, do nothing or refresh
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set up ChipGroup listeners
        binding.chipThisMonth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Filter transactions for this month
            }
        }

        binding.chipAllCategories.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Show all categories
            }
        }

        binding.chipPayee.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Filter by payee
            }
        }

        // Set up notification button
        binding.btnNotification.setOnClickListener {
            // TODO: Handle notification click
        }
    }

    private fun loadTransactions() {
        // TODO: Load transactions from database or API
        // For now, we'll just set a dummy balance
        binding.tvBalance.text = "R2,450.85"
    }
}