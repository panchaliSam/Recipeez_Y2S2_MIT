package com.example.recipeez.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecipieScreen : AppCompatActivity() {
    private lateinit var reciepeRef: DatabaseReference

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitle: TextView
    private lateinit var preparationTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var cookTimeView: TextView
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var stepsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipie_screen)

        // Retrieve the recipe ID from the Intent
        val recipeId = intent.getStringExtra("RECIPE_ID")
        recipeImageView = findViewById(R.id.recipe_image)
        recipeTitle = findViewById(R.id.recipe_title)
        preparationTimeView = findViewById(R.id.prep_time)
        totalTimeView = findViewById(R.id.total_time)
        cookTimeView = findViewById(R.id.cook_time)
        ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view)
        stepsRecyclerView = findViewById(R.id.steps_recycler_view)


        // Initialize Firebase Database reference
        reciepeRef = FirebaseDatabase.getInstance().getReference("recipes")
        Toast.makeText(this, "Recipe ID 2: $recipeId", Toast.LENGTH_SHORT).show()
        // Fetch and display the recipe data using the recipeId
        recipeId?.let {
            getRecipeData(recipeId)
        }

    }

    data class Recipe(
        val name: String? = "",
        val description: String? = "",
        val preparationTime: String? = "",
        val totalTime: String? = "",
        val cookTime: String? = "",
        val steps: List<String> = listOf(),
        val ingredients: List<String> = listOf(),
        val imageUrl: String? = ""
    )

    private fun getRecipeData(recipeId: String) {
        reciepeRef.child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipe = snapshot.getValue(Recipe::class.java)
                recipe?.let {
                    populateUI(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun populateUI(recipe: Recipe) {


        // Set the title
        recipeTitle.text = recipe.name


        // Set the times
        preparationTimeView.text = "Prep Time: ${recipe.preparationTime}"
        totalTimeView.text = "Total Time: ${recipe.totalTime}"
        cookTimeView.text = "Cook Time: ${recipe.cookTime}"

        // Load the image using Glide
        Glide.with(this)
            .load(recipe.imageUrl)
            .into(recipeImageView)

        // Set up RecyclerView for ingredients and steps (Adapter setup required)
        val ingredientsAdapter = IngredientsAdapter(recipe.ingredients ?: emptyList())
        ingredientsRecyclerView.adapter = ingredientsAdapter

        val stepsAdapter = StepsAdapter(recipe.steps ?: emptyList())
        stepsRecyclerView.adapter = stepsAdapter
    }

    class IngredientsAdapter(private val ingredients: List<String>) :
        RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
            return IngredientViewHolder(view)
        }

        override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
            holder.bind(ingredients[position])
        }

        override fun getItemCount(): Int = ingredients.size

        class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ingredientTextView: TextView = itemView.findViewById(R.id.ingredient_text)

            fun bind(ingredient: String) {
                ingredientTextView.text = ingredient
            }
        }
    }

    class StepsAdapter(private val steps: List<String>) :
        RecyclerView.Adapter<StepsAdapter.StepViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
            return StepViewHolder(view)
        }

        override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
            holder.bind(steps[position])
        }

        override fun getItemCount(): Int = steps.size

        class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val stepTextView: TextView = itemView.findViewById(R.id.step_text)

            fun bind(step: String) {
                stepTextView.text = step
            }
        }
    }


}