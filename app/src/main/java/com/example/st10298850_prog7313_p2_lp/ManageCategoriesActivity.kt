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
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private lateinit var database: AppDatabase
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupClickListeners()
        setupRecyclerView()
        loadCategories()
        updateTotalBudget()
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

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = database.categoryDao().getCategoriesForUser(1) // Assuming user ID 1 for now
            categoryAdapter.submitList(categories)
        }
    }

    private fun updateTotalBudget() {
        lifecycleScope.launch {
            val totalBudget = database.categoryDao().getTotalBudgetForUser(1) ?: 0.0
            binding.tvTotalBudget.text = String.format("R%.2f", totalBudget)
        }
    }

    private fun showEditCategoryDialog(category: Category? = null) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditCategoryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        if (category != null) {
            dialogBinding.tvTitle.text = "Edit Category"
            dialogBinding.etCategoryName.setText(category.name)
            dialogBinding.etBudgetAmount.setText(category.budgetedAmount.toString())
        } else {
            dialogBinding.tvTitle.text = "Add Category"
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSaveChanges.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString()
            val amount = dialogBinding.etBudgetAmount.text.toString().toDoubleOrNull()

            if (name.isNotEmpty() && amount != null && amount > 0) {
                if (category == null) {
                    val newCategory = Category(userId = 1, name = name, budgetedAmount = amount)
                    addCategory(newCategory)
                } else {
                    category.name = name
                    category.budgetedAmount = amount
                    updateCategory(category)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addCategory(category: Category) {
        lifecycleScope.launch {
            database.categoryDao().insertCategory(category)
            loadCategories()
            updateTotalBudget()
        }
    }

    private fun updateCategory(category: Category) {
        lifecycleScope.launch {
            database.categoryDao().updateCategory(category)
            loadCategories()
            updateTotalBudget()
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            database.categoryDao().deleteCategory(category)
            loadCategories()
            updateTotalBudget()
        }
    }
}