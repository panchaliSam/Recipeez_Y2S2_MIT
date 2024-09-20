package com.example.recipeez.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import com.example.recipeez.R
import com.example.recipeez.databinding.ActivityLoginScreenBinding
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val loginBtn = Intent(this, HomeScreen::class.java)
                        startActivity(loginBtn)
                        finish() // Optionally finish the LoginScreen activity so the user can't navigate back
                    } else {
                        Toast.makeText(this, task.exception?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed!!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle eye icon click to toggle password visibility
        binding.eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Show password
                binding.loginPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeIcon.setImageResource(R.drawable.eye) // Use appropriate icon for open eye
            } else {
                // Hide password
                binding.loginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeIcon.setImageResource(R.drawable.eye) // Use appropriate icon for closed eye
            }
            binding.loginPassword.setSelection(binding.loginPassword.text.length) // Move cursor to the end
        }

        // Handle "Login Back" button click to navigate to WelcomeScreen
        binding.loginBackBtn.setOnClickListener {
            val loginBackIntent = Intent(this, WelcomeScreen::class.java)
            startActivity(loginBackIntent)
            finish() // Optionally finish the LoginScreen activity if you don't want to return to it
        }
    }
}
