package com.example.recipeez.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecipeScreen : Fragment() {
    private lateinit var recipeRef: DatabaseReference

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitle: TextView
    private lateinit var preparationTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var cookTimeView: TextView
    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var stepsContainer: LinearLayout
    private lateinit var tabs: TabLayout

    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getString(ARG_PARAM1) // Assuming recipeId is passed as param1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_screen, container, false)
        initializeViews(view)

        // Initialize Firebase Database reference
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes")

        // Set up TabLayout
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

        // Fetch and display the recipe data using the recipeId
        recipeId?.let {
            getRecipeData(recipeId!!)
        }

        return view
    }

    private fun initializeViews(view: View) {
        recipeImageView = view.findViewById(R.id.recipe_image)
        recipeTitle = view.findViewById(R.id.recipe_title)
        preparationTimeView = view.findViewById(R.id.prep_time)
        totalTimeView = view.findViewById(R.id.total_time)
        cookTimeView = view.findViewById(R.id.cook_time)
        ingredientsContainer = view.findViewById(R.id.ingredients_container)
        stepsContainer = view.findViewById(R.id.steps_container)
        tabs = view.findViewById(R.id.tabs)

        // Set initial visibility
        ingredientsContainer.visibility = View.VISIBLE
        stepsContainer.visibility = View.GONE
    }

    private fun getRecipeData(recipeId: String) {
        recipeRef.child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the data as a Map
                    val recipeData = snapshot.value as Map<String, Any>

                    // Access individual fields from the map
                    val name = recipeData["name"] as? String
                    val preparationTime = recipeData["preparationTime"] as? String
                    val totalTime = recipeData["totalTime"] as? String
                    val cookingTime = recipeData["cookingTime"] as? String
                    val imageUrl = recipeData["imageUrl"] as? String
                    val ingredientsList =
                        recipeData["ingredientsList"] as? List<String> ?: emptyList()
                    val quantityList = recipeData["quantityList"] as? List<String> ?: emptyList()
                    val stepsList = recipeData["stepsList"] as? List<String> ?: emptyList()

                    // Populate the UI with the retrieved data
                    populateUI(
                        name,
                        preparationTime,
                        totalTime,
                        cookingTime,
                        imageUrl,
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

    private fun populateUI(
        name: String?,
        preparationTime: String?,
        totalTime: String?,
        cookingTime: String?,
        imageUrl: String?,
        ingredientsList: List<String>,
        stepsList: List<String>,
        quantityList: List<String>
    ) {
        recipeTitle.text = name
        preparationTimeView.text = "Prep Time: $preparationTime"
        totalTimeView.text = "Total Time: $totalTime"
        cookTimeView.text = "Cook Time: $cookingTime"

        Glide.with(this)
            .load(imageUrl)
            .into(recipeImageView)

        // Add ingredients and quantities dynamically
        ingredientsContainer.removeAllViews() // Clear old views if any
        for (i in ingredientsList.indices) {
            // Create a horizontal LinearLayout for each ingredient and its quantity
            val ingredientLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Create and add the ingredient TextView
            val ingredientView = TextView(requireContext()).apply {
                text = ingredientsList[i]
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    gravity = android.view.Gravity.END
                }
            }

            // Add ingredient to the layout
            ingredientLayout.addView(ingredientView)

            // Create and add the quantity TextView
            val quantityView = TextView(requireContext()).apply {
                text = quantityList[i]
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            ingredientLayout.addView(quantityView) // Add quantity to the layout
            ingredientsContainer.addView(ingredientLayout) // Add the ingredient layout to the container
        }

        // Add steps dynamically
        stepsContainer.removeAllViews() // Clear old views if any
        for (step in stepsList) {
            val stepView = TextView(requireContext()).apply {
                text = step
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            stepsContainer.addView(stepView)
        }
    }

    companion object {
        private const val ARG_PARAM1 = "recipe_id"

        @JvmStatic
        fun newInstance(recipeId: String) =
            RecipeScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, recipeId)
                }
            }
    }
}
