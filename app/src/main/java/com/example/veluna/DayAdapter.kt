package com.example.veluna

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DayAdapter(
    private var days: List<DayItem>, // List of DayItem
    private var isLoved: Boolean,
    private var predictedPeriodDates: List<Date> = listOf(),
    private var periodDates: List<Date> = listOf(),
    private val onMoodEditClick: (DayItem) -> Unit // Callback for mood edit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    // Rentang periode
    private var startPeriod: Date? = null
    private var endPeriod: Date? = null

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val moodEditIcon: ImageView = itemView.findViewById(R.id.moodEditIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_main_date_item, parent, false)
        return DayViewHolder(view)
    }
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayItem = days[position]
        holder.tvDate.text = dayItem.date
        holder.tvDay.text = dayItem.day

        // Konversi dateString menjadi objek Date
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayDate = dateFormat.parse(dayItem.fullDate) ?: return

        // Normalisasi Tanggal untuk memastikan waktu di-set ke 00:00:00
        val normalizedDayDate = normalizeDate(dayDate)
        val normalizedPredictedDates = predictedPeriodDates.map { normalizeDate(it) }

        // Logika untuk menentukan status berdasarkan isToday, isLoved, dan rentang periode
        when {
            normalizedPredictedDates.contains(normalizedDayDate) -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white))
                Log.d("DayAdapter", "Predicted Period Match: $normalizedDayDate")
            }
            periodDates.contains(normalizedDayDate) && normalizedDayDate.before(normalizeDate(Date())) -> { // Tanggal dari `periodDates` lainnya
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_bef)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white))
                Log.d("DayAdapter", "In Other Period Dates: $normalizedDayDate")
            }
            dayItem.isToday && isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_now)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
                Log.d("DayAdapter", "Today in Loved Period: $normalizedDayDate")
            }
            dayItem.isToday && !isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_background)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
            startPeriod != null && endPeriod != null && normalizedDayDate in normalizeDate(startPeriod!!)..normalizeDate(endPeriod!!) -> {
                if (normalizedDayDate.before(normalizeDate(Date()))) {
                    holder.tvDate.setBackgroundResource(R.drawable.circle_period_bef)
                    holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.black))
                    Log.d("DayAdapter", "Before Today in Period Range: $normalizedDayDate")
                } else {
                    holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                    holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
                    Log.d("DayAdapter", "In Current Period Range: $normalizedDayDate")
                }
            }
            else -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_not)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
        }

        // Tampilkan ikon edit hanya untuk hari ini
        holder.moodEditIcon.visibility = if (dayItem.isToday) View.VISIBLE else View.GONE

        // Set klik ikon edit
        holder.moodEditIcon.setOnClickListener {
            onMoodEditClick(dayItem)
        }
    }


    override fun getItemCount(): Int = days.size

    // Perbarui daftar hari dan rentang periode
    fun updateDays(newDays: List<DayItem>, newStartPeriod: Date?, newEndPeriod: Date?, isLoved: Boolean, predictedDates: List<Date> = listOf(), periodDates: List<Date> = listOf()) {
        this.days = newDays
        this.startPeriod = newStartPeriod
        this.endPeriod = newEndPeriod
        this.isLoved = isLoved
        this.predictedPeriodDates = predictedDates.map { normalizeDate(it) }
        this.periodDates = periodDates.map { normalizeDate(it) }
        notifyDataSetChanged()

        // Debugging: Log untuk memeriksa predictedDates yang diperbarui
        Log.d("DayAdapterUpd", "Updated Predicted Dates: $predictedPeriodDates")
    }

    // Fungsi untuk menormalkan waktu ke 00:00:00 agar perbandingan hanya pada tanggal
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
