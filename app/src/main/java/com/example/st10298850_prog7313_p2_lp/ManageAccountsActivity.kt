package com.example.st10298850_prog7313_p2_lp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.data.Account
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityManageAccountsBinding
import com.example.st10298850_prog7313_p2_lp.databinding.DialogEditAccountBinding
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import kotlinx.coroutines.launch

/**
 * ManageAccountsActivity allows users to view, add, and manage their financial accounts.
 */
class ManageAccountsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAccountsBinding
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var database: AppDatabase

    /**
     * Initializes the activity, sets up the UI, and loads user accounts.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in, redirect to login if not
        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            redirectToLogin()
            return
        }

        // Initialize database and setup UI components
        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupClickListeners()
        loadAccounts(userId)
    }

    /**
     * Redirects the user to the login screen.
     */
    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Sets up the RecyclerView to display the list of accounts.
     */
    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter()
        binding.rvAccounts.apply {
            layoutManager = LinearLayoutManager(this@ManageAccountsActivity)
            adapter = accountAdapter
        }
    }

    /**
     * Sets up click listeners for various UI elements.
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            showAddAccountDialog()
        }

        binding.fabAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }

    /**
     * Loads and displays accounts for the given user ID.
     */
    private fun loadAccounts(userId: Long) {
        lifecycleScope.launch {
            val accounts = database.accountDao().getAccountsForUser(userId)
            accountAdapter.submitList(accounts)
        }
    }

    /**
     * Displays a dialog for adding a new account.
     */
    private fun showAddAccountDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditAccountBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSaveChanges.setOnClickListener {
            // Retrieve input values
            val name = dialogBinding.etAccountName.text.toString()
            val type = dialogBinding.etAccountType.text.toString()
            val amount = dialogBinding.etAccountAmount.text.toString().toDoubleOrNull()

            // Validate input and create new account
            if (name.isNotEmpty() && type.isNotEmpty() && amount != null) {
                val userId = UserSessionManager.getUserId(this)
                val newAccount = Account(userId = userId, name = name, type = type, goalAmount = amount)
                addAccount(newAccount)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    /**
     * Adds a new account to the database and refreshes the account list.
     */
    private fun addAccount(account: Account) {
        lifecycleScope.launch {
            database.accountDao().insertAccount(account)
            loadAccounts(account.userId) // Refresh the list with the correct userId
        }
    }
}