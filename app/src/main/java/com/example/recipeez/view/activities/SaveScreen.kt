package com.example.recipeez.view.activities

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SaveScreen : AppCompatActivity() {

    private lateinit var usersRef: DatabaseReference
    private lateinit var recipeLinearLayout: LinearLayout
    private lateinit var auth: FirebaseAuth

    // Store all recipes with recipeId, recipeName, and imageUrl
    private var recipeDataList = ArrayList<Triple<String, String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_save_screen)
        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference.child("users")

        // Reference to the LinearLayout in the layout file
        recipeLinearLayout = findViewById(R.id.recipeLinearLayout)

        // Get current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadSavedRecipes(currentUser.uid)
        } else {
            Toast.makeText(this, "No user authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadSavedRecipes(userId: String) {
        // Access the savedRecipes under the userId
        usersRef.child(userId).child("savedRecipes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear previous data
                    recipeDataList.clear()

                    // Iterate through saved recipes and retrieve data from "recipes" table
                    for (recipeSnapshot in snapshot.children) {
                        val recipeId = recipeSnapshot.value as? String

                        recipeId?.let {
                            // Fetch the recipe data (including name and image URL) from the "recipes" table
                            val recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId)
                            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(recipeSnapshot: DataSnapshot) {
                                    val recipeName = recipeSnapshot.child("name").value as? String ?: "Unknown Recipe"
                                    val imageUrl = recipeSnapshot.child("imageUrl").value as? String ?: ""

                                    // Store the data in the recipeDataList as a Triple
                                    recipeDataList.add(Triple(recipeId, recipeName, imageUrl))

                                    // After adding the data, call displaySavedRecipes with the updated list
                                    displaySavedRecipes(recipeDataList)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@SaveScreen, "Failed to load recipe data", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else {
                    Toast.makeText(this@SaveScreen, "No saved recipes found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SaveScreen, "Failed to load saved recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun displaySavedRecipes(recipeDataList: ArrayList<Triple<String, String, String>>) {
        // Remove all previous views in the recipeLinearLayout
        recipeLinearLayout.removeAllViews()

        // Loop through the recipeDataList and create rows dynamically
        for (i in recipeDataList.indices step 3) {
            // Create a new horizontal LinearLayout for each row
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 0) // Margin between rows
                }
            }

            // Add up to 3 ImageViews to the rowLayout
            for (j in 0 until 3) {
                if (i + j < recipeDataList.size) {
                    val (recipeId, recipeName, imageUrl) = recipeDataList[i + j]

                    // Create ImageView for each recipe
                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0, // Set width to 0 to use weight for equal distribution
                            300 // Fixed height for the images
                        ).apply {
                            weight = 1f // Each ImageView takes equal weight
                            setMargins(8, 8, 8, 8) // Margin between images
                        }
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    // Load the image using Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(imageView)

                    // Set click listener for the image
                    imageView.setOnClickListener {
                        Toast.makeText(this, "Selected Recipe: $recipeName", Toast.LENGTH_SHORT).show()
                        // Handle image click events, e.g., open recipe details
                    }

                    // Add the ImageView to the rowLayout
                    rowLayout.addView(imageView)
                } else {
                    // Add empty space for missing items to keep 3 items per row
                    val emptyView = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            300 // Same height as ImageView to maintain consistency
                        ).apply {
                            weight = 1f // Equal space as ImageView
                        }
                    }
                    rowLayout.addView(emptyView)
                }
            }

            // Add the rowLayout to the recipeLinearLayout
            recipeLinearLayout.addView(rowLayout)
        }
    }





}