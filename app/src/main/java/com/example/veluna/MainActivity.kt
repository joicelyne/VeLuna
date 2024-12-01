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
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
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

        // Periksa status login
        checkLoginState()

        // Inisialisasi NavHostFragment dan NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Inisialisasi BottomNavigationView
        bottomNavigation = findViewById(R.id.bottom_nav)
        bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.MainPage, R.id.profileFragment, R.id.cycleHistory, R.id.moodNotes, R.id.datesEditPeriod, R.id.editprofileFragment,    -> {
                    // Tampilkan BottomNavigationView
                    bottomNavigation.isVisible = true
                }
                else -> {
                    // Sembunyikan BottomNavigationView
                    bottomNavigation.isVisible = false
                }
            }
        }

        // Set listener untuk item klik pada BottomNavigationView
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.MainPage -> {
                    // Bersihkan tumpukan navigasi dan arahkan ke HomeFragment
                    navController.popBackStack(R.id.MainPage, false)
                    true
                }

                R.id.profileFragment -> {
                    // Arahkan ke ProfileFragment
                    navController.navigate(R.id.profileFragment)
                    true
                }

                R.id.cycleHistory -> {
                    // Arahkan ke HistoryFragment
                    navController.navigate(R.id.cycleHistory)
                    true
                }

//                R.id.editprofileFragment -> {
//                    // Arahkan ke ProfileFragment
//                    navController.navigate(R.id.editprofileFragment)
//                    true
//                }
                else -> false
            }
        }

        val db = FirebaseFirestore.getInstance()

        // Find the toolbar from the layout
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.GONE

        // Set the toolbar as the ActionBar
        setSupportActionBar(toolbar)

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

//    fun showBottomNavigation() {
//        bottomNavigation.visibility = View.VISIBLE
//    }

    private fun checkLoginState() {
        val sharedPreferences = getSharedPreferences("VelunaPrefs", 0)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (isLoggedIn) {
            // Jika sudah login, langsung ke MainPageFragment
            navController.navigate(R.id.action_global_MainPageFragment)
        }
    }
}