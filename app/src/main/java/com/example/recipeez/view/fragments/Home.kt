package com.example.recipeez.view.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Home : Fragment() {
    private lateinit var todayRecipeImage: ImageView
    private lateinit var latestRecipesContainer: LinearLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userPreferencesRef: DatabaseReference
    private lateinit var recipesRef: DatabaseReference
    private lateinit var editorsChoice: Button
    private lateinit var forYou: Button

    // Loading state
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Views
        todayRecipeImage = view.findViewById(R.id.today_recipe_image)
        latestRecipesContainer = view.findViewById(R.id.latest_recipes_container)
        editorsChoice = view.findViewById(R.id.editors_choice_button)
        forYou = view.findViewById(R.id.for_you_button)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Authenticate user and set up Firebase references
        setupFirebaseRefs()

        // Set up button listeners
        setupButtonListeners()

        return view
    }

    private fun setupFirebaseRefs() {
        firebaseAuth.currentUser?.let {
            userPreferencesRef = databaseReference.child("users").child(it.uid)
            recipesRef = databaseReference.child("recipes")
            // Automatically load "For You" tab
            loadUserPreferences(forYouSelected = true)
        } ?: run {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        forYou.setOnClickListener {
            if (!isLoading) {
                setButtonStyle(forYou, editorsChoice)
                loadUserPreferences(forYouSelected = true)
            }
        }

        editorsChoice.setOnClickListener {
            if (!isLoading) {
                setButtonStyle(editorsChoice, forYou)
                loadUserPreferences(forYouSelected = false)
            }
        }
    }

    // Updated to load preferences based on selected tab
    private fun loadUserPreferences(forYouSelected: Boolean) {
        if (isLoading) return

        userPreferencesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Extract user preferences
                val foodType = snapshot.child("foodType").getValue(String::class.java) ?: ""
                val cuisinesList = snapshot.child("cuisines").children.mapNotNull { it.getValue(String::class.java) }
                val cuisinePreference = cuisinesList.firstOrNull() ?: ""

                // Based on tab selection, load recipes
                if (forYouSelected) {
                    loadRecipes(foodType, cuisinePreference)
                } else {
                    loadRecipes()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load preferences", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Updated to optimize querying logic
    private fun loadRecipes(foodType: String = "", cuisinePreference: String = "") {
        if (isLoading) return

        isLoading = true // Start loading

        val query = if (foodType.isNotEmpty() && cuisinePreference.isNotEmpty()) {
            recipesRef.orderByChild("cuisine").equalTo(cuisinePreference)
        } else {
            recipesRef
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipesList = snapshot.children.toList()

                // Display today's recipe as the banner
                if (recipesList.isNotEmpty()) {
                    val firstRecipe = recipesList.first()
                    val imageUrl = firstRecipe.child("imageUrl").getValue(String::class.java)
                    val vegPreference = firstRecipe.child("foodType").getValue(String::class.java) ?: ""

                    if (vegPreference == foodType || foodType.isEmpty()) {
                        loadBannerRecipes(imageUrl)
                    }

                    // Load the latest recipes excluding today's recipe
                    val latestRecipes = recipesList.drop(1)
                    loadLatestRecipes(latestRecipes, foodType)
                }

                isLoading = false // Stop loading
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load recipes", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    private fun loadBannerRecipes(imageUrl: String?) {
        imageUrl?.let {
            Glide.with(this@Home)
                .load(it)
                .into(todayRecipeImage)
        } ?: run {
            Toast.makeText(context, "No banner image available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLatestRecipes(latestRecipes: List<DataSnapshot>, foodType: String) {
        latestRecipesContainer.removeAllViews()

        latestRecipes.forEach { recipeSnapshot ->
            val imageUrl = recipeSnapshot.child("imageUrl").getValue(String::class.java)
            val vegPreference = recipeSnapshot.child("foodType").getValue(String::class.java) ?: ""

            if (vegPreference == foodType || foodType.isEmpty()) {
                val imageView = ImageView(context)
                val params = LinearLayout.LayoutParams(250, 250).apply { setMargins(8, 0, 8, 0) }
                imageView.layoutParams = params
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                Glide.with(this@Home)
                    .load(imageUrl)
                    .into(imageView)

                latestRecipesContainer.addView(imageView)
            }
        }
    }

    // Button style toggle
    private fun setButtonStyle(activeButton: Button, inactiveButton: Button) {
        activeButton.setBackgroundColor(Color.parseColor("#2F5233"))
        activeButton.setTextColor(Color.WHITE)
        inactiveButton.setBackgroundColor(Color.WHITE)
        inactiveButton.setTextColor(Color.BLACK)
    }
}
