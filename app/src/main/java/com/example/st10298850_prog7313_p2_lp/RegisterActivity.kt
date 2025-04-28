package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityRegisterBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.User

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Set up click listener for the "Login" text
        binding.loginTextView.setOnClickListener {
            finish() // This will close the RegisterActivity and return to MainActivity
        }

        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (validateInput(name, email, password)) {
            lifecycleScope.launch {
                val existingUser = database.userDao().getUserByEmail(email)
                if (existingUser == null) {
                    val newUser = User(name = name, email = email, password = password)
                    database.userDao().insertUser(newUser)
                    Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    finish() // Go back to login screen
                } else {
                    Toast.makeText(this@RegisterActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        return passwordPattern.matches(password)
    }
}