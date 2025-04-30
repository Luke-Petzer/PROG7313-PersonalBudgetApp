package com.example.st10298850_prog7313_p2_lp

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.data.Account
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityManageAccountsBinding
import com.example.st10298850_prog7313_p2_lp.databinding.DialogEditAccountBinding
import kotlinx.coroutines.launch

class ManageAccountsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAccountsBinding
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupClickListeners()
        loadAccounts()
    }

    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter()
        binding.rvAccounts.apply {
            layoutManager = LinearLayoutManager(this@ManageAccountsActivity)
            adapter = accountAdapter
        }
    }

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

    private fun loadAccounts() {
        lifecycleScope.launch {
            val accounts = database.accountDao().getAccountsForUser(1) // Assuming user ID 1 for now
            accountAdapter.submitList(accounts)
        }
    }

    private fun showAddAccountDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditAccountBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSaveChanges.setOnClickListener {
            val name = dialogBinding.etAccountName.text.toString()
            val type = dialogBinding.etAccountType.text.toString()
            val amount = dialogBinding.etAccountAmount.text.toString().toDoubleOrNull()

            if (name.isNotEmpty() && type.isNotEmpty() && amount != null) {
                val newAccount = Account(userId = 1, name = name, type = type, goalAmount = amount)
                addAccount(newAccount)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addAccount(account: Account) {
        lifecycleScope.launch {
            database.accountDao().insertAccount(account)
            loadAccounts() // Refresh the list
        }
    }
}