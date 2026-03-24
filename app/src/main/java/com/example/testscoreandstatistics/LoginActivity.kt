package com.example.testscoreandstatistics

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
        binding.errorText.visibility = View.GONE
        toggleLoading(false)
    }

    @SuppressLint("HardwareIds")
    private fun setupListeners(){
        binding.loginButton.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            binding.errorText.visibility = View.GONE

            // Unique Device ID to enforce single-session login
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                toggleLoading(true)
                viewmodel.loginUser(username, password, deviceId, object : RestRepository.LoginListener{
                    override fun onLoginSuccessful() {
                        runOnUiThread {
                            toggleLoading(false)
                            gotoMainActivity()
                        }
                    }

                    override fun onLoginFailed(error: String?) {
                        runOnUiThread {
                            toggleLoading(false)
                            binding.errorText.text = error ?: "Login failed"
                            binding.errorText.visibility = View.VISIBLE
                        }
                    }
                })
            } else {
                binding.errorText.text = "Please enter username and password"
                binding.errorText.visibility = View.VISIBLE
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
