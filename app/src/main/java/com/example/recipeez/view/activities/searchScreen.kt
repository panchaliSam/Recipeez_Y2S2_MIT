package com.example.recipeez.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private lateinit var searchView: SearchView
    private var recipeDataList = ArrayList<Triple<String, String, String>>() // Store all recipes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_screen)
        database = FirebaseDatabase.getInstance().getReference("recipes")

        // Initialize the LinearLayout by finding it by its ID
        linearLayoutRecipes = findViewById(R.id.linearLayoutRecipes)
        searchView = findViewById(R.id.searchView)

        // Fetch recipes from Firebase and display them
        fetchRecipes()

        // Implement search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Filter recipes when the search is submitted
                query?.let { filterRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter recipes as the user types
                newText?.let { filterRecipes(it) }
                return true
            }
        })
    }

    private fun fetchRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear any previous views before adding new ones
                    linearLayoutRecipes.removeAllViews()

                    // List to hold the image URLs fetched from Firebase
//                    val recipeDataList = ArrayList<Triple<String, String, String>>()

                    // Iterate over each recipe in the snapshot and get the "imageUrl" value
                    for (recipeSnapshot in snapshot.children) {
                        val imageUrl = recipeSnapshot.child("imageUrl").value.toString()
                        val recipeId =
                            recipeSnapshot.key.toString() // Get the unique ID of the recipe
                        val recipeTitle =
                            recipeSnapshot.child("name").value.toString() // Fetch the recipe title
                        recipeDataList.add(Triple(imageUrl, recipeId, recipeTitle))
                    }

                    // Display the fetched images in rows of 3
                    displayRecipes(recipeDataList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@searchScreen, "Failed to load recipes", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
    private fun filterRecipes(query: String) {
        linearLayoutRecipes.removeAllViews()
        val filteredList = recipeDataList.filter { it.third.contains(query, ignoreCase = true) }
        displayRecipes(filteredList)
    }
    private fun displayRecipes(recipeDataList: List<Triple<String, String, String>>) {
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
// Create a container for each recipe (to hold both ImageView and TextView)
            val recipeContainer = LinearLayout(this)
            recipeContainer.orientation = LinearLayout.VERTICAL
            recipeContainer.layoutParams = LinearLayout.LayoutParams(
                0, // Width set to 0, because weight will distribute space equally
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f // Distribute the space equally between three containers
                setMargins(8, 8, 8, 8) // Add margin around each container
            }

            // Create an ImageView for each recipe image
            val imageView = ImageView(this)
            val imageParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300 // Fixed height of 300 pixels for each image
            )
            imageView.layoutParams = imageParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            val (imageUrl, recipeId, recipeTitle) = recipeDataList[i]

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
            // Create a TextView for the recipe title
            val textView = TextView(this)
            textView.text = recipeTitle // Set the title text
            textView.gravity = Gravity.CENTER
            textView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Add the ImageView and TextView to the recipe container
            recipeContainer.addView(imageView)
            recipeContainer.addView(textView)
            // Add the ImageView to the current row
            rowLayout?.addView(recipeContainer)


        }
    }
}