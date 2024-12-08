package com.example.veluna

import android.os.Bundle
import android.widget.ImageButton
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CycleHistory : AppCompatActivity() {

    private lateinit var cycleAdapter: CycleHistoryAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

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

        // Initialize adapter with empty list
        cycleAdapter = CycleHistoryAdapter(emptyList())
        recyclerView.adapter = cycleAdapter

        // Load data from Firestore
        loadCycleData()
    }

    private fun loadCycleData() {
        val currentUserId = userId ?: return

        db.collection("users")
            .document(currentUserId)
            .collection("period")
            .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan dari terbaru ke terlama
            .get()
            .addOnSuccessListener { querySnapshot ->
                val cycleData = querySnapshot.documents.mapNotNull { document ->
                    val periodStart = document.getTimestamp("periodStart")?.toDate()
                    val periodLength = document.get("maxDays") as? Int ?: 0
                    val periodEnd = periodStart?.let { start ->
                        Calendar.getInstance().apply {
                            time = start
                            add(Calendar.DATE, periodLength - 1)
                        }.time
                    }

                    if (periodStart != null && periodEnd != null) {
                        Cycle(
                            dateRange = "${formatDate(periodStart)} - ${formatDate(periodEnd)}",
                            cycleLength = calculateCycleLength(periodStart, periodEnd),
                            periodLength = periodLength
                        )
                    } else null
                }

                // Update adapter with fetched data
                cycleAdapter.updateData(cycleData)
            }
            .addOnFailureListener { e ->
                Log.e("CycleHistory", "Failed to load data: ${e.message}")
            }
    }

    private fun calculateCycleLength(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (1000 * 60 * 60 * 24)).toInt() + 1 // Tambahkan 1 untuk menyertakan hari mulai
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return dateFormat.format(date)
    }
}
