package com.example.recipeez.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class searchScreen : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var linearLayoutRecipes: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_screen)
        database = FirebaseDatabase.getInstance().getReference("recipes")

        // Initialize the LinearLayout by finding it by its ID
        linearLayoutRecipes = findViewById(R.id.linearLayoutRecipes)

        // Fetch recipes from Firebase and display them
        fetchRecipes()
    }

    private fun fetchRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear any previous views before adding new ones
                    linearLayoutRecipes.removeAllViews()

                    // List to hold the image URLs fetched from Firebase
                    val recipeDataList = ArrayList<Pair<String, String>>()

                    // Iterate over each recipe in the snapshot and get the "imageUrl" value
                    for (recipeSnapshot in snapshot.children) {
                        val imageUrl = recipeSnapshot.child("imageUrl").value.toString()
                        val recipeId =
                            recipeSnapshot.key.toString() // Get the unique ID of the recipe
                        recipeDataList.add(Pair(imageUrl, recipeId))
                    }

                    // Display the fetched images in rows of 3
                    displayRecipes(recipeDataList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@searchScreen, "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRecipes(recipeDataList: List<Pair<String, String>>) {
        // Variable to hold the row layout (a LinearLayout with horizontal orientation)
        var rowLayout: LinearLayout? = null

        for (i in recipeDataList.indices) {
            // For every 3rd image, create a new row
            if (i % 3 == 0) {
                // Create a new LinearLayout to act as the row container
                rowLayout = LinearLayout(this)
                rowLayout.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                rowLayout.orientation = LinearLayout.HORIZONTAL
                rowLayout.gravity = Gravity.CENTER

                // Add this new row to the main vertical layout
                linearLayoutRecipes.addView(rowLayout)
            }

            // Create an ImageView for each recipe image
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(
                0, // Width set to 0, because weight will distribute space equally
                300 // Fixed height of 300 pixels for each image
            )
            params.weight = 1f // Distribute the space equally between three images
            params.setMargins(8, 8, 8, 8) // Add margin around each image
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            val (imageUrl, recipeId) = recipeDataList[i]

            // Load the image using Glide
            Glide.with(this).load(imageUrl).into(imageView)
            // Set click listener on the ImageView
            imageView.setOnClickListener {
                // Create an Intent to open RecipeScreen
                val intent = Intent(this@searchScreen, RecipieScreen::class.java)
                // Pass the recipeId to the RecipeScreen
                intent.putExtra("RECIPE_ID", recipeId)
                startActivity(intent)
            }
            // Add the ImageView to the current row
            rowLayout?.addView(imageView)


        }
    }
}