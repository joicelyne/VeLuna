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
    private var days: List<DayItem>,
    private var isLoved: Boolean,
    private var periodDates: List<Date> = listOf(),
    private var predictedPeriodDates: List<Date> = listOf(),
    private val onMoodEditClick: (DayItem) -> Unit,
    private val onDateClick: (DayItem) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private var startPeriod: Date? = null
    private var endPeriod: Date? = null

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val moodEditIcon: ImageView = itemView.findViewById(R.id.moodEditIcon)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val dayItem = days[position]
                    onDateClick(dayItem)
                }
            }
        }
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

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayDate = dateFormat.parse(dayItem.fullDate) ?: return
        val normalizedDayDate = normalizeDate(dayDate)

        val normalizedPeriodDates = periodDates.map { normalizeDate(it) }
        val normalizedPredictedDates = predictedPeriodDates.map { normalizeDate(it) }

        when {
            normalizedPeriodDates.contains(normalizedDayDate) -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_now)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white))
            }

            normalizedPredictedDates.contains(normalizedDayDate) -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white))
            }

            dayItem.isToday && isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_now)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }

            dayItem.isToday && !isLoved -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_background)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }

            startPeriod != null && endPeriod != null && normalizedDayDate in normalizeDate(startPeriod!!)..normalizeDate(endPeriod!!) -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_next)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }

            else -> {
                holder.tvDate.setBackgroundResource(R.drawable.circle_period_not)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.color3))
            }
        }

        holder.moodEditIcon.visibility = if (dayItem.isToday) View.VISIBLE else View.GONE
        holder.moodEditIcon.setOnClickListener {
            onMoodEditClick(dayItem)
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<DayItem>, newStartPeriod: Date?, newEndPeriod: Date?, isLoved: Boolean, predictedDates: List<Date> = listOf()) {
        this.days = newDays
        this.startPeriod = newStartPeriod
        this.endPeriod = newEndPeriod
        this.isLoved = isLoved
        this.predictedPeriodDates = predictedDates.map { normalizeDate(it) }
        notifyDataSetChanged()
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
