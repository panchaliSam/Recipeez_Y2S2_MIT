package com.example.recipeez.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Addrecipie : AppCompatActivity() {

    // Declare views
    private lateinit var recipeName: EditText
    private lateinit var description: EditText
    private lateinit var cuisineDropdown: Spinner
    private lateinit var foodTypeDropdown: Spinner
    private lateinit var ingredients: EditText
    private lateinit var steps: EditText
    private lateinit var preparationTime: EditText
    private lateinit var cookingTime: EditText
    private lateinit var totalTime: EditText
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imagePreview: ImageView

    // Firebase authentication and database references
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val recipesRef = database.getReference("recipes")
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addrecipie)

        // Initialize views
        recipeName = findViewById(R.id.recipe_name)
        description = findViewById(R.id.description)
        cuisineDropdown = findViewById(R.id.cuisineDropdown)
        foodTypeDropdown = findViewById(R.id.foodTypeDropdown)
        ingredients = findViewById(R.id.ingredients)
        steps = findViewById(R.id.steps)
        preparationTime = findViewById(R.id.preparation_time)
        cookingTime = findViewById(R.id.cooking_time)
        totalTime = findViewById(R.id.total_time)
        submitButton = findViewById(R.id.submit_button)
        selectImageButton = findViewById(R.id.imageButton)
        imagePreview = findViewById(R.id.image_preview)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        selectImageButton.setOnClickListener {
            openImageChooser()
        }
        // Set up click listener for the submit button
        submitButton.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndSaveRecipe()
            } else {
                saveRecipeToDatabase(null)
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imagePreview.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndSaveRecipe() {
        if (imageUri != null) {
            val imageRef = storageRef.child("images/${imageUri!!.lastPathSegment}")
            val uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    saveRecipeToDatabase(uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecipeToDatabase(imageUrl: String?) {
        // Check if the user is authenticated
        val user = firebaseAuth.currentUser
        if (user == null) {
            // If not authenticated, show a message
            Toast.makeText(this, "Please log in to submit a recipe", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the values from the form fields
        val name = recipeName.text.toString().trim()
        val desc = description.text.toString().trim()
        val cuisine = cuisineDropdown.selectedItem.toString()
        val foodType = foodTypeDropdown.selectedItem.toString()
        val ingredientList = ingredients.text.toString().trim()
        val stepList = steps.text.toString().trim()
        val prepTime = preparationTime.text.toString().trim()
        val cookTime = cookingTime.text.toString().trim()
        val totalTimeValue = totalTime.text.toString().trim()

        // Check if any field is empty
        if (name.isEmpty() || desc.isEmpty() || ingredientList.isEmpty() || stepList.isEmpty() ||
            prepTime.isEmpty() || cookTime.isEmpty() || totalTimeValue.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unique ID for each recipe entry
        val recipeId = recipesRef.push().key

        // Create a recipe object to store data
        val recipe = Recipe(
            name = name,
            description = desc,
            cuisine = cuisine,
            foodType = foodType,
            ingredients = ingredientList,
            steps = stepList,
            preparationTime = prepTime,
            cookingTime = cookTime,
            totalTime = totalTimeValue,
            imageUrl = imageUrl,
            userId = user.uid // Store the user's ID as well
        )

        // Store data in Firebase under the unique ID
        if (recipeId != null) {
            recipesRef.child(recipeId).setValue(recipe).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                    clearFormFields() // Optionally clear the form
                } else {
                    Toast.makeText(this, "Failed to add recipe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Clear form fields after submission
    private fun clearFormFields() {
        recipeName.text.clear()
        description.text.clear()
        ingredients.text.clear()
        steps.text.clear()
        preparationTime.text.clear()
        cookingTime.text.clear()
        totalTime.text.clear()
        imagePreview.setImageResource(R.drawable.uploadimage) // Reset image preview
        imageUri = null
    }

    // Define the Recipe data model
    data class Recipe(
        val name: String,
        val description: String,
        val cuisine: String,
        val foodType: String,
        val ingredients: String,
        val steps: String,
        val preparationTime: String,
        val cookingTime: String,
        val totalTime: String,
        val imageUrl: String? = null,
        val userId: String // User ID of the person who submitted the recipe
    )
}
