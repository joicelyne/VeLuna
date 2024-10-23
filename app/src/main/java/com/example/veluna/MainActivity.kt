package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Navigating to CalendarEditPeriod
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

    }
}