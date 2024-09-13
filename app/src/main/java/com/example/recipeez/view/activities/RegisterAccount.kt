package com.example.recipeez.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.recipeez.R
import com.example.recipeez.databinding.ActivityRegisterAccountBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterAccount : AppCompatActivity() {
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var confirmPasswordVisibilityToggle: ImageView
    private lateinit var binding: ActivityRegisterAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
                if(pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            val intent = Intent(this,HomeScreen::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this,"Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Empty fields are not allowed!!", Toast.LENGTH_SHORT).show()
            }
        }

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