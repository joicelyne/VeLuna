package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editPeriodButton: Button = findViewById(R.id.editPeriodButton)
        editPeriodButton.setOnClickListener {
            try {
                // Navigate to CalendarActivity
                val intent = Intent(this, DatesEditPeriod::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting CalendarActivity", e)
                Toast.makeText(this, "Error starting activity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val cyclePageButton: Button = findViewById(R.id.cyclePageButton)
        cyclePageButton.setOnClickListener {
            val intent = Intent(this, CycleHistory::class.java)
            startActivity(intent)
        }

        val moodNotesButton: Button = findViewById(R.id.moodNotesButton)
        moodNotesButton.setOnClickListener {
            val intent = Intent(this, MoodNotes::class.java)
            startActivity(intent)
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
}