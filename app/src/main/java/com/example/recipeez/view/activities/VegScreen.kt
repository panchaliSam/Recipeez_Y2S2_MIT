package com.example.recipeez.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class VegScreen : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var skipButton: Button
    private lateinit var sriLankanCuisineButton: ImageButton
    private lateinit var indianCuisineButton: ImageButton
    private lateinit var chineseCuisineButton: ImageButton
    private lateinit var italianCuisineButton: ImageButton
    private lateinit var mexicanCuisineButton: ImageButton
    private lateinit var japaneseCuisineButton: ImageButton
    private lateinit var otherCuisineButton: ImageButton
    private lateinit var continueButton: Button

    private val selectedCuisines = mutableListOf<String>()
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("users")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_veg_screen)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        backButton = findViewById(R.id.backButton)
        skipButton = findViewById(R.id.skipButton)
        sriLankanCuisineButton = findViewById(R.id.sriLankanCuisine)
        indianCuisineButton = findViewById(R.id.indianCuisine)
        chineseCuisineButton = findViewById(R.id.chineseCuisine)
        italianCuisineButton = findViewById(R.id.italianCuisine)
        mexicanCuisineButton = findViewById(R.id.mexicanCuisine)
        japaneseCuisineButton = findViewById(R.id.japaneseCuisine)
        otherCuisineButton = findViewById(R.id.OtherCuisine)
        continueButton = findViewById(R.id.continueButton)

        // Disable the continue button initially
        continueButton.isEnabled = false

        // Set up click listeners for each cuisine button
        setUpCuisineSelection(sriLankanCuisineButton, "Sri Lankan")
        setUpCuisineSelection(indianCuisineButton, "Indian")
        setUpCuisineSelection(chineseCuisineButton, "Chinese")
        setUpCuisineSelection(italianCuisineButton, "Italian")
        setUpCuisineSelection(mexicanCuisineButton, "Mexican")
        setUpCuisineSelection(japaneseCuisineButton, "Japanese")
        setUpCuisineSelection(otherCuisineButton, "Other")

        backButton.setOnClickListener {
            val intent = Intent(this, QuestionnaireScreen::class.java)
            startActivity(intent)
            finish()
        }

        skipButton.setOnClickListener {
            val intent = Intent(this, AllergyScreen::class.java)
            startActivity(intent)
            finish()
        }

        // Continue button saves selected cuisines to Firebase
        continueButton.setOnClickListener {
            saveSelectedCuisines()
        }
    }

    private fun setUpCuisineSelection(cuisineButton: ImageButton, cuisineName: String) {
        cuisineButton.setOnClickListener {
            if (selectedCuisines.contains(cuisineName)) {
                // If already selected, remove it from the list
                selectedCuisines.remove(cuisineName)
                cuisineButton.alpha = 1.0f // Reset button opacity
            } else {
                // If not selected, add it to the list
                selectedCuisines.add(cuisineName)
                cuisineButton.alpha = 0.5f // Indicate selection with reduced opacity
            }

            // Enable the continue button if at least one cuisine is selected
            continueButton.isEnabled = selectedCuisines.isNotEmpty()
        }
    }

    private fun saveSelectedCuisines() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Get current user ID
            val userId = currentUser.uid

            // Save selected cuisines to 'cuisines' field in users table
            val userUpdates = mapOf("cuisines" to selectedCuisines)

            // Save to Firebase Realtime Database
            database.child(userId).updateChildren(userUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cuisines saved successfully!", Toast.LENGTH_SHORT).show()

                    // Redirect to AllergyScreen
                    val intent = Intent(this, AllergyScreen::class.java)
                    startActivity(intent)
                    finish() // Close this activity so it doesn't remain in the backstack
                } else {
                    Toast.makeText(
                        this,
                        "Failed to save cuisines: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
