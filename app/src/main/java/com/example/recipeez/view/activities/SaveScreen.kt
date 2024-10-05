package com.example.recipeez.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.example.recipeez.view.fragments.RecipeScreen
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

    private lateinit var sortByCuisineSpinner: Spinner
    private lateinit var sortByFoodTypeSpinner: Spinner
    // Store all recipes with recipeId, recipeName, and imageUrl
    private var recipeDataList = ArrayList<Triple<String, String, String>>()
    private var filteredRecipeDataList = ArrayList<Triple<String, String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_save_screen)
        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference.child("users")

        // Reference to the LinearLayout in the layout file
        recipeLinearLayout = findViewById(R.id.recipeLinearLayout)

        // Initialize Spinners
        sortByCuisineSpinner = findViewById(R.id.sortByCuisineSpinner)
        sortByFoodTypeSpinner = findViewById(R.id.sortByFoodTypeSpinner)

        setupSpinnerListeners()
        // Get current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadSavedRecipes(currentUser.uid)

        } else {
            Toast.makeText(this, "No user authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupSpinnerListeners() {
        // Cuisine spinner listener
        sortByCuisineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        // Food type spinner listener (veg/non-veg)
        sortByFoodTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    private fun filterRecipes() {
        // Get the selected cuisine and food type
        val selectedCuisine = sortByCuisineSpinner.selectedItem.toString()
        val selectedFoodType = sortByFoodTypeSpinner.selectedItem.toString()

        // Clear the filtered list
        filteredRecipeDataList.clear()

        // Iterate through all recipes and check the database for cuisine and foodType match
        for (recipe in recipeDataList) {
            val (recipeId, recipeName, imageUrl) = recipe

            // Fetch the recipe data from the Firebase 'recipes' table
            val recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId)
            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(recipeSnapshot: DataSnapshot) {
                    if (recipeSnapshot.exists()) {
                        val recipeCuisine = recipeSnapshot.child("cuisine").value as? String ?: "Unknown"
                        val recipeFoodType = recipeSnapshot.child("foodType").value as? String ?: "Unknown"

                        // Filter logic based on selected cuisine and food type
                        val cuisineMatch = selectedCuisine == "All" || recipeCuisine == selectedCuisine
                        val foodTypeMatch = selectedFoodType == "All" || recipeFoodType == selectedFoodType

                        if (cuisineMatch && foodTypeMatch) {
                            // Add recipe to filtered list if it matches
                            filteredRecipeDataList.add(Triple(recipeId, recipeName, imageUrl))

                            // Display the filtered recipes
                            displaySavedRecipes(filteredRecipeDataList)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SaveScreen, "Error fetching recipe data", Toast.LENGTH_SHORT).show()
                }
            })
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

            // Add up to 3 ImageViews and TextViews to the rowLayout
            for (j in 0 until 3) {
                if (i + j < recipeDataList.size) {
                    val (recipeId, recipeName, imageUrl) = recipeDataList[i + j]

                    // Create a vertical layout to hold the image and the recipe name
                    val verticalLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,  // Set width to 0 to use weight for equal distribution
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f // Each vertical layout takes equal weight
                            setMargins(8, 8, 8, 8) // Margin between items
                        }
                    }

                    // Create ImageView for each recipe
                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            300 // Fixed height for the images
                        )
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    // Load the image using Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(imageView)
                    imageView.setOnClickListener {
                        // Create an Intent to open RecipeScreen
                        val intent = Intent(this@SaveScreen, RecipeScreen::class.java)
                        // Pass the recipeId to the RecipeScreen
                        intent.putExtra("RECIPE_ID", recipeId)
                        startActivity(intent)
                    }
                    // Create TextView for the recipe name
                    val textView = TextView(this).apply {
                        text = recipeName
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // Add ImageView and TextView to the vertical layout
                    verticalLayout.addView(imageView)
                    verticalLayout.addView(textView)

                    // Add the vertical layout to the rowLayout
                    rowLayout.addView(verticalLayout)
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

            // Add the rowLayout to the containerRecipe
            recipeLinearLayout.addView(rowLayout)
        }
    }





}