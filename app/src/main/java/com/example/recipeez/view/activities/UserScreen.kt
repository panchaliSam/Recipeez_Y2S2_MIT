package com.example.recipeez.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
private lateinit var usersRef: DatabaseReference
private lateinit var containerCookBook: LinearLayout
private lateinit var containerRecipe: LinearLayout
    private lateinit var cookBookLabel: TextView
private lateinit var firebaseAuth: FirebaseAuth
private lateinit var firebaseDatabase: FirebaseDatabase
private lateinit var tabs: TabLayout
private var recipeDataList = ArrayList<Triple<String, String, String>>()
class UserScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_screen)
// Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference.child("users")

        // Reference to the LinearLayout containerCookBook
        containerCookBook = findViewById(R.id.containerCookBook)

        containerRecipe = findViewById(R.id.containerRecipe)
        cookBookLabel = findViewById(R.id.cookBookLabel)

        tabs = findViewById(R.id.tabLayout)

        // Get the current user's ID (Assuming user is already authenticated)
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            fetchUserRecipes(currentUserId)
            loadLikedRecipes(currentUserId)
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        containerCookBook.visibility = View.VISIBLE
                        containerRecipe.visibility = View.GONE
                    }

                    1 -> {
                        containerCookBook.visibility = View.GONE
                        containerRecipe.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadLikedRecipes(userId: String) {
        // Access the savedRecipes under the userId
        usersRef.child(userId).child("likedRecipes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Clear previous data
                        recipeDataList.clear()

                        // Iterate through saved recipes and retrieve data from "recipes" table
                        for (recipeSnapshot in snapshot.children) {
                            val recipeId = recipeSnapshot.value as? String

                            recipeId?.let {
                                // Fetch the recipe data (including name and image URL) from the "recipes" table
                                val recipeRef =
                                    FirebaseDatabase.getInstance().getReference("recipes")
                                        .child(recipeId)
                                recipeRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(recipeSnapshot: DataSnapshot) {
                                        val recipeName =
                                            recipeSnapshot.child("name").value as? String
                                                ?: "Unknown Recipe"
                                        val imageUrl =
                                            recipeSnapshot.child("imageUrl").value as? String ?: ""

                                        // Store the data in the recipeDataList as a Triple
                                        recipeDataList.add(Triple(recipeId, recipeName, imageUrl))

                                        // After adding the data, call displaySavedRecipes with the updated list
                                        displaySavedRecipes(recipeDataList)
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@UserScreen,
                                            "Failed to load recipe data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@UserScreen,
                            "No saved recipes found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@UserScreen,
                        "Failed to load saved recipes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun fetchUserRecipes(userId: String) {
        val recipeRef = firebaseDatabase.getReference("recipes")

        // Query to fetch all recipes for the current user
        val query = recipeRef.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Iterate through the recipes
                for (recipeSnapshot in dataSnapshot.children) {
                    val recipeId = recipeSnapshot.value as? String ?: "Unknown RecipeId"
                    val imageUrl = recipeSnapshot.child("imageUrl").getValue(String::class.java)
                    val recipeTitle =
                        recipeSnapshot.child("name").value as? String
                            ?: "Unknown Recipe"
                    // Check if imageUrl is not null, then display it
                    if (imageUrl != null) {
                        displayRecipeImage(imageUrl,recipeTitle,recipeId)
                    } else {
                        Toast.makeText(this@UserScreen, "No image URL found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@UserScreen,
                    "Error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayRecipeImage(imageUrl: String, recipeTitle: String,recipeId: String) {
        // Variable to keep track of current row
        var currentRowLayout: LinearLayout? = null
        var imageCounter = 0

        if (imageCounter % 3 == 0) {
            // Create a new horizontal LinearLayout for the row
            currentRowLayout = LinearLayout(this)
            currentRowLayout.orientation = LinearLayout.HORIZONTAL
            currentRowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Add the new row to the containerCookBook
            containerCookBook.addView(currentRowLayout)
        }

        // Create a vertical LinearLayout to hold ImageView and TextView
        val verticalLayout = LinearLayout(this)
        verticalLayout.orientation = LinearLayout.VERTICAL
        verticalLayout.layoutParams = LinearLayout.LayoutParams(
            0,  // Width is set to 0 because weight will be used to divide space equally
            LinearLayout.LayoutParams.WRAP_CONTENT,  // Height is wrap content for vertical layout
            1f  // Weight of 1 to equally distribute the images in a row
        )

        // Create ImageView dynamically
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            300  // Height can be adjusted based on your design
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Load the image using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
        imageView.setOnClickListener {
            // Create an Intent to open RecipeScreen
            val intent = Intent(this@UserScreen, RecipieScreen::class.java)
            // Pass the recipeId to the RecipeScreen
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }
        // Create TextView dynamically for the recipe title
        val textView = TextView(this)
        textView.text = recipeTitle
        textView.gravity = Gravity.CENTER
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Add ImageView and TextView to the vertical layout
        verticalLayout.addView(imageView)
        verticalLayout.addView(textView)

        // Add the vertical layout to the current row layout
        currentRowLayout?.addView(verticalLayout)

        // Increment the image counter
        imageCounter++
    }


    private fun displaySavedRecipes(recipeDataList: ArrayList<Triple<String, String, String>>) {
        // Remove all previous views in the recipeLinearLayout
        containerRecipe.removeAllViews()

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
                        val intent = Intent(this@UserScreen, RecipieScreen::class.java)
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
            containerRecipe.addView(rowLayout)
        }
    }

}