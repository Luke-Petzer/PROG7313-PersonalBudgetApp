package com.example.st10298850_prog7313_p2_lp

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityManageCategoriesBinding
import com.example.st10298850_prog7313_p2_lp.databinding.DialogEditCategoryBinding
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.Category
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private lateinit var database: AppDatabase
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            // User not logged in, redirect to login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        database = AppDatabase.getDatabase(this)

        setupClickListeners()
        
        setupRecyclerView()
        loadCategories(userId)
        updateTotalBudget(userId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.fabAddCategory.setOnClickListener {
            showEditCategoryDialog()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> deleteCategory(category) }
        )
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@ManageCategoriesActivity)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories(userId: Long) {
        lifecycleScope.launch {
            val categories = database.categoryDao().getCategoriesForUser(userId)
            categoryAdapter.submitList(categories)
        }
    }

    private fun updateTotalBudget(userId: Long) {
        lifecycleScope.launch {
            val totalBudget = database.categoryDao().getTotalBudgetForUser(userId) ?: 0.0
            binding.tvTotalBudget.text = String.format("R%.2f", totalBudget)
        }
    }

    private fun showEditCategoryDialog(category: Category? = null) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditCategoryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val isNewCategory = category == null
        dialogBinding.tvTitle.text = if (isNewCategory) "Add Category" else "Edit Category"
        if (!isNewCategory) {
            dialogBinding.etCategoryName.setText(category?.name)
            dialogBinding.etBudgetAmount.setText(category?.budgetedAmount.toString())
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSaveChanges.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString()
            val amount = dialogBinding.etBudgetAmount.text.toString().toDoubleOrNull()

            if (name.isNotEmpty() && amount != null && amount > 0) {
                val userId = UserSessionManager.getUserId(this)
                if (isNewCategory) {
                    val newCategory = Category(name = name, budgetedAmount = amount, userId = userId)
                    addCategory(newCategory)
                } else {
                    category?.name = name
                    category?.budgetedAmount = amount
                    updateCategory(category!!)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateCategory(category: Category) {
        val userId = UserSessionManager.getUserId(this)
        lifecycleScope.launch {
            database.categoryDao().updateCategory(category)
            loadCategories(userId)
            updateTotalBudget(userId)
        }
    }

    private fun deleteCategory(category: Category) {
        val userId = UserSessionManager.getUserId(this)
        lifecycleScope.launch {
            database.categoryDao().deleteCategory(category)
            loadCategories(userId)
            updateTotalBudget(userId)
        }
    }

    private fun addCategory(category: Category) {
        val userId = UserSessionManager.getUserId(this)
        lifecycleScope.launch {
            database.categoryDao().insertCategory(category)
            loadCategories(userId)
            updateTotalBudget(userId)
        }
    }
}