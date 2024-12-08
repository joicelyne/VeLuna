package com.example.veluna

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DayAdapter(
    private var days: List<DayItem>, // Now it's mutable for updates
    private val onMoodEditClick: (DayItem) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

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

        val dayDiff = calculateDayDiff(dayItem.date)

        // Ubah drawable berdasarkan kondisi
        when {
            // Tanggal hari ini
            dayItem.isToday -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_now)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3)) // Teks putih
            }
            // Tanggal dalam periode masa depan (5 hari ke depan)
            dayDiff in 1..5 -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white)) // Teks putih
            }
            // Tanggal periode yang sudah terlewat
            dayDiff < 0 && Math.abs(dayDiff) <= 5 -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_bef)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white)) // Teks putih
            }
            // Tanggal lainnya (di luar periode)
            else -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_not)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3)) // Warna teks default
            }
        }


        holder.moodEditIcon.visibility = if (dayItem.isToday) View.VISIBLE else View.GONE

        holder.moodEditIcon.setOnClickListener {
            onMoodEditClick(dayItem)
        }
    }

    private fun calculateDayDiff(dateString: String): Int {
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val day = dateFormat.parse(dateString) ?: return -1
        val today = Calendar.getInstance().time
        return ((day.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    override fun getItemCount(): Int = days.size

    // Function to update the list of days and refresh the RecyclerView
    fun updateDays(newDays: List<DayItem>) {
        this.days = newDays
        notifyDataSetChanged() // Notify the adapter that data has changed
    }
}
