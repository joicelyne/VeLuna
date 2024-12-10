package com.example.veluna

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarAdapter(
    private val monthYearList: List<Pair<Int, Int>>
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calender_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val (month, year) = monthYearList[position]
        holder.bind(month, year)
    }

    override fun getItemCount(): Int = monthYearList.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthYearLabel: TextView = itemView.findViewById(R.id.month_year_label)
        private val calendarGrid: GridLayout = itemView.findViewById(R.id.calendar_grid)

        fun bind(month: Int, year: Int) {
            // Set label bulan dan tahun
            val monthName = getMonthName(month)
            monthYearLabel.text = "$monthName $year"

            // Setup grid kalender untuk bulan dan tahun ini
            setupCalendar(calendarGrid, month, year)
        }

        private fun setupCalendar(gridLayout: GridLayout, month: Int, year: Int) {
            gridLayout.removeAllViews()

            // Tambahkan label hari (Mon, Tue, ...)
            val dayLabels = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (label in dayLabels) {
                val dayView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.days_edit_period, gridLayout, false)
                val textView = dayView.findViewById<TextView>(R.id.dayLabel)
                textView.text = label
                gridLayout.addView(dayView)
            }

            // Atur kalender
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Tambahkan ruang kosong sebelum hari pertama
            for (i in 1 until firstDayOfMonth) {
                val emptyView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.dates_edit_period, gridLayout, false)
                emptyView.visibility = View.INVISIBLE
                gridLayout.addView(emptyView)
            }

            // Tambahkan hari-hari dalam bulan
            for (day in 1..daysInMonth) {
                val dateView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.dates_edit_period, gridLayout, false)
                val tvDate = dateView.findViewById<TextView>(R.id.tv_date)
                val btnToggle = dateView.findViewById<Button>(R.id.btn_toggle)

                tvDate.text = day.toString()

                // Set toggle functionality
                btnToggle.setOnClickListener {
                    toggleButton(btnToggle)
                }

                gridLayout.addView(dateView)
            }
        }

        private fun toggleButton(button: Button) {
            // Check/uncheck toggle button
            if (button.isSelected) {
                button.isSelected = false
                button.setBackgroundResource(R.drawable.default_date_background) // Default style
            } else {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.date_button_selector) // Checked style
            }
        }

        private fun getMonthName(month: Int): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        }
    }
}
