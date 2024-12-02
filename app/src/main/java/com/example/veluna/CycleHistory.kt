package com.example.veluna

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CycleHistory : AppCompatActivity() {

    private lateinit var cycleAdapter: CycleHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cycle_history)

        // Back button listener
        findViewById<ImageButton>(R.id.back_button_cycle_history).setOnClickListener {
            onBackPressed() // Navigate back
        }

        // Initialize RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.rv_cycle_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy data for testing
        val cycleData = listOf(
            Cycle("Feb 12 - Mar 14", 24, 6),
            Cycle("Jan 14 - Feb 12", 32, 5),
            Cycle("Dec 12 - Jan 14", 30, 7),
            Cycle("Nov 12 - Dec 12", 30, 4),
            Cycle("Oct 18 - Nov 12", 24, 6)
        )

        // Set up adapter
        cycleAdapter = CycleHistoryAdapter(cycleData)
        recyclerView.adapter = cycleAdapter
    }
}
