package com.example.veluna

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

        // Logika untuk menentukan status berdasarkan isToday, isLoved, dan rentang periode
        when {
            // Tanggal hari ini dalam periode (isLoved = true)
            dayItem.isToday && isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_now)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
            // Tanggal hari ini di luar periode (isLoved = false)
            dayItem.isToday && !isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_background)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
            // Tanggal lain dalam periode (rentang antara startPeriod dan endPeriod)
            startPeriod != null && endPeriod != null && dayDate in startPeriod!!..endPeriod!! -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
            // Tanggal lainnya di luar periode
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
    fun updateDays(newDays: List<DayItem>, newStartPeriod: Date?, newEndPeriod: Date?, isLoved: Boolean) {
        this.days = newDays
        this.startPeriod = newStartPeriod
        this.endPeriod = newEndPeriod
        this.isLoved = isLoved
        notifyDataSetChanged()
    }

}
