package com.example.recipeez.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddRecipe : Fragment() {

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        // Initialize views
        recipeName = view.findViewById(R.id.recipe_name)
        description = view.findViewById(R.id.description)
        cuisineDropdown = view.findViewById(R.id.cuisineDropdown)
        foodTypeDropdown = view.findViewById(R.id.foodTypeDropdown)
        ingredientsDropdown = view.findViewById(R.id.ingredientsDropdown)
        quantityDropdown = view.findViewById(R.id.quantityDropdown)
        steps = view.findViewById(R.id.steps)
        preparationTime = view.findViewById(R.id.preparation_time)
        cookingTime = view.findViewById(R.id.cooking_time)
        totalTime = view.findViewById(R.id.total_time)
        submitButton = view.findViewById(R.id.submit_button)
        selectImageButton = view.findViewById(R.id.imageButton)
        imagePreview = view.findViewById(R.id.image_preview)
        dynamicIngredientsContainer = view.findViewById(R.id.dynamic_ingredients_container)
        addButton = view.findViewById(R.id.add_button)
        dynamicStepsContainer = view.findViewById(R.id.dynamic_steps_container)
        addStepsButton = view.findViewById(R.id.add_steps)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up click listeners
        selectImageButton.setOnClickListener {
            openImageChooser()
        }

        addButton.setOnClickListener {
            addIngredientQuantityFields()
        }

        addStepsButton.setOnClickListener {
            addStepsFields()
        }

        submitButton.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndSaveRecipe()
            } else {
                saveRecipeToDatabase(null)
            }
        }

        return view
    }

    private fun addStepsFields() {
        // Create a new EditText for the step
        val newStepEditText = EditText(requireContext()).apply {
            hint = "Enter step $stepCounter"
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        stepCounter++
        // Add the new EditText to the dynamicStepsContainer
        dynamicStepsContainer.addView(newStepEditText)
    }

    private fun addIngredientQuantityFields() {
        // Create a horizontal LinearLayout
        val horizontalLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create new ingredient and quantity spinners
        val newIngredientSpinner = Spinner(requireContext())
        val newQuantitySpinner = Spinner(requireContext())

        // Set layout parameters for the spinners
        val spinnerLayoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        newIngredientSpinner.layoutParams = spinnerLayoutParams
        newQuantitySpinner.layoutParams = spinnerLayoutParams

        // Populate the spinners with the existing arrays from resources
        val ingredientAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.ingredient_options_array, android.R.layout.simple_spinner_item
        )
        ingredientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        newIngredientSpinner.adapter = ingredientAdapter

        val quantityAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.ingredient_quantity_array, android.R.layout.simple_spinner_item
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imagePreview.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndSaveRecipe() {
        imageUri?.let { uri ->
            val imageRef = storageRef.child("images/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    saveRecipeToDatabase(downloadUri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecipeToDatabase(imageUrl: String?) {
        // Check if the user is authenticated
        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to submit a recipe", Toast.LENGTH_SHORT).show()
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
        for (i in 0 until dynamicIngredientsContainer.childCount) {
            val child = dynamicIngredientsContainer.getChildAt(i) as LinearLayout
            val ingredientSpinner = child.getChildAt(0) as Spinner
            val quantitySpinner = child.getChildAt(1) as Spinner
            ingredientsList.add(ingredientSpinner.selectedItem.toString())
            quantityList.add(quantitySpinner.selectedItem.toString())
        }

        // Add all steps
        for (i in 0 until dynamicStepsContainer.childCount) {
            val stepEditText = dynamicStepsContainer.getChildAt(i) as EditText
            stepsList.add(stepEditText.text.toString().trim())
        }

        // Create a recipe ID
        val recipeId = recipesRef.push().key

        // Create a Recipe object
        val recipe = Recipe(
            recipeId = recipeId,
            name = name,
            description = desc,
            cuisine = cuisine,
            foodType = foodType,
            preparationTime = prepTime,
            cookingTime = cookTime,
            totalTime = totalTimeValue,
            imageUrl = imageUrl,
            ingredients = ingredientsList,
            quantities = quantityList,
            steps = stepsList
        )

        // Save to Firebase
        recipeId?.let {
            recipesRef.child(it).setValue(recipe).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Recipe submitted successfully!", Toast.LENGTH_SHORT).show()
                    clearFields()
                } else {
                    Toast.makeText(requireContext(), "Recipe submission failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearFields() {
        recipeName.text.clear()
        description.text.clear()
        preparationTime.text.clear()
        cookingTime.text.clear()
        totalTime.text.clear()
        imagePreview.setImageResource(0) // Clear image preview
        dynamicIngredientsContainer.removeAllViews() // Clear all dynamic ingredients
        dynamicStepsContainer.removeAllViews() // Clear all dynamic steps
    }

    // Data model for Recipe
    data class Recipe(
        val recipeId: String? = null,
        val name: String? = null,
        val description: String? = null,
        val cuisine: String? = null,
        val foodType: String? = null,
        val preparationTime: String? = null,
        val cookingTime: String? = null,
        val totalTime: String? = null,
        val imageUrl: String? = null,
        val ingredients: List<String>? = null,
        val quantities: List<String>? = null,
        val steps: List<String>? = null
    )
}
