package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodNotes : AppCompatActivity() {

    private lateinit var moodChipGroup: ChipGroup
    private lateinit var flowChipGroup: ChipGroup
    private lateinit var applyButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_notes)

        moodChipGroup = findViewById(R.id.moodChipGroup)
        flowChipGroup = findViewById(R.id.flowChipGroup)
        applyButton = findViewById(R.id.applyButton)

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
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMoods = getSelectedChips(moodChipGroup)
        val selectedFlow = getSelectedChips(flowChipGroup).firstOrNull()

        val moodNotesData = mapOf(
            "moods" to selectedMoods,
            "flow" to selectedFlow
        )

        db.collection("users").document(userId).collection("moodNotes")
            .add(moodNotesData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Mood notes saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to save mood notes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
