package com.example.st10298850_prog7313_p2_lp

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityManageCategoriesBinding
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupClickListeners()
        setupRecyclerView()
        loadCategories()
//        setupListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }
    private fun showAddCategoryDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_category)

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnSaveChanges = dialog.findViewById<Button>(R.id.btnSaveChanges)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnSaveChanges.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setupRecyclerView() {
        // Setup your RecyclerView here
    }

    private fun loadCategories() {
        // Load categories from database
    }
}

//    private fun showEditCategoryDialog(category: Category? = null) {
//        val dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.dialog_edit_category)
//
//        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
//        val etCategoryName = dialog.findViewById<TextInputEditText>(R.id.etCategoryName)
//        val etBudgetAmount = dialog.findViewById<TextInputEditText>(R.id.etBudgetAmount)
//        val btnSaveChanges = dialog.findViewById<Button>(R.id.btnSaveChanges)
//        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
//
//        if (category != null) {
//            tvTitle.text = "Edit Category"
//            etCategoryName.setText(category.name)
//            etBudgetAmount.setText(category.budgetedAmount.toString())
//        } else {
//            tvTitle.text = "Add Category"
//        }
//
//        btnSaveChanges.setOnClickListener {
//            val name = etCategoryName.text.toString()
//            val amount = etBudgetAmount.text.toString().toDoubleOrNull() ?: 0.0
//
//            if (name.isNotEmpty() && amount > 0) {
//                if (category == null) {
//                    // Add new category
//                    val newCategory = Category(name = name, budgetedAmount = amount)
//                    addCategory(newCategory)
//                } else {
//                    // Update existing category
//                    category.name = name
//                    category.budgetedAmount = amount
//                    updateCategory(category)
//                }
//                dialog.dismiss()
//            } else {
//                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        btnClose.setOnClickListener { dialog.dismiss() }
//
//        dialog.show()
//    }
//
//    private fun addCategory(category: Category) {
//        lifecycleScope.launch {
//            database.categoryDao().insertCategory(category)
//            loadCategories()
//        }
//    }
//
//    private fun updateCategory(category: Category) {
//        lifecycleScope.launch {
//            database.categoryDao().updateCategory(category)
//            loadCategories()
//        }
//    }
