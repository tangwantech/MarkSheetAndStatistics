package com.example.testscoreandstatistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.testscoreandstatistics.databinding.ActivityLoginBinding
import com.example.testscoreandstatistics.repositories.RestRepository
import com.example.testscoreandstatistics.viewmodels.LoginActivityViewmodel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewmodel: LoginActivityViewmodel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install Splash Screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.username.text?.clear()
        binding.password.text?.clear()
        toggleLoading(false)
    }

    private fun setupListeners(){
        binding.loginButton.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                toggleLoading(true)
                viewmodel.loginUser(username, password, object : RestRepository.LoginListener{
                    override fun onLoginSuccessful() {
                        runOnUiThread {
                            toggleLoading(false)
                            gotoMainActivity()
                        }
                    }

                    override fun onLoginFailed(error: String?) {
                        runOnUiThread {
                            toggleLoading(false)
                            Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
//                viewmodel.test(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
        binding.username.isEnabled = !isLoading
        binding.password.isEnabled = !isLoading
    }

    private fun gotoMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setupViewModel(){
        viewmodel = ViewModelProvider(this)[LoginActivityViewmodel::class.java]
    }
}
