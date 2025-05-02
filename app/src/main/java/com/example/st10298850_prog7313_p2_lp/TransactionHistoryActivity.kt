package com.example.st10298850_prog7313_p2_lp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import com.example.st10298850_prog7313_p2_lp.utils.ImagePreviewUtil
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.TransactionAdapter
import java.io.File
import androidx.core.content.FileProvider

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
        transactionAdapter = TransactionAdapter { transaction ->
            if (!transaction.receiptPath.isNullOrEmpty()) {
                showImagePopup(transaction.receiptPath)
            }
        }

        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
        }

        viewModel.filteredTransactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }
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
                R.id.navigation_add -> startActivity(
                    Intent(
                        this,
                        AddTransactionActivity::class.java
                    )
                )

                R.id.navigation_budget -> startActivity(
                    Intent(
                        this,
                        TransactionHistoryActivity::class.java
                    )
                )

                R.id.navigation_settings -> startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )

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

    private fun showImagePopup(receiptPath: String) {
        Log.d("ImagePopup", "Attempting to show image from path: $receiptPath")

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_popup)

        val imageView = dialog.findViewById<ImageView>(R.id.popupImageView)

        val file = File(receiptPath)
        if (!file.exists()) {
            Log.e("ImagePopup", "File does not exist: $receiptPath")
            Toast.makeText(this, "Receipt image not found", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        Log.d("ImagePopup", "Created URI: $uri")

        Glide.with(this)
            .load(uri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("ImagePopup", "Glide failed to load image", e)
                    e?.logRootCauses("ImagePopup")
                    runOnUiThread {
                        Toast.makeText(this@TransactionHistoryActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("ImagePopup", "Glide successfully loaded image")
                    return false
                }
            })
            .into(imageView)

        dialog.show()
        Log.d("ImagePopup", "Dialog shown")
    }
}