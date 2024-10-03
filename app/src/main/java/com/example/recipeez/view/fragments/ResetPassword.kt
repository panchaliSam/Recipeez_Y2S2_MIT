package com.example.recipeez.view.fragments

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.recipeez.R
import com.example.recipeez.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    companion object {
        fun newInstance(): ResetPassword {
            return ResetPassword()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Close the fragment when close button is clicked
        binding.closeResetPassword.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Toggle Old Password visibility
        binding.toggleOldPassword.setOnClickListener {
            isOldPasswordVisible = !isOldPasswordVisible
            togglePasswordVisibility(binding.editOldPassword, isOldPasswordVisible)
        }

        // Toggle New Password visibility
        binding.toggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(binding.editNewPassword, isNewPasswordVisible)
        }

        // Toggle Confirm Password visibility
        binding.toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding.editConfirmPassword, isConfirmPasswordVisible)
        }

        // Handle save button click to update the password
        binding.savePasswordButton.setOnClickListener {
            val oldPassword = binding.editOldPassword.text.toString().trim()
            val newPassword = binding.editNewPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase: Reauthenticate the user using the old password, then update to the new password
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                val email = currentUser.email
                if (email != null) {
                    val credential = EmailAuthProvider.getCredential(email, oldPassword)

                    currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Error updating password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length) // Move cursor to end of text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
