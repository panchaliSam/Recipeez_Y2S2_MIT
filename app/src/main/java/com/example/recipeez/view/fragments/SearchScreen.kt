package com.example.recipeez.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.database.*

class SearchScreen : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var linearLayoutRecipes: LinearLayout
    private lateinit var searchView: SearchView
    private var recipeDataList = ArrayList<Triple<String, String, String>>() // Store all recipes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_screen, container, false)

        // Initialize views using safe calls to avoid NullPointerException
        linearLayoutRecipes = view.findViewById(R.id.linearLayoutRecipes)
        searchView = view.findViewById(R.id.searchView)

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("recipes")

        // Fetch recipes from Firebase
        fetchRecipes()

        // Implement search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterRecipes(it) }
                return true
            }
        })

        return view
    }

    private fun fetchRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear any previous views before adding new ones
                    linearLayoutRecipes.removeAllViews()

                    // List to hold the image URLs fetched from Firebase
                    recipeDataList.clear()  // Clear the list to avoid duplicates

                    // Iterate over each recipe in the snapshot and get the "imageUrl" value
                    for (recipeSnapshot in snapshot.children) {
                        val imageUrl = recipeSnapshot.child("imageUrl").value.toString()
                        val recipeId = recipeSnapshot.key.toString() // Get the unique ID of the recipe
                        val recipeTitle = recipeSnapshot.child("name").value.toString() // Fetch the recipe title
                        recipeDataList.add(Triple(imageUrl, recipeId, recipeTitle))
                    }

                    // Display the fetched images in rows of 3
                    displayRecipes(recipeDataList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterRecipes(query: String) {
        linearLayoutRecipes.removeAllViews()
        val filteredList = recipeDataList.filter { it.third.contains(query, ignoreCase = true) }
        displayRecipes(filteredList)
    }

    private fun displayRecipes(recipeDataList: List<Triple<String, String, String>>) {
        var rowLayout: LinearLayout? = null

        for (i in recipeDataList.indices) {
            if (i % 3 == 0) {
                rowLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                }
                linearLayoutRecipes.addView(rowLayout)
            }

            val recipeContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, // Width set to 0, because weight will distribute space equally
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val (imageUrl, recipeId, recipeTitle) = recipeDataList[i]

            // Ensure the image URL is valid or load a default image
            Glide.with(requireContext())
                .load(imageUrl)
                .error(R.drawable.spanish) // Load a default image in case of an error
                .into(imageView)

            imageView.setOnClickListener {
                val intent = Intent(requireContext(), RecipeScreen::class.java)
                intent.putExtra("RECIPE_ID", recipeId)
                startActivity(intent)
            }

            val textView = TextView(requireContext()).apply {
                text = recipeTitle
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            recipeContainer.addView(imageView)
            recipeContainer.addView(textView)

            rowLayout?.addView(recipeContainer)
        }
    }
}
