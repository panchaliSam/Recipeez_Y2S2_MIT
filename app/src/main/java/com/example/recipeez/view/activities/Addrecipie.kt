package com.example.recipeez.view.activities

import android.annotation.SuppressLint
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
    private lateinit var ingredientsDropdown: Spinner
    private lateinit var quantityDropdown: Spinner
    private lateinit var steps: EditText
    private lateinit var preparationTime: EditText
    private lateinit var cookingTime: EditText
    private lateinit var totalTime: EditText
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var dynamicIngredientsContainer: LinearLayout
    private lateinit var addButton: Button
    private lateinit var dynamicStepsContainer: LinearLayout
    private lateinit var addStepsButton: Button

    // Firebase authentication and database references
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val recipesRef = database.getReference("recipes")
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private var stepCounter = 1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addrecipie)

        // Initialize views
        recipeName = findViewById(R.id.recipe_name)
        description = findViewById(R.id.description)
        cuisineDropdown = findViewById(R.id.cuisineDropdown)
        foodTypeDropdown = findViewById(R.id.foodTypeDropdown)
        ingredientsDropdown = findViewById(R.id.ingredientsDropdown)
        quantityDropdown = findViewById(R.id.quantityDropdown)
        steps = findViewById(R.id.steps)
        preparationTime = findViewById(R.id.preparation_time)
        cookingTime = findViewById(R.id.cooking_time)
        totalTime = findViewById(R.id.total_time)
        submitButton = findViewById(R.id.submit_button)
        selectImageButton = findViewById(R.id.imageButton)
        imagePreview = findViewById(R.id.image_preview)
        dynamicIngredientsContainer = findViewById(R.id.dynamic_ingredients_container)
        addButton = findViewById(R.id.add_button)
        dynamicStepsContainer = findViewById(R.id.dynamic_steps_container)
        addStepsButton = findViewById(R.id.add_steps)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        selectImageButton.setOnClickListener {
            openImageChooser()
        }

        // Set up click listener for the add button to add new ingredient and quantity spinners
        addButton.setOnClickListener {
            addIngredientQuantityFields()
        }
        addStepsButton.setOnClickListener {
            addStepsFields()
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

    private fun addStepsFields() {
        // Create a new EditText for the step
        val newStepEditText = EditText(this)
        newStepEditText.hint = "Enter step $stepCounter"
        newStepEditText.setPadding(16, 16, 16, 16)
        newStepEditText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        stepCounter++
        // Add the new EditText to the dynamicStepsContainer
        dynamicStepsContainer.addView(newStepEditText)
    }

    private fun addIngredientQuantityFields() {
        // Create a horizontal LinearLayout
        val horizontalLayout = LinearLayout(this)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Create new ingredient and quantity spinners
        val newIngredientSpinner = Spinner(this)
        val newQuantitySpinner = Spinner(this)

        // Set layout parameters for the spinners
        val spinnerLayoutParams = LinearLayout.LayoutParams(
            0, // Width is set to 0, since we will use weight
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f // Weight to distribute space evenly
        )

        newIngredientSpinner.layoutParams = spinnerLayoutParams
        newQuantitySpinner.layoutParams = spinnerLayoutParams

        // Populate the spinners with the existing arrays from resources
        val ingredientAdapter = ArrayAdapter.createFromResource(
            this, R.array.ingredient_options_array, android.R.layout.simple_spinner_item
        )
        ingredientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        newIngredientSpinner.adapter = ingredientAdapter

        val quantityAdapter = ArrayAdapter.createFromResource(
            this, R.array.ingredient_quantity_array, android.R.layout.simple_spinner_item
        )
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        newQuantitySpinner.adapter = quantityAdapter

        // Add the spinners to the horizontal layout
        horizontalLayout.addView(newIngredientSpinner)
        horizontalLayout.addView(newQuantitySpinner)

        // Add the horizontal layout to the dynamic ingredients container
        dynamicIngredientsContainer.addView(horizontalLayout)
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
            Toast.makeText(this, "Please log in to submit a recipe", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the values from the form fields
        val name = recipeName.text.toString().trim()
        val desc = description.text.toString().trim()
        val cuisine = cuisineDropdown.selectedItem.toString()
        val foodType = foodTypeDropdown.selectedItem.toString()
        val prepTime = preparationTime.text.toString().trim()
        val cookTime = cookingTime.text.toString().trim()
        val totalTimeValue = totalTime.text.toString().trim()

        // Initialize lists for ingredients, quantities, and steps
        val ingredientsList = mutableListOf<String>()
        val quantityList = mutableListOf<String>()
        val stepsList = mutableListOf<String>() // For all steps

        // Add selected ingredient and quantity from dropdowns to the list
        ingredientsList.add(ingredientsDropdown.selectedItem.toString())
        quantityList.add(quantityDropdown.selectedItem.toString())

        // Add the initial step to the steps list
        stepsList.add(steps.text.toString().trim())

        // Iterate through all dynamically added spinners in the container for ingredients and quantities
        for (i in 0 until dynamicIngredientsContainer.childCount) {
            val ingredientSpinner = (dynamicIngredientsContainer.getChildAt(i) as LinearLayout).getChildAt(0) as Spinner
            val quantitySpinner = (dynamicIngredientsContainer.getChildAt(i) as LinearLayout).getChildAt(1) as Spinner
            ingredientsList.add(ingredientSpinner.selectedItem.toString())
            quantityList.add(quantitySpinner.selectedItem.toString())
        }

        // Iterate through all dynamically added step fields
        for (i in 0 until dynamicStepsContainer.childCount) {
            val stepEditText = dynamicStepsContainer.getChildAt(i) as EditText
            stepsList.add(stepEditText.text.toString().trim()) // Add dynamic steps to the list
        }

        // Check if any required field is empty
        if (name.isEmpty() || desc.isEmpty() || stepsList.isEmpty() ||
            prepTime.isEmpty() || cookTime.isEmpty() || totalTimeValue.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unique ID for the recipe entry
        val recipeId = recipesRef.push().key

        // Create a map for the recipe data
        val recipeData = hashMapOf(
            "name" to name,
            "description" to desc,
            "cuisine" to cuisine,
            "foodType" to foodType,
            "ingredientsList" to ingredientsList, // Store the list of all ingredients
            "quantityList" to quantityList,       // Store the list of all quantities
            "stepsList" to stepsList,             // Store the list of all steps
            "preparationTime" to prepTime,
            "cookingTime" to cookTime,
            "totalTime" to totalTimeValue,
            "imageUrl" to imageUrl,
            "userId" to user.uid
        )

        // Store data in Firebase under the unique ID
        if (recipeId != null) {
            recipesRef.child(recipeId).setValue(recipeData).addOnCompleteListener { task ->
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
        steps.text.clear()
        preparationTime.text.clear()
        cookingTime.text.clear()
        totalTime.text.clear()
        imagePreview.setImageResource(R.drawable.uploadimage) // Reset image preview
        imageUri = null
        dynamicIngredientsContainer.removeAllViews()
        dynamicStepsContainer.removeAllViews()
    }


}
