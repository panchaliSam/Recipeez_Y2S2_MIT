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

        // Edit Profile button click listener
        binding.editProfile.setOnClickListener {
            val userEditProfileFragment = UserEditProfile.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, userEditProfileFragment)
                .addToBackStack(null)
                .commit()
        }

        //Reset Password button click listener
        binding.resetPassword.setOnClickListener {
            val resetPasswordFragment = ResetPassword.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, resetPasswordFragment)
                .addToBackStack(null)
                .commit()
        }

        //Saved Recipes
        binding.saveRecipe.setOnClickListener {
            val savedRecipeFragment = SaveScreen()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, savedRecipeFragment) // Use the correct container ID
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
                    val profileImageUrl = snapshot.child("profileImage").value?.toString()

                    // Set user email in the TextView
                    binding.userEmailTextView.text = email

                    // Check if the profileImageUrl exists and is not empty
                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Load the user's profile image using Glide
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.user) // Default image if loading fails
                            .error(R.drawable.user) // Default image if URL is invalid
                            .into(binding.profileImageView)
                    } else {
                        // If the user does not have a profile picture, load the default image
                        binding.profileImageView.setImageResource(R.drawable.user)
                    }
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
