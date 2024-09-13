package com.example.recipeez.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.recipeez.R
import com.example.recipeez.databinding.ActivityRegisterAccountBinding
import com.example.recipeez.databinding.ActivityWelcomeScreenBinding

class WelcomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.regWelcome.setOnClickListener{
            val intent = Intent(this,RegisterAccount::class.java)
            startActivity(intent)
        }
        binding.loginWelcome.setOnClickListener {
            val intent = Intent(this,LoginScreen::class.java)
            startActivity(intent)
        }
    }
}