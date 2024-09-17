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

class QuestionnaireScreen : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var continueButton: Button
    private lateinit var skipButton: Button


    private var isVegetarianSelected: String = ""

    // Firebase Auth and Database Reference
    private lateinit var firebaseAuth: FirebaseAuth
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire_screen)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI components
        backButton = findViewById(R.id.backButton)
        yesButton = findViewById(R.id.yesButton)
        noButton = findViewById(R.id.noButton)
        continueButton = findViewById(R.id.continueButton)
        skipButton = findViewById(R.id.skipButton)

        // Set click listeners for Yes/No buttons
        backButton.setOnClickListener {
            val intent = Intent(this, RegisterAccount::class.java)
            startActivity(intent)
            finish()
        }

        yesButton.setOnClickListener {
            isVegetarianSelected = "Veg"
            updateButtonStates()
        }

        noButton.setOnClickListener {
            isVegetarianSelected = "Non-Veg"
            updateButtonStates()
        }

        // Set click listener for Continue button
        continueButton.setOnClickListener {
            if (isVegetarianSelected != null) {
                // Save selection to Firebase
                saveSelectionToFirebase(isVegetarianSelected!!)
            }
        }

        skipButton.setOnClickListener {
            val intent = Intent(this, VegScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateButtonStates() {
        continueButton.isEnabled = true
        continueButton.setBackgroundColor(getColor(R.color.green))  // Assuming green is defined in colors.xml

        if (isVegetarianSelected == "Veg") {
            yesButton.setBackgroundColor(getColor(R.color.green))
            yesButton.setTextColor(getColor(R.color.white))
            noButton.setBackgroundColor(getColor(R.color.white))
            noButton.setTextColor(getColor(R.color.black))
        } else {
            noButton.setBackgroundColor(getColor(R.color.green))
            noButton.setTextColor(getColor(R.color.white))
            yesButton.setBackgroundColor(getColor(R.color.white))
            yesButton.setTextColor(getColor(R.color.black))
        }
    }

    // Save the vegetarian selection to Firebase
    private fun saveSelectionToFirebase(isVegetarian: String) {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Create a map to hold the vegetarian selection
            val userSelection = mapOf("foodType" to isVegetarian)

            // Save the selection under the user's node in Firebase
            databaseReference.child(userId).updateChildren(userSelection)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Selection saved successfully!", Toast.LENGTH_SHORT)
                            .show()

                        val intent = Intent(this, VegScreen::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            "Failed to save selection: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
