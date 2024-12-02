package com.example.veluna

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Inisialisasi BottomNavigationView
        bottomNavigation = findViewById(R.id.bottom_nav)

        // Periksa status login
        checkLoginState()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.MainPage -> {
                    showBottomNavigation()
                    bottomNavigation.menu.findItem(R.id.MainPage).isChecked = true
                }
                R.id.profileFragment -> {
                    showBottomNavigation()
                    bottomNavigation.menu.findItem(R.id.profileFragment).isChecked = true
                }
                R.id.cycleHistory -> {
                    showBottomNavigation()
                    bottomNavigation.menu.findItem(R.id.cycleHistory).isChecked = true
                }
                else -> {
                    hideBottomNavigation()
                }
            }
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.MainPage -> {
                    navController.navigate(R.id.MainPage)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.cycleHistory -> {
                    navController.navigate(R.id.cycleHistory)
                    true
                }
                else -> false
            }
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.VISIBLE // Pastikan toolbar terlihat
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color3)) // Gunakan warna color3
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)
        Log.d("ToolbarColor", "Toolbar background: ${toolbar.background}")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideBottomNavigation() {
        bottomNavigation.isVisible = false
        Log.d("MainActivity", "BottomNavigationView hidden")
    }

    fun showBottomNavigation() {
        bottomNavigation.isVisible = true
        Log.d("MainActivity", "BottomNavigationView shown")
    }


    private fun checkLoginState() {
        val sharedPreferences = getSharedPreferences("VelunaPrefs", 0)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d("MainActivity", "Login state: $isLoggedIn, Current user: ${currentUser?.uid}")

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (isLoggedIn && currentUser != null) {
            navController.navigate(R.id.action_global_MainPageFragment)
            showBottomNavigation()
            bottomNavigation.menu.findItem(R.id.MainPage).isChecked = true
        } else {
            navController.navigate(R.id.action_WelcomePageFragment_to_LoginFragment)
            hideBottomNavigation()
        }
    }

}
