package com.example.veluna

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatesEditPeriod : AppCompatActivity() {

    private lateinit var octoberCalendar: GridLayout
    private lateinit var novemberCalendar: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_edit_period)

        octoberCalendar = findViewById(R.id.october_calendar)
        novemberCalendar = findViewById(R.id.november_calendar)

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        setupCalendar(octoberCalendar, 31, currentMonth, currentYear) // October with 31 days
        setupCalendar(novemberCalendar, 30, currentMonth + 1, currentYear) // November with 30 days

        findViewById<ImageButton>(R.id.back_button_edit_perioddate).setOnClickListener {
            finish()
        }

    }

    private fun setupCalendar(gridLayout: GridLayout, daysInMonth: Int, month: Int, year: Int) {
        gridLayout.removeAllViews()  // Clear previous views
        val inflater = LayoutInflater.from(this)

        val dayLabels = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (label in dayLabels) {
            val dayView = inflater.inflate(R.layout.days_edit_period, gridLayout, false)
            val textView = dayView.findViewById<TextView>(R.id.dayLabel)
            textView.text = label
            gridLayout.addView(dayView)
        }

        val firstDayOfMonth = getFirstDayOfMonth(month, year)

        for (i in 1 until firstDayOfMonth) {
            val emptyView = inflater.inflate(R.layout.dates_edit_period, gridLayout, false)
            emptyView.visibility = View.INVISIBLE // Just placeholder views
            gridLayout.addView(emptyView)
        }

        for (day in 1..daysInMonth) {
            val dayView = inflater.inflate(R.layout.dates_edit_period, gridLayout, false)
            val tvDate = dayView.findViewById<TextView>(R.id.tv_date)
            val btnToggle = dayView.findViewById<Button>(R.id.btn_toggle)

            tvDate.text = day.toString()

            btnToggle.tag = "day_$day"

            dayView.setOnClickListener {
                toggleSelection(btnToggle)
            }

            gridLayout.addView(dayView)
        }
    }

    private fun getFirstDayOfMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    private fun toggleSelection(button: Button) {
        button.isSelected = !button.isSelected
    }
}
