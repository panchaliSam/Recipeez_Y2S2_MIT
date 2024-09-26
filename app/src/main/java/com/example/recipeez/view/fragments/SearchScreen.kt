package com.example.recipeez.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchScreen : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var database: DatabaseReference
    private lateinit var linearLayoutRecipes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        database = FirebaseDatabase.getInstance().getReference("recipes")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_screen, container, false)

        // Initialize the LinearLayout where recipes will be displayed
        linearLayoutRecipes = view.findViewById(R.id.linearLayoutRecipes)

        // Fetch recipes from Firebase and display them
        fetchRecipes()

        // Return the view for the fragment
        return view
    }

    private fun fetchRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear any previous views before adding new ones
                    linearLayoutRecipes.removeAllViews()

                    val recipeDataList = ArrayList<Pair<String, String>>()

                    for (recipeSnapshot in snapshot.children) {
                        val imageUrl = recipeSnapshot.child("imageUrl").value.toString()
                        val recipeId = recipeSnapshot.key.toString()
                        recipeDataList.add(Pair(imageUrl, recipeId))
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

    private fun displayRecipes(recipeDataList: List<Pair<String, String>>) {
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

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, // Width set to 0, because weight will distribute space equally
                    300 // Fixed height of 300 pixels for each image
                ).apply {
                    weight = 1f // Distribute the space equally between three images
                    setMargins(8, 8, 8, 8) // Add margin around each image
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val (imageUrl, recipeId) = recipeDataList[i]
            Glide.with(this).load(imageUrl).into(imageView)

            // Set click listener on the ImageView
            imageView.setOnClickListener {
                val intent = Intent(requireContext(), RecipeScreen::class.java)
                intent.putExtra("RECIPE_ID", recipeId)
                startActivity(intent)
            }

            rowLayout?.addView(imageView)
        }
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            SearchScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
