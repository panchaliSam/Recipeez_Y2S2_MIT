package com.example.recipeez.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class Addrecipie : AppCompatActivity() {
    private var imageUrl: String? = null
    private var videoUrl: String? = null
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    // Declare views
    private lateinit var recipeName: EditText
    private lateinit var description: EditText
    private lateinit var cuisineDropdown: Spinner
    private lateinit var foodTypeDropdown: Spinner
    private lateinit var ingredientsDropdown: Spinner
    private lateinit var quantityDropdown: Spinner
    private lateinit var steps: EditText
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var selectVideoButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var videoPreview: ImageView
    private lateinit var dynamicIngredientsContainer: LinearLayout
    private lateinit var addButton: Button
    private lateinit var dynamicStepsContainer: LinearLayout
    private lateinit var addStepsButton: Button
    private lateinit var backButton: ImageButton

    // Firebase authentication and database references
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val recipesRef = database.getReference("recipes")
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val PICK_IMAGE_REQUEST = 1

    private var stepCounter = 2

    // Variables to store preparation and cooking time
    private var preparationTime: Pair<Int, Int>? = null
    private var cookingTime: Pair<Int, Int>? = null

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
        submitButton = findViewById(R.id.submit_button)
        selectImageButton = findViewById(R.id.imageButton)
        selectVideoButton = findViewById(R.id.videoButton)
        imagePreview = findViewById(R.id.image_preview)
        dynamicIngredientsContainer = findViewById(R.id.dynamic_ingredients_container)
        addButton = findViewById(R.id.add_button)
        dynamicStepsContainer = findViewById(R.id.dynamic_steps_container)
        addStepsButton = findViewById(R.id.add_steps)
        backButton = findViewById(R.id.backButton)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        submitButton.setOnClickListener {
            uploadMediaAndSaveRecipe()
        }

        // Choose Image
        selectImageButton.setOnClickListener {
            selectImage()
        }

        // Choose Video
        selectVideoButton.setOnClickListener {
            selectVideo()
        }
        backButton.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        // Set up click listener for the add button to add new ingredient and quantity spinners
        addButton.setOnClickListener {
            addIngredientQuantityFields()
        }
        addStepsButton.setOnClickListener {
            addStepsFields()
        }
        val preparationTimeButton: Button = findViewById(R.id.button_preparation_time)
        val cookingTimeButton: Button = findViewById(R.id.button_cooking_time)

        // Open TimePickerDialog for preparation time
        preparationTimeButton.setOnClickListener {
            showTimePickerDialog { hours, minutes ->
                preparationTime = Pair(hours, minutes)
                preparationTimeButton.text = "Preparation Time: $hours hr $minutes min"
            }
        }

        // Open TimePickerDialog for cooking time
        cookingTimeButton.setOnClickListener {
            showTimePickerDialog { hours, minutes ->
                cookingTime = Pair(hours, minutes)
                cookingTimeButton.text = "Cooking Time: $hours hr $minutes min"
            }
        }

    }
    private fun showTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        }, currentHour, currentMinute, true).show()
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


    // Function to handle image selection
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Function to handle video selection
    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, VIDEO_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    imageUri = data.data
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
                }
                VIDEO_PICK_CODE -> {
                    videoUri = data.data
                    Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // This function is triggered when the submit button is clicked
    private fun uploadMediaAndSaveRecipe() {
        if (imageUri != null) {
            // Upload image first
            uploadMedia(imageUri!!, "images") { url ->
                imageUrl = url
                // After image upload, check if video needs to be uploaded
                if (videoUri != null) {
                    uploadMedia(videoUri!!, "videos") { url ->
                        videoUrl = url
                        saveRecipeToDatabase()
                    }
                } else {
                    saveRecipeToDatabase() // No video, so save after image
                }
            }
        } else if (videoUri != null) {
            // Only upload video if there's no image
            uploadMedia(videoUri!!, "videos") { url ->
                videoUrl = url
                saveRecipeToDatabase() // Save after video upload
            }
        } else {
            // No media, just save the recipe
            saveRecipeToDatabase()
        }
    }

    // Media upload function
    private fun uploadMedia(uri: Uri, folder: String, onSuccess: (String?) -> Unit) {
        val mediaRef = storageRef.child("$folder/${uri.lastPathSegment}")
        val uploadTask = mediaRef.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "$folder upload failed", Toast.LENGTH_SHORT).show()
            onSuccess(null) // Handle failure by returning null
        }
    }

    private fun saveRecipeToDatabase() {
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
//        val totalTimeValue = totalTime.text.toString().trim()

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
        if (name.isEmpty() || desc.isEmpty() || stepsList.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unique ID for the recipe entry
        val recipeId = recipesRef.push().key

        // Extract preparation and cooking times
        val prepHours = preparationTime?.first ?: 0
        val prepMinutes = preparationTime?.second ?: 0
        val cookHours = cookingTime?.first ?: 0
        val cookMinutes = cookingTime?.second ?: 0

        // Calculate total hours and minutes
        val totalHours = prepHours + cookHours + (prepMinutes + cookMinutes) / 60
        val totalMinutes = (prepMinutes + cookMinutes) % 60
        // Create a map for the recipe data
        val recipeData = hashMapOf(
            "name" to name,
            "description" to desc,
            "cuisine" to cuisine,
            "foodType" to foodType,
            "ingredientsList" to ingredientsList, // Store the list of all ingredients
            "quantityList" to quantityList,       // Store the list of all quantities
            "stepsList" to stepsList,
            "preparationTime" to "${prepHours}h ${prepMinutes}m", // Include preparation time
            "cookingTime" to "${cookHours}h ${cookMinutes}m",     // Include cooking time
            "totalTime" to "${totalHours}h ${totalMinutes}m",      // Total time calculation
            "imageUrl" to imageUrl,
            "videoUrl" to videoUrl,
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
        // Reset preparation and cooking time buttons
        val preparationTimeButton: Button = findViewById(R.id.button_preparation_time)
        val cookingTimeButton: Button = findViewById(R.id.button_cooking_time)

        // Reset the text for these buttons
        preparationTimeButton.text = "Set Preparation Time"
        cookingTimeButton.text = "Set Cooking Time"

        // Optionally reset preparationTime and cookingTime variables to null
        preparationTime = null
        cookingTime = null
//        totalTime.text.clear()
        imagePreview.setImageResource(R.drawable.uploadimage) // Reset image preview
        imageUri = null
        dynamicIngredientsContainer.removeAllViews()
        dynamicStepsContainer.removeAllViews()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val VIDEO_PICK_CODE = 1001
    }
}
