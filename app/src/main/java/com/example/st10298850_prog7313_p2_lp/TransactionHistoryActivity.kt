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
import android.widget.Toast
import java.io.File
import androidx.core.content.FileProvider

/**
 * Activity for displaying and managing transaction history.
 * This activity allows users to view their transactions, filter them by date and category,
 * and view receipt images associated with transactions.
 */
class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var viewModel: TransactionHistoryViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    /**
     * Initializes the activity, sets up the UI, and checks for user authentication.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in, redirect to login if not
        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupViewModel(userId)
        setupRecyclerView()
        setupUI()
        observeTransactions()
    }

    /**
     * Logs the start of the activity for debugging purposes.
     */
    override fun onStart() {
        super.onStart()
        Log.d("ImagePopup", "Glide load started")
    }

    /**
     * Sets up the ViewModel for this activity.
     * @param userId The ID of the currently logged-in user.
     */
    private fun setupViewModel(userId: Long) {
        val factory = TransactionHistoryViewModelFactory(application, userId)
        viewModel = ViewModelProvider(this, factory)[TransactionHistoryViewModel::class.java]
    }

    /**
     * Sets up the RecyclerView to display transactions.
     * Configures the adapter and sets up a click listener for viewing receipt images.
     */
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

        // Observe changes in filtered transactions and update the adapter
        viewModel.filteredTransactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }
    }

    /**
     * Sets up the UI components including toolbar, bottom navigation, date filter, and category chips.
     */
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupBottomNavigation()
        setupDateFilter()
        setupCategoryChips()

        // Setup click listeners for various UI elements
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

    /**
     * Sets up the bottom navigation menu.
     */
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

    /**
     * Sets up the date filter chip for filtering transactions by the current month.
     */
    private fun setupDateFilter() {
        binding.chipThisMonth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setThisMonthFilter()
            } else {
                viewModel.clearDateFilter()
            }
        }
    }

    /**
     * Sets up category chips for filtering transactions by category.
     */
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

            // Set up listener for category selection
            binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
                val selectedChip = binding.chipGroupCategories.findViewById<Chip>(checkedId)
                val selectedCategory = selectedChip?.text?.toString()
                viewModel.setCategoryFilter(selectedCategory)
            }
        }
    }

    /**
     * Shows a date range picker dialog for selecting a custom date range.
     */
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

    /**
     * Observes changes in filtered transactions and updates the UI accordingly.
     */
    private fun observeTransactions() {
        viewModel.filteredTransactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }
    }

    /**
     * Shows a popup dialog displaying the receipt image for a transaction.
     * @param receiptPath The file path of the receipt image.
     */
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

        // Load the image using Glide
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
            .into(object : com.bumptech.glide.request.target.Target<Drawable> {
                override fun onStart() {
                    Log.d("ImagePopup", "Glide load started")
                }

                override fun onStop() {
                    Log.d("ImagePopup", "Glide load stopped")
                }

                override fun onDestroy() {
                    Log.d("ImagePopup", "Glide target destroyed")
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    Log.d("ImagePopup", "Glide load started with placeholder")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e("ImagePopup", "Glide load failed")
                }

                override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                    Log.d("ImagePopup", "Glide resource ready")
                    imageView.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("ImagePopup", "Glide load cleared")
                }

                override fun getSize(cb: com.bumptech.glide.request.target.SizeReadyCallback) {
                    cb.onSizeReady(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                }

                override fun removeCallback(cb: com.bumptech.glide.request.target.SizeReadyCallback) {}
                override fun setRequest(request: com.bumptech.glide.request.Request?) {}
                override fun getRequest(): com.bumptech.glide.request.Request? = null
            })

        dialog.show()
        Log.d("ImagePopup", "Dialog shown")
    }
}