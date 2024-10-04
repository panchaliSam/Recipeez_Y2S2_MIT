package com.example.recipeez.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class RecipeScreen : AppCompatActivity() {
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
    private lateinit var shareButton: ImageView
    private lateinit var bottomNavigationView: BottomNavigationView


    private var isLiked = false
    private var isSaved = false
    private var isShowingIngredients = true // Track the current state

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_recipe_screen)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadFragment(Home())  // Load Home fragment
                    true
                }
                R.id.profile -> {
                    loadFragment(UserAccount())  // Load Profile fragment
                    true
                }
                R.id.search -> {
                    loadFragment(SearchScreen())  // Load Search fragment
                    true
                }
                else -> false  // Return false for unrecognized menu items
            }
        }

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
        shareButton = findViewById(R.id.share_button)
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
                likeButton.setImageResource(R.drawable.heartfilled)
                recipeId?.let { id -> likeRecipeToUser(id) }
            } else {
                // Set back the unfilled heart when unliked
                likeButton.setImageResource(R.drawable.love)
            }
        }

        // Set up the click listener for Save button
        saveButton.setOnClickListener {
            isSaved = !isSaved
            if (isSaved) {
                saveButton.setImageResource(R.drawable.savefilled)
                // Add recipe to user's saved recipes
                recipeId?.let { id -> saveRecipeToUser(id) }
            } else {
                saveButton.setImageResource(R.drawable.save)
            }
        }
        shareButton.setOnClickListener {
            shareRecipe()
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
            checkIfRecipeIsSaved(recipeId)  // <-- Check if the recipe is saved
            getRecipeData(recipeId)
            checkIfRecipeIsLiked(recipeId)
        }

    }

    private fun shareRecipe() {
        // Gather the recipe details
        val recipeName = recipeTitle.text.toString()
        val preparationTime = preparationTimeView.text.toString()
        val totalTime = totalTimeView.text.toString()
        val cookTime = cookTimeView.text.toString()

        val ingredients = ingredientsContainer.children
            .map { (it as TextView).text.toString() }
            .joinToString(separator = "\n")

        val steps = stepsContainer.children
            .map { (it as TextView).text.toString() }
            .joinToString(separator = "\n")

        // Create the share message
        val shareMessage = """
        Recipe: $recipeName
        Preparation Time: $preparationTime
        Total Time: $totalTime
        Cook Time: $cookTime

        Ingredients:
        $ingredients

        Steps:
        $steps
    """.trimIndent()

        // Create a share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        // Start the share intent
        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"))
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
                    val ingredientsList =
                        recipeData["ingredientsList"] as? List<String> ?: emptyList()
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
                val savedRecipes =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                val updatedRecipes = savedRecipes?.toMutableList() ?: mutableListOf()

                if (!updatedRecipes.contains(recipeId)) {
                    updatedRecipes.add(recipeId)
                    userSavedRef.setValue(updatedRecipes)
                    Toast.makeText(this@RecipeScreen, "Recipe saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RecipeScreen, "Recipe already saved", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SaveRecipeError", "Error: ${error.message}")
            }
        })
    }

    // Function to check if the recipe is already saved by the user
    private fun checkIfRecipeIsSaved(recipeId: String) {
        val userSavedRef = usersRef.child(userId).child("savedRecipes")
        userSavedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedRecipes =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                val updatedRecipes = savedRecipes?.toMutableList() ?: mutableListOf()

                if (updatedRecipes.contains(recipeId)) {
                    isSaved = true
                    saveButton.setImageResource(R.drawable.savefilled)  // Set the saved icon
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SaveCheckError", "Error: ${error.message}")
            }
        })
    }

    private fun likeRecipeToUser(recipeId: String) {
        val userSavedRef = usersRef.child(userId).child("likedRecipes")

        // Add the recipe ID to the user's saved recipes list
        userSavedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedRecipes =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                val updatedRecipes = likedRecipes?.toMutableList() ?: mutableListOf()

                if (!updatedRecipes.contains(recipeId)) {
                    updatedRecipes.add(recipeId)
                    userSavedRef.setValue(updatedRecipes)
                    Toast.makeText(
                        this@RecipeScreen,
                        "Recipe added to wishlist",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@RecipeScreen,
                        "Recipe already added to wishlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LikeRecipeError", "Error: ${error.message}")
            }
        })
    }

    // Function to check if the recipe is already saved by the user
    private fun checkIfRecipeIsLiked(recipeId: String) {
        val userSavedRef = usersRef.child(userId).child("likedRecipes")
        userSavedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedRecipes =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                val updatedRecipes = likedRecipes?.toMutableList() ?: mutableListOf()

                if (updatedRecipes.contains(recipeId)) {
                    isSaved = true
                    likeButton.setImageResource(R.drawable.heartfilled)  // Set the saved icon
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SaveCheckError", "Error: ${error.message}")
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
            recipeImageView.visibility = View.GONE
        }

        // Handle video URL
        if (!videoUrl.isNullOrEmpty()) {
            recipeVideoView.visibility = View.VISIBLE
            recipeVideoView.setVideoURI(Uri.parse(videoUrl))
            val mediaController = MediaController(this)
            mediaController.setAnchorView(recipeVideoView)
            recipeVideoView.setMediaController(mediaController)
            recipeVideoView.requestFocus()
            recipeVideoView.start() // Optional: auto-start the video
        } else {
            recipeVideoView.visibility = View.GONE
        }

        // Populate ingredients
        ingredientsContainer.removeAllViews()
        ingredientsList.forEachIndexed { index, ingredient ->
            val ingredientView = TextView(this).apply {
                text =
                    "${quantityList.getOrElse(index) { "" }} $ingredient" // Display quantity if available
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 8, 16, 8)
            }
            ingredientsContainer.addView(ingredientView)
            ingredientView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // Populate steps
        stepsContainer.removeAllViews()
        stepsList.forEach { step ->
            val stepView = TextView(this).apply {
                text = step
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 8, 16, 8)
            }
            stepsContainer.addView(stepView)
            stepView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        // Get the FragmentManager and start a transaction
        val transaction = supportFragmentManager.beginTransaction()
        // Replace the container view with the new fragment
        transaction.replace(R.id.fragment_container, fragment) // Make sure you have a container in your layout
        // Commit the transaction
        transaction.commit()
        return true
    }

}

