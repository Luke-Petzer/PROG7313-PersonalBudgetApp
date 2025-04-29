package com.example.st10298850_prog7313_p2_lp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add these lines to drop and recreate the database
        database = AppDatabase.getDatabase(this)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        // Set up click listener for the "Sign up" text
        binding.signUpTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // TODO: Implement login logic

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = database.userDao().getUserByEmailAndPassword(email, password)
                if (user != null) {
                    // Update login streak
                    val currentDate = System.currentTimeMillis()
                    val dayInMillis = 24 * 60 * 60 * 1000
                    val streak = if (currentDate - user.lastLoginDate < dayInMillis) user.loginStreak + 1 else 1
                    database.userDao().updateLoginStreak(user.userId, streak, currentDate)

                    Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to the HomeActivity
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.putExtra("USER_ID", user.userId)
                    startActivity(intent)
                    finish() // Close the MainActivity so the user can't go back to it
                } else {
                    Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}