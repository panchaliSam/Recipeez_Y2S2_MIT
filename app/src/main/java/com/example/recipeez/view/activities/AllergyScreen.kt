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

class AllergyScreen : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var skipButton: Button
    private lateinit var eggsAllergyButton: ImageButton
    private lateinit var peanutsAllergyButton: ImageButton
    private lateinit var prawnsAllergyButton: ImageButton
    private lateinit var shrimpsAllergyButton: ImageButton
    private lateinit var crabsAllergyButton: ImageButton
    private lateinit var garlicAllergyButton: ImageButton
    private lateinit var porkAllergyButton: ImageButton
    private lateinit var wheatAllergyButton: ImageButton
    private lateinit var continueButton: Button

    private val selectedAllergies = mutableListOf<String>()
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("users")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allergy_screen)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        backButton = findViewById(R.id.backButton)
        skipButton = findViewById(R.id.skipButton)
        eggsAllergyButton = findViewById(R.id.eggsAllergy)
        peanutsAllergyButton = findViewById(R.id.peanutsAllergy)
        prawnsAllergyButton = findViewById(R.id.prawnsAllergy)
        shrimpsAllergyButton = findViewById(R.id.shrimpsAllergy)
        crabsAllergyButton = findViewById(R.id.crabsAllergy)
        garlicAllergyButton = findViewById(R.id.garlicAllergy)
        porkAllergyButton = findViewById(R.id.porkAllergy)
        wheatAllergyButton = findViewById(R.id.wheatAllergy)
        continueButton = findViewById(R.id.continueButton)

        // Set up click listeners for each allergy button
        setUpAllergySelection(eggsAllergyButton, "Eggs")
        setUpAllergySelection(peanutsAllergyButton, "Peanuts")
        setUpAllergySelection(prawnsAllergyButton, "Prawns")
        setUpAllergySelection(shrimpsAllergyButton, "Shrimps")
        setUpAllergySelection(crabsAllergyButton, "Crabs")
        setUpAllergySelection(garlicAllergyButton, "Garlic")
        setUpAllergySelection(porkAllergyButton, "Pork")
        setUpAllergySelection(wheatAllergyButton, "Wheat")

        backButton.setOnClickListener {
            val intent = Intent(this, CuisinesScreen::class.java)
            startActivity(intent)
            finish()
        }

        skipButton.setOnClickListener {
            val intent = Intent(this, Main::class.java)
            startActivity(intent)
            finish()
        }


        // Continue button saves selected allergies to Firebase
        continueButton.setOnClickListener {
            saveSelectedAllergies()
        }

    }

    private fun setUpAllergySelection(allergyButton: ImageButton, allergyName: String) {
        allergyButton.setOnClickListener {
            if (selectedAllergies.contains(allergyName)) {
                // If already selected, remove it from the list
                selectedAllergies.remove(allergyName)
                allergyButton.alpha = 1.0f // Reset button opacity
            } else {
                // If not selected, add it to the list
                selectedAllergies.add(allergyName)
                allergyButton.alpha = 0.5f // Indicate selection with reduced opacity
            }
        }
    }

    private fun saveSelectedAllergies() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Get current user ID
            val userId = currentUser.uid

            // Save selected allergies to 'allergies' field in users table
            val userUpdates = mapOf("allergies" to selectedAllergies)

            // Save to Firebase Realtime Database
            database.child(userId).updateChildren(userUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Allergies saved successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Main::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to save allergies: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
