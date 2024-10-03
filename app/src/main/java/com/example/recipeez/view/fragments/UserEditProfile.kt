package com.example.recipeez.view.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserEditProfile : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var editEmail: EditText
    private lateinit var closeEditProfile: ImageButton
    private lateinit var changeProfileImageButton: Button
    private lateinit var saveProfileButton: Button

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var imageUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_edit_profile, container, false)

        profileImageView = view.findViewById(R.id.profileImageView)
        editEmail = view.findViewById(R.id.editEmail)
        closeEditProfile = view.findViewById(R.id.closeEditProfile)
        changeProfileImageButton = view.findViewById(R.id.changeProfileImageButton)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)

        loadUserEmail()
        loadUserProfileImage()

        saveProfileButton.setOnClickListener {
            val newEmail = editEmail.text.toString().trim()
            if (newEmail.isNotEmpty()) {
                updateUserEmail(newEmail)
            } else {
                Toast.makeText(context, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        closeEditProfile.setOnClickListener {
            activity?.onBackPressed()
        }

        changeProfileImageButton.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun loadUserEmail() {
        currentUser?.let {
            editEmail.setText(it.email)
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfileImage() {
        currentUser?.photoUrl?.let { imageUrl ->
            Glide.with(this).load(imageUrl).into(profileImageView)
        } ?: run {
            profileImageView.setImageResource(R.drawable.user)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                profileImageView.setImageURI(it)
                uploadImageToFirebase()
            }
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase() {
        imageUri?.let { uri ->
            val fileReference = storageReference.child("${currentUser?.uid}.jpg")
            fileReference.putFile(uri).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfileImage(downloadUrl.toString())
                    storeImageUrlInDatabase(downloadUrl.toString())
                }.addOnFailureListener { e ->
                    Log.e("UserEditProfile", "Failed to get download URL: ${e.message}")
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("UserEditProfile", "Error uploading image: ${e.message}")
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfileImage(imageUrl: String) {
        currentUser?.let { user ->
            val profileUpdates = userProfileChangeRequest {
                photoUri = Uri.parse(imageUrl)
            }
            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UserEditProfile", "Error updating profile image: ${task.exception}")
                    Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun storeImageUrlInDatabase(imageUrl: String) {
        currentUser?.let { user ->
            databaseReference.child(user.uid).child("profileImage").setValue(imageUrl)
                .addOnSuccessListener {
                    Log.d("UserEditProfile", "Profile image URL stored in database successfully")
                }.addOnFailureListener { e ->
                    Log.e("UserEditProfile", "Error storing profile image URL: ${e.message}")
                }
        }
    }

    private fun updateUserEmail(newEmail: String) {
        currentUser?.let { user ->
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Email updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UserEditProfile", "Error updating email: ${task.exception}")
                    Toast.makeText(context, "Failed to update email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserEditProfile()
        private const val GALLERY_REQUEST_CODE = 100
    }
}
