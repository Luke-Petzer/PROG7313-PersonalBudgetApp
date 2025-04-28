package com.example.st10298850_prog7313_p2_lp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityRegisterBinding



class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the "Login" text
        binding.loginTextView.setOnClickListener {
            finish() // This will close the RegisterActivity and return to MainActivity
        }

        binding.registerButton.setOnClickListener {
            // TODO: Implement registration logic
        }
    }
}