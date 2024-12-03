package com.example.veluna

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodNotes : AppCompatActivity() {

    private lateinit var moodChipGroup: ChipGroup
    private lateinit var flowChipGroup: ChipGroup
    private val selectedFeelings = mutableListOf<String>()
    private lateinit var applyButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_notes)

        moodChipGroup = findViewById(R.id.moodChipGroup)
        flowChipGroup = findViewById(R.id.flowChipGroup)
        applyButton = findViewById(R.id.applyButton)

        val feelingButtons = listOf(
            Pair(findViewById<ImageButton>(R.id.feeling_cramps), "Cramps"),
            Pair(findViewById<ImageButton>(R.id.feeling_tenderbreast), "Tender Breast"),
            Pair(findViewById<ImageButton>(R.id.feeling_fatigue), "Fatigue"),
            Pair(findViewById<ImageButton>(R.id.feeling_bloating), "Bloating")
        )

        feelingButtons.forEach { (button, feeling) ->
            button.setOnClickListener {
                if (selectedFeelings.contains(feeling)) {
                    selectedFeelings.remove(feeling)
                    button.alpha = 1.0f // Reset button appearance
                } else {
                    selectedFeelings.add(feeling)
                    button.alpha = 0.5f // Indicate selection
                }
            }
        }

        applyButton.setOnClickListener {
            saveMoodNotes()
        }

        findViewById<ImageButton>(R.id.back_button_mood_notes).setOnClickListener {
            finish()
        }
    }

    private fun saveMoodNotes() {
        val userId = userId
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMoods = getSelectedChips(moodChipGroup)
        val selectedFlow = getSelectedChips(flowChipGroup).firstOrNull()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val moodNotesData = mapOf(
            "feelings" to selectedFeelings,
            "moods" to selectedMoods,
            "flow" to selectedFlow,
            "timestamp" to currentDate
        )

        db.collection("users").document(userId).collection("moodNotes")
            .add(moodNotesData)
            .addOnSuccessListener {
                Toast.makeText(this, "Mood notes saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save mood notes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getSelectedChips(chipGroup: ChipGroup): List<String> {
        val selectedChips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedChips.add(chip.text.toString())
            }
        }
        return selectedChips
    }
}