package com.example.recipeez.view.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.example.recipeez.databinding.FragmentUserAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserAccount : Fragment() {

    // View Binding
    private var _binding: FragmentUserAccountBinding? = null
    private val binding get() = _binding!!

    // Firebase references
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserData()

        // Logout button click listener
        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            requireActivity().finish() // Finish the activity or navigate back to login screen
        }

        // View My Recipes button click listener
        binding.viewMyRecipes.setOnClickListener {
            val addRecipeFragment = AddRecipe()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addRecipeFragment) // Use the correct container ID
                .addToBackStack(null) // Add to back stack so user can navigate back
                .commit()
        }
    }

    private fun fetchUserData() {
        // Get current user's ID
        val currentUserId = firebaseAuth.currentUser?.uid

        // Fetch user details from Firebase Realtime Database
        if (currentUserId != null) {
            database.child(currentUserId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val email = snapshot.child("email").value.toString()
                    val imageUrl = snapshot.child("imageUrl").value.toString()

                    // Set user email in the TextView
                    binding.userEmailTextView.text = email

                    // Load the user's profile image using Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.user) // Default image in case there's no profile image
                        .into(binding.profileImageView)
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle any errors here
                Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
    }
}
