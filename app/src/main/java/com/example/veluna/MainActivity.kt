package com.example.veluna

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inisialisasi NavHostFragment dan NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Inisialisasi BottomNavigationView
        bottomNavigation = findViewById(R.id.bottom_nav)
        bottomNavigation.setupWithNavController(navController)

        // Set listener untuk item klik pada BottomNavigationView
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // Bersihkan tumpukan navigasi dan arahkan ke HomeFragment
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.profileFragment -> {
                    // Arahkan ke ProfileFragment
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.historyFragment -> {
                    // Arahkan ke HistoryFragment
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.editprofileFragment -> {
                    // Arahkan ke ProfileFragment
                    navController.navigate(R.id.editprofileFragment)
                    true
                }
                else -> false
            }
        }
    }

    // Fungsi untuk menyembunyikan BottomNavigationView
    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }
}