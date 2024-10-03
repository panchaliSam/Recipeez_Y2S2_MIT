package com.example.recipeez.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.recipeez.R
import com.example.recipeez.view.fragments.Home
import com.example.recipeez.view.fragments.SearchScreen
import com.example.recipeez.view.fragments.UserAccount
import com.google.android.material.bottomnavigation.BottomNavigationView

class Main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the BottomNavigationView from the layout
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Load the Home fragment initially when the app starts
        loadFragment(Home())

        // Set a listener for item selection in the BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadFragment(Home())  // Load Home fragment
                    true
                }
                R.id.profile -> {
                    loadFragment(UserAccount())  // Load Profile fragment
                    true
                }
                R.id.search -> {
                    loadFragment(SearchScreen())  // Load Search fragment
                    true
                }
                else -> false  // Return false for unrecognized menu items
            }
        }
    }

    // A method to replace the current fragment in the FrameLayout container
    private fun loadFragment(fragment: Fragment) {  // Accept any fragment, not just Home
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commitAllowingStateLoss()  // Use this if you are getting 'IllegalStateException'
    }
}