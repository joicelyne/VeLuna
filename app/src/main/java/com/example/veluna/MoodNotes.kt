package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.ChipGroup
import java.util.Calendar

class MoodNotes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_notes)

        findViewById<ImageButton>(R.id.back_button_mood_notes).setOnClickListener {
            finish()
        }

    }
}