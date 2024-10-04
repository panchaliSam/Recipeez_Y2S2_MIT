package com.example.recipeez.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.example.recipeez.view.fragments.RecipeScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SaveScreen : Fragment() {

    private lateinit var usersRef: DatabaseReference
    private lateinit var recipeLinearLayout: LinearLayout
    private lateinit var auth: FirebaseAuth

    private lateinit var sortByCuisineSpinner: Spinner
    private lateinit var sortByFoodTypeSpinner: Spinner
    private var recipeDataList = ArrayList<Triple<String, String, String>>()
    private var filteredRecipeDataList = ArrayList<Triple<String, String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_screen, container, false)

        recipeLinearLayout = view.findViewById(R.id.recipeLinearLayout)
        sortByCuisineSpinner = view.findViewById(R.id.sortByCuisineSpinner)
        sortByFoodTypeSpinner = view.findViewById(R.id.sortByFoodTypeSpinner)

        setupSpinnerListeners()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadSavedRecipes(currentUser.uid)
        } else {
            Toast.makeText(requireContext(), "No user authenticated", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupSpinnerListeners() {
        sortByCuisineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        sortByFoodTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun filterRecipes() {
        val selectedCuisine = sortByCuisineSpinner.selectedItem.toString()
        val selectedFoodType = sortByFoodTypeSpinner.selectedItem.toString()

        filteredRecipeDataList.clear()

        for (recipe in recipeDataList) {
            val (recipeId, recipeName, imageUrl) = recipe
            val recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId)
            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(recipeSnapshot: DataSnapshot) {
                    if (recipeSnapshot.exists()) {
                        val recipeCuisine = recipeSnapshot.child("cuisine").value as? String ?: "Unknown"
                        val recipeFoodType = recipeSnapshot.child("foodType").value as? String ?: "Unknown"

                        val cuisineMatch = selectedCuisine == "All" || recipeCuisine == selectedCuisine
                        val foodTypeMatch = selectedFoodType == "All" || recipeFoodType == selectedFoodType

                        if (cuisineMatch && foodTypeMatch) {
                            filteredRecipeDataList.add(Triple(recipeId, recipeName, imageUrl))
                            displaySavedRecipes(filteredRecipeDataList)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching recipe data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadSavedRecipes(userId: String) {
        usersRef.child(userId).child("savedRecipes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    recipeDataList.clear()
                    for (recipeSnapshot in snapshot.children) {
                        val recipeId = recipeSnapshot.value as? String
                        recipeId?.let {
                            val recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(it)
                            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(recipeSnapshot: DataSnapshot) {
                                    val recipeName = recipeSnapshot.child("name").value as? String ?: "Unknown Recipe"
                                    val imageUrl = recipeSnapshot.child("imageUrl").value as? String ?: ""

                                    recipeDataList.add(Triple(it, recipeName, imageUrl))
                                    displaySavedRecipes(recipeDataList)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(requireContext(), "Failed to load recipe data", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No saved recipes found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load saved recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displaySavedRecipes(recipeDataList: ArrayList<Triple<String, String, String>>) {
        recipeLinearLayout.removeAllViews()

        for (i in recipeDataList.indices step 3) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 0)
                }
            }

            for (j in 0 until 3) {
                if (i + j < recipeDataList.size) {
                    val (recipeId, recipeName, imageUrl) = recipeDataList[i + j]

                    val verticalLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f
                            setMargins(8, 8, 8, 8)
                        }
                    }

                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            300
                        )
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    Glide.with(requireContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(imageView)

                    imageView.setOnClickListener {
                        val intent = Intent(requireContext(), RecipeScreen::class.java)
                        intent.putExtra("RECIPE_ID", recipeId)
                        startActivity(intent)
                    }

                    val textView = TextView(requireContext()).apply {
                        text = recipeName
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    verticalLayout.addView(imageView)
                    verticalLayout.addView(textView)
                    rowLayout.addView(verticalLayout)
                } else {
                    val emptyView = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            300
                        ).apply {
                            weight = 1f
                        }
                    }
                    rowLayout.addView(emptyView)
                }
            }

            recipeLinearLayout.addView(rowLayout)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SaveScreen().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
