package com.example.veluna

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarAdapter(
    private val monthYearList: List<Pair<Int, Int>>
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val selectedDates = mutableSetOf<Date>()
    fun getSelectedDates(): List<Date> = selectedDates.toList()

    fun setSelectedDates(dates: List<Date>) {
        selectedDates.clear()
        selectedDates.addAll(dates.map { normalizeDate(it) }) // Normalisasi semua tanggal
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calender_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val (month, year) = monthYearList[position]
        holder.bind(month, year)

        val dayLabelsRecyclerView = holder.itemView.findViewById<RecyclerView>(R.id.day_labels_recycler)
        setupDayLabels(dayLabelsRecyclerView)
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

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            // Hari pertama bulan (1 = Minggu, 7 = Sabtu, 2 = Senin, dst.)
            val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)

            // Jumlah hari dalam bulan
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Ambil tanggal hari ini
            val today = normalizeDate(Date())

            // Tambahkan placeholder untuk hari kosong sebelum hari pertama
            for (i in 1 until firstDayOfMonth) {
                val emptyView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.dates_edit_period, gridLayout, false)
                emptyView.visibility = View.INVISIBLE // Elemen tidak terlihat
                gridLayout.addView(emptyView)
            }

            // Tambahkan hari-hari dalam bulan
            for (day in 1..daysInMonth) {
                val dateView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.dates_edit_period, gridLayout, false)
                val tvDate = dateView.findViewById<TextView>(R.id.tv_date)
                val btnToggle = dateView.findViewById<Button>(R.id.btn_toggle)

                tvDate.text = day.toString()

                val date = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.MONTH, month)
                    set(Calendar.YEAR, year)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                // Logika untuk tanggal di periodDates tetapi belum lewat today
                if (selectedDates.contains(date) && date.after(today)) {
                    btnToggle.isEnabled = false // Nonaktifkan tombol
                    btnToggle.setBackgroundResource(R.drawable.predict_date_background) // Gunakan drawable khusus
                    tvDate.setTextColor(itemView.context.getColor(R.color.black)) // Warna teks normal
                }
                // Logika untuk tanggal di periodDates dan sudah lewat today
                else if (selectedDates.contains(date) && !date.after(today)) {
                    btnToggle.isSelected = true
                    btnToggle.setBackgroundResource(R.drawable.selected_date_background) // Drawable periodDates
                    tvDate.setTextColor(itemView.context.getColor(R.color.black)) // Teks normal
                    btnToggle.setOnClickListener {
                        toggleButton(btnToggle, date) // Masih bisa di-interaksi
                    }
                }
                // Logika untuk tanggal di luar periodDates
                else {
                    if (date.after(today)) {
                        btnToggle.isEnabled = false // Nonaktifkan interaksi
                        btnToggle.setBackgroundResource(R.drawable.next_date_background) // Gunakan drawable prediksi
                        tvDate.setTextColor(itemView.context.getColor(R.color.grey)) // Ubah warna teks menjadi abu-abu
                    } else {
                        btnToggle.isSelected = false
                        btnToggle.setBackgroundResource(R.drawable.default_date_background) // Drawable default
                        tvDate.setTextColor(itemView.context.getColor(R.color.black)) // Teks normal
                        btnToggle.setOnClickListener {
                            toggleButton(btnToggle, date)
                        }
                    }
                }

                gridLayout.addView(dateView)
            }
        }



        private fun toggleButton(button: Button, date: Date) {
            if (button.isSelected) {
                button.isSelected = false
                button.setBackgroundResource(R.drawable.default_date_background)
                selectedDates.remove(date)
            } else {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.selected_date_background)
                selectedDates.add(date)
            }
        }

        private fun getMonthName(month: Int): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        }
    }

    private fun setupDayLabels(recyclerView: RecyclerView) {
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val adapter = DaysAdapter(daysOfWeek)
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 7) // 7 kolom untuk 7 hari
        recyclerView.adapter = adapter
    }

    private fun normalizeDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
