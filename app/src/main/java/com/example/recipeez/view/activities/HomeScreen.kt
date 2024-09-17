package com.example.recipeez.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*


class HomeScreen : AppCompatActivity() {
    private lateinit var todayRecipeImage: ImageView
    private lateinit var latestRecipesContainer: LinearLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userPreferencesRef: DatabaseReference
    private lateinit var recipesRef: DatabaseReference
    private lateinit var editorsChoice: Button
    private lateinit var forYou: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        todayRecipeImage = findViewById(R.id.today_recipe_image)
        latestRecipesContainer = findViewById(R.id.latest_recipes_container)
        editorsChoice = findViewById(R.id.editors_choice_button)
        forYou = findViewById(R.id.for_you_button)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth.currentUser?.let {
            userPreferencesRef = databaseReference.child("users").child(it.uid)
            recipesRef = databaseReference.child("recipes")
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        checkUserAuthentication()
        setupButtonListeners()
    }

    private fun checkUserAuthentication() {
        firebaseAuth.currentUser?.let {
            loadUserPreferences(forYouSelected = true) // Default to "For You" tab
        } ?: run {
            Toast.makeText(this, "Please log in to view recipes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        forYou.setOnClickListener {
            setButtonStyle(forYou, editorsChoice)
            loadUserPreferences(forYouSelected = true)
        }

        editorsChoice.setOnClickListener {
            setButtonStyle(editorsChoice, forYou)
            loadUserPreferences(forYouSelected = false)
        }
    }

    private fun loadUserPreferences(forYouSelected: Boolean) {
        userPreferencesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodType = snapshot.child("foodType").getValue(String::class.java) ?: ""
                val cuisinesList = snapshot.child("cuisines").children.mapNotNull { it.getValue(String::class.java) }
                val cuisinePreference = cuisinesList.firstOrNull() ?: ""

                if (forYouSelected) {
                    loadRecipes(foodType, cuisinePreference)
                } else {
                    loadRecipes()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeScreen, "Failed to load preferences", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadRecipes(foodType: String = "", cuisinePreference: String = "") {
        val query = if (foodType.isNotEmpty() && cuisinePreference.isNotEmpty()) {
            recipesRef.orderByChild("cuisine").equalTo(cuisinePreference)
        } else {
            recipesRef
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipesList = snapshot.children.toList()

                if (recipesList.isNotEmpty()) {
                    // First recipe goes to loadBannerRecipes
                    val firstRecipe = recipesList.first()
                    val imageUrl = firstRecipe.child("imageUrl").getValue(String::class.java)
                    val vegPreference = firstRecipe.child("foodType").getValue(String::class.java) ?: ""

                    if (vegPreference == foodType || foodType.isEmpty()) {
                        loadBannerRecipes(imageUrl)
                    }

                    // Rest of the recipes go to loadLatestRecipes
                    val latestRecipes = recipesList.drop(1)
                    loadLatestRecipes(latestRecipes, foodType)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeScreen, "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBannerRecipes(imageUrl: String?) {
        imageUrl?.let {
            Glide.with(this@HomeScreen)
                .load(it)
                .into(todayRecipeImage)
        } ?: run {
            Toast.makeText(this@HomeScreen, "No banner image available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLatestRecipes(latestRecipes: List<DataSnapshot>, foodType: String) {
        latestRecipesContainer.removeAllViews()

        latestRecipes.forEach { recipeSnapshot ->
            val imageUrl = recipeSnapshot.child("imageUrl").getValue(String::class.java)
            val vegPreference = recipeSnapshot.child("foodType").getValue(String::class.java) ?: ""

            if (vegPreference == foodType || foodType.isEmpty()) {
                val imageView = ImageView(this@HomeScreen)
                val params = LinearLayout.LayoutParams(250, 250).apply { setMargins(8, 0, 8, 0) }
                imageView.layoutParams = params
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                Glide.with(this@HomeScreen)
                    .load(imageUrl)
                    .into(imageView)

                latestRecipesContainer.addView(imageView)
            }
        }
    }

    private fun setButtonStyle(activeButton: Button, inactiveButton: Button) {
        activeButton.setBackgroundColor(Color.parseColor("#2F5233"))
        activeButton.setTextColor(Color.WHITE)
        inactiveButton.setBackgroundColor(Color.WHITE)
        inactiveButton.setTextColor(Color.BLACK)
    }
}

