package com.example.testscoreandstatistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testscoreandstatistics.databinding.ActivityMainBinding
import com.example.testscoreandstatistics.repositories.UserRepository
import com.example.testscoreandstatistics.viewmodels.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    
    private val testScoreFragment by lazy { TestScoreFragment() }
    private val statisticsFragment by lazy { StatisticsFragment() }
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupNavigation()
        setupBackNavigation()
        
        if (savedInstanceState == null) {
            // Load the default fragment
            showFragment(testScoreFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!UserRepository.isUserLoggedIn()) {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
    }
    
    private fun setupNavigation() {
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_test_score -> {
                    showFragment(testScoreFragment)
                    true
                }
                R.id.navigation_statistics -> {
                    showFragment(statisticsFragment)
                    true
                }
                R.id.navigation_logout -> {
                    showLogoutConfirmation()
                    false
                }
                else -> false
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showLogoutConfirmation()
            }
        })
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_logout)
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(R.string.ok) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        // Clear user data and navigate to LoginActivity
        UserRepository.clearUserData()
        redirectToLogin()
    }

    private fun showFragment(fragment: Fragment) {
        if (activeFragment == fragment) return
        
        val transaction = supportFragmentManager.beginTransaction()
        
        // Hide the current active fragment if it exists
        activeFragment?.let { transaction.hide(it) }
        
        // If the fragment hasn't been added yet, add it. Otherwise, just show it.
        if (!fragment.isAdded) {
            transaction.add(R.id.nav_host_fragment, fragment)
        } else {
            transaction.show(fragment)
        }
        
        transaction.commit()
        activeFragment = fragment
    }

    fun toggleProgress(show: Boolean) {
        binding.mainProgress.visibility = if (show) View.VISIBLE else View.GONE
    }
}
