package com.example.recipeez.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.example.recipeez.R

class RegisterAccount : AppCompatActivity() {
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var confirmPasswordVisibilityToggle: ImageView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)

        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        confirmPasswordVisibilityToggle = findViewById(R.id.confirmPasswordVisibilityToggle)

        passwordVisibilityToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordEditText, passwordVisibilityToggle, isPasswordVisible)
        }

        confirmPasswordVisibilityToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordEditText, confirmPasswordVisibilityToggle, isConfirmPasswordVisible)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleImageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggleImageView.setImageResource(R.drawable.eye) // Use the same icon, no change
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleImageView.setImageResource(R.drawable.eye) // Optionally change icon if desired
        }
        // Move the cursor to the end of the text
        editText.setSelection(editText.text.length)
    }
}