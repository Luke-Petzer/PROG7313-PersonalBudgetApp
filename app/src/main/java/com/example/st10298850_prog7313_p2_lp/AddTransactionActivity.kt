package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityAddTransactionBinding
import com.example.st10298850_prog7313_p2_lp.repositories.AccountRepository
import com.example.st10298850_prog7313_p2_lp.repositories.CategoryRepository
import com.example.st10298850_prog7313_p2_lp.repositories.TransactionRepository
import com.example.st10298850_prog7313_p2_lp.viewmodels.AddTransactionViewModel
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("AddTransactionActivity", "etCategory null? ${binding.etCategory == null}")
        Log.d("AddTransactionActivity", "etAccount null? ${binding.etAccount == null}")

        setupViewModel()
        setupDropdowns()
        setupDatePickers()
        setupRepeatToggle()
        setupTabLayoutListener()

        binding.btnAddTransaction.setOnClickListener {
            addTransaction()
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val categoryRepository = CategoryRepository(database.categoryDao())
        val accountRepository = AccountRepository(database.accountDao())
        val factory = AddTransactionViewModel.Factory(transactionRepository, categoryRepository, accountRepository)
        viewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

        val currentUserId = getCurrentUserId()
        viewModel.loadCategoriesForUser(currentUserId)
        viewModel.loadAccountsForUser(currentUserId)
    }

    private fun setupDropdowns() {
        viewModel.categories.observe(this) { categories ->
            Log.d("AddTransactionActivity", "Observed ${categories.size} categories")
            Log.d("AddTransactionActivity", "Category names: ${categories.map { it.name }}")
            if (categories.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.insertDefaultCategories(getCurrentUserId())
                    viewModel.loadCategoriesForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories.map { it.name })
                binding.etCategory.setAdapter(adapter)
                binding.etCategory.threshold = 1
                binding.etCategory.setOnClickListener {
                    binding.etCategory.showDropDown()
                }
            }
        }

        viewModel.accounts.observe(this) { accounts ->
            Log.d("AddTransactionActivity", "Observed ${accounts.size} accounts")
            Log.d("AddTransactionActivity", "Account names: ${accounts.map { it.name }}")
            if (accounts.isEmpty()) {
                lifecycleScope.launch {

                    viewModel.loadAccountsForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, accounts.map { it.name })
                binding.etAccount.setAdapter(adapter)
                binding.etAccount.threshold = 1
                binding.etAccount.setOnClickListener {
                    binding.etAccount.showDropDown()
                }
            }
        }
    }

    private fun setupDatePickers() {
        // Implementation as before
    }

    private fun setupRepeatToggle() {
        // Implementation as before
    }

    private fun setupTabLayoutListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Expense selected
                        // Update UI or viewModel as needed
                    }
                    1 -> {
                        // Income selected
                        // Update UI or viewModel as needed
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun addTransaction() {
        val amountText = binding.tvAmount.text.toString().replace("R", "")
        val amount = amountText.toDoubleOrNull()

        val category = binding.etCategory.text.toString()
        val account = binding.etAccount.text.toString()
        val description = binding.etDescription.text.toString()
        val dateStr = binding.etDate.text.toString()

        if (amount == null || category.isEmpty() || account.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: return

        // Find the selected account ID
        val selectedAccount = viewModel.accounts.value?.find { it.name == account }
        val accountId = selectedAccount?.accountId ?: return

        val transaction = Transaction(
            userId = getCurrentUserId(),
            type = if (binding.tabLayout.selectedTabPosition == 0) "Expense" else "Income",
            amount = amount,
            accountId = accountId,
            date = date,
            description = description,
            receiptPath = null, // Implement image upload if needed
            repeat = binding.switchRepeat.isChecked
        )

        viewModel.addTransaction(transaction)
        Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getCurrentUserId(): Long {
        // Implement this method to return the current user's ID
        // This could be stored in SharedPreferences, a database, or a singleton object
        return 1 // Placeholder, replace with actual implementation
    }
}