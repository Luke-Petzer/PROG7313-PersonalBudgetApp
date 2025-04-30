package com.example.st10298850_prog7313_p2_lp.repositories

import android.util.Log
import com.example.st10298850_prog7313_p2_lp.data.Category
import com.example.st10298850_prog7313_p2_lp.data.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    suspend fun getCategoriesForUser(userId: Long): List<Category> {
        val categories = categoryDao.getCategoriesForUser(userId)
        Log.d("CategoryRepository", "Fetched ${categories.size} categories for user $userId")
        return categories
    }

    suspend fun insertDefaultCategories(userId: Long) {
        val defaultCategories = listOf(
            Category(name = "Food", userId = userId, budgetedAmount = 0.0),
            Category(name = "Transportation", userId = userId, budgetedAmount = 0.0),
            Category(name = "Entertainment", userId = userId, budgetedAmount = 0.0),
            Category(name = "Utilities", userId = userId, budgetedAmount = 0.0),
            Category(name = "Shopping", userId = userId, budgetedAmount = 0.0)
        )
        defaultCategories.forEach { categoryDao.insertCategory(it) }
        Log.d("CategoryRepository", "Inserted ${defaultCategories.size} default categories for user $userId")
    }
}