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
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import kotlinx.coroutines.launch

/**
 * MainActivity serves as the entry point of the application.
 * It handles user login and navigation to the registration screen.
 */
class MainActivity : AppCompatActivity() {
    // View binding for easy access to the layout views
    private lateinit var binding: ActivityMainBinding
    // Database instance for data operations
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the database
        database = AppDatabase.getDatabase(this)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the login button
        binding.loginButton.setOnClickListener {
            loginUser()
        }

        // Set up click listener for the sign-up text view
        binding.signUpTextView.setOnClickListener {
            // Navigate to the RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle system insets to ensure proper layout with edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Handles the user login process.
     * Validates input, checks credentials, updates login streak, and navigates to HomeActivity on success.
     */
    private fun loginUser() {
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Perform login operation in a coroutine
        lifecycleScope.launch {
            try {
                // Attempt to retrieve user with given credentials
                val user = database.userDao().getUserByUsernameAndPassword(username, password)
                if (user != null) {
                    // Update login streak
                    val currentDate = System.currentTimeMillis()
                    val dayInMillis = 24 * 60 * 60 * 1000
                    val streak = if (currentDate - user.lastLoginDate < dayInMillis) user.loginStreak + 1 else 1
                    database.userDao().updateLoginStreak(user.userId, streak, currentDate)

                    // Save user ID in SharedPreferences for session management
                    UserSessionManager.saveUserId(this@MainActivity, user.userId)

                    Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to the HomeActivity
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Close the MainActivity so the user can't go back to it
                } else {
                    // Invalid credentials
                    Toast.makeText(this@MainActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during login
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}