package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityRegisterBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.User

/**
 * RegisterActivity handles the user registration process.
 * It allows users to create a new account by providing their details.
 */
class RegisterActivity : AppCompatActivity() {
    // View binding for easy access to the layout views
    private lateinit var binding: ActivityRegisterBinding
    // Database instance for data operations
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database
        database = AppDatabase.getDatabase(this)

        // Set up click listener for the "Login" text
        binding.loginTextView.setOnClickListener {
            finish() // This will close the RegisterActivity and return to MainActivity
        }

        // Set up click listener for the register button
        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Handles the user registration process.
     * Validates input, checks for existing username, and creates a new user if validation passes.
     */
    private fun registerUser() {
        val name = binding.nameEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (validateInput(name, username, email, password)) {
            lifecycleScope.launch {
                // Check if the username already exists
                val existingUser = database.userDao().getUserByUsername(username)
                if (existingUser == null) {
                    // Create and insert new user
                    val newUser = User(name = name, username = username, email = email, password = password)
                    database.userDao().insertUser(newUser)
                    Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    finish() // Go back to login screen
                } else {
                    Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Validates the user input for registration.
     * Checks if all fields are filled, email format is valid, and password is strong.
     *
     * @param name User's full name
     * @param username User's chosen username
     * @param email User's email address
     * @param password User's chosen password
     * @return Boolean indicating whether the input is valid
     */
    private fun validateInput(name: String, username: String, email: String, password: String): Boolean {
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isPasswordStrong(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long, contain 1 uppercase letter, 1 number, and 1 special character", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    /**
     * Checks if the provided email is in a valid format.
     *
     * @param email The email address to validate
     * @return Boolean indicating whether the email is valid
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the provided password meets the strength criteria.
     * The password must be at least 8 characters long, contain 1 uppercase letter,
     * 1 number, and 1 special character.
     *
     * @param password The password to validate
     * @return Boolean indicating whether the password is strong enough
     */
    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        return passwordPattern.matches(password)
    }
}