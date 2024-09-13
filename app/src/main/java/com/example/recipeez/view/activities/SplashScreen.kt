package com.example.recipeez.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeez.R

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to WelcomeScreen activity
            val intent = Intent(this, WelcomeScreen::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
