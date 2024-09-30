package com.example.recipeez.view.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class RecipieScreen : AppCompatActivity() {
    private lateinit var reciepeRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference // Reference to the 'users' table
    private lateinit var auth: FirebaseAuth // Firebase Authentication instance
    private lateinit var userId: String // User ID of the authenticated user
    private lateinit var recipeImageView: ImageView
    private lateinit var likeButton: ImageView
    private lateinit var saveButton: ImageView
    private lateinit var recipeVideoView: VideoView
    private lateinit var recipeTitle: TextView
    private lateinit var preparationTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var cookTimeView: TextView
    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var stepsContainer: LinearLayout
    private lateinit var tabs: TabLayout



    private var isLiked = false
    private var isSaved = false
    private var isShowingIngredients = true // Track the current state
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipie_screen)

        // Retrieve the recipe ID from the Intent
        val recipeId = intent.getStringExtra("RECIPE_ID")
        recipeImageView = findViewById(R.id.recipe_image)
        recipeVideoView = findViewById(R.id.recipe_video)
        recipeTitle = findViewById(R.id.recipe_title)
        preparationTimeView = findViewById(R.id.prep_time)
        totalTimeView = findViewById(R.id.total_time)
        cookTimeView = findViewById(R.id.cook_time)
        ingredientsContainer = findViewById(R.id.ingredients_container)
        stepsContainer = findViewById(R.id.steps_container)
        likeButton = findViewById(R.id.like_button)
        saveButton = findViewById(R.id.save_button)

        tabs = findViewById(R.id.tabs)

        // Set initial visibility
        ingredientsContainer.visibility = View.VISIBLE
        stepsContainer.visibility = View.GONE
        // Set up the click listener for Like button
        likeButton.setOnClickListener {
            // Toggle the like state
            isLiked = !isLiked

            // Change the drawable based on the current state
            if (isLiked) {
                // Set the red filled heart when liked
                likeButton.setImageResource(R.drawable.heart_filled)
            } else {
                // Set back the unfilled heart when unliked
                likeButton.setImageResource(R.drawable.love)
            }
        }

        // Set up the click listener for Save button
        saveButton.setOnClickListener {
            isSaved = !isSaved
            if (isSaved) {
                saveButton.setImageResource(R.drawable.save_filled)
                // Add recipe to user's saved recipes
                recipeId?.let { id -> saveRecipeToUser(id) }
            } else {
                saveButton.setImageResource(R.drawable.save)
            }
        }
        // Setup TabSelectedListener
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        ingredientsContainer.visibility = View.VISIBLE
                        stepsContainer.visibility = View.GONE
                    }

                    1 -> {
                        ingredientsContainer.visibility = View.GONE
                        stepsContainer.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Initialize FirebaseAuth and Database references
        auth = FirebaseAuth.getInstance()
        reciepeRef = FirebaseDatabase.getInstance().getReference("recipes")
        usersRef = FirebaseDatabase.getInstance().getReference("users")

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid // Retrieve the authenticated user's ID
        } else {
            // If not authenticated, show a message and finish the activity
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish() // Exit the activity if user is not authenticated
            return
        }

        // Fetch and display the recipe data using the recipeId
        recipeId?.let {
            getRecipeData(recipeId)
        }

    }


    fun getRecipeData(recipeId: String) {
        reciepeRef.child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the data as a Map
                    val recipeData = snapshot.value as Map<String, Any>

                    // Access individual fields from the map
                    val name = recipeData["name"] as? String
                    val description = recipeData["description"] as? String
                    val preparationTime = recipeData["preparationTime"] as? String
                    val totalTime = recipeData["totalTime"] as? String
                    val cookingTime = recipeData["cookingTime"] as? String
                    val imageUrl = recipeData["imageUrl"] as? String
                    val videoUrl = recipeData["videoUrl"] as? String
                    val ingredientsList = recipeData["ingredientsList"] as? List<String> ?: emptyList()
                    val quantityList = recipeData["quantityList"] as? List<String> ?: emptyList()

                    val stepsList = recipeData["stepsList"] as? List<String> ?: emptyList()

                    // Populate the UI with the retrieved data
                    populateUI(
                        name,
                        description,
                        preparationTime,
                        totalTime,
                        cookingTime,
                        imageUrl,
                        videoUrl,
                        ingredientsList,
                        stepsList,
                        quantityList
                    )
                } else {
                    Log.e("RecipeError", "Recipe not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecipeError", "Database error: ${error.message}")
            }
        })
    }
    // Function to save the recipe to the user's saved recipes
    private fun saveRecipeToUser(recipeId: String) {
        val userSavedRef = usersRef.child(userId).child("savedRecipes")

        // Add the recipe ID to the user's saved recipes list
        userSavedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedRecipes = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                val updatedRecipes = savedRecipes?.toMutableList() ?: mutableListOf()

                if (!updatedRecipes.contains(recipeId)) {
                    updatedRecipes.add(recipeId)
                    userSavedRef.setValue(updatedRecipes)
                    Toast.makeText(this@RecipieScreen, "Recipe saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RecipieScreen, "Recipe already saved", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SaveRecipeError", "Error: ${error.message}")
            }
        })
    }
    private fun populateUI(
        name: String?,
        description: String?,
        preparationTime: String?,
        totalTime: String?,
        cookingTime: String?,
        imageUrl: String?,
        videoUrl: String?,
        ingredientsList: List<String>,
        stepsList: List<String>,
        quantityList: List<String>
    ) {
        recipeTitle.text = name
        preparationTimeView.text = "Prep Time: $preparationTime"
        totalTimeView.text = "Total Time: $totalTime"
        cookTimeView.text = "Cook Time: $cookingTime"

        // Handle image URL
        if (!imageUrl.isNullOrEmpty()) {
            recipeImageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .into(recipeImageView)
        } else {
            recipeImageView.visibility = View.GONE // Hide the ImageView if no image is available
        }
        // Handle video URL
        if (!videoUrl.isNullOrEmpty()) {
            recipeVideoView.visibility = View.VISIBLE

            // Set video URI and initialize MediaController
            val videoUri = Uri.parse(videoUrl)
            recipeVideoView.setVideoURI(videoUri)
            val mediaController = MediaController(this)
            recipeVideoView.setMediaController(mediaController)
            mediaController.setAnchorView(recipeVideoView)

            // Start video playback (optional)
            recipeVideoView.start()
        } else {
            recipeVideoView.visibility = View.GONE // Hide the VideoView if no video is available
        }
// Add ingredients and quantities dynamically
        ingredientsContainer.removeAllViews() // Clear old views if any
        for (i in ingredientsList.indices) {
            // Create a horizontal LinearLayout for each ingredient and its quantity
            val ingredientLayout = LinearLayout(this)
            ingredientLayout.orientation = LinearLayout.HORIZONTAL
            ingredientLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
// Create and add the ingredient TextView
            val ingredientView = TextView(this)
            ingredientView.text = ingredientsList[i] // Set the ingredient name
            ingredientView.textSize = 16f

            // Set the width of the ingredient to wrap and align it to the right
            val ingredientParams = LinearLayout.LayoutParams(
                0, // 0 width to use weight for layout control
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Weight to push it to the right
            )
            ingredientParams.gravity = android.view.Gravity.END // Align to the right
            ingredientView.layoutParams = ingredientParams

            // Add ingredient to the layout
            ingredientLayout.addView(ingredientView)

            // Add the ingredientLayout to the parent container
            ingredientsContainer.addView(ingredientLayout)
            // Create and add the quantity TextView
            val quantityView = TextView(this)
            quantityView.text = quantityList[i] // Set the quantity
            quantityView.textSize = 16f

            // Set the width of quantity to wrap and align it to the left
            val quantityParams = LinearLayout.LayoutParams(
                0, // 0 width to use weight for layout control
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Weight to push it to the left
            )
            quantityView.layoutParams = quantityParams
            ingredientLayout.addView(quantityView) // Add quantity to the layout


        }


        // Add steps dynamically
        stepsContainer.removeAllViews() // Clear old views if any
        for (step in stepsList) {
            val stepView = TextView(this)
            stepView.text = step
            stepView.textSize = 16f
            stepView.setPadding(0, 8, 0, 8)
            stepsContainer.addView(stepView)
        }

    }


}