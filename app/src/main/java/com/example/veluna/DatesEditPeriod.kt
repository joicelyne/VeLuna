package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class DatesEditPeriod : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_edit_period)

        recyclerView = findViewById(R.id.calendar_recycler_view)

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Membuat daftar bulan dan tahun
        val monthYearList = mutableListOf<Pair<Int, Int>>()
        for (year in currentYear - 1..currentYear + 1) {
            for (month in 0..11) {
                monthYearList.add(Pair(month, year))
            }
        }

        // Menentukan posisi bulan saat ini
        val currentIndex = monthYearList.indexOf(Pair(currentMonth, currentYear))

        // Mengatur RecyclerView
        val adapter = CalendarAdapter(monthYearList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Scroll ke bulan saat ini
        recyclerView.scrollToPosition(currentIndex)

        // Handle tombol Apply
        findViewById<Button>(R.id.applyButton).setOnClickListener {
            // Kembali ke halaman utama (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle tombol Back
        findViewById<ImageButton>(R.id.back_button_edit_perioddate).setOnClickListener {
            finish()
        }
    }
}
