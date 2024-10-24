package com.example.veluna

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var navController: NavController

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

        val db = FirebaseFirestore.getInstance()

        // Find the toolbar from the layout
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        // Set the toolbar as the ActionBar
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the action bar with the NavController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Fungsi untuk menyembunyikan BottomNavigationView
    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }
}