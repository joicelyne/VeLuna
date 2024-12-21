package com.example.veluna

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DaysAdapter(private val days: List<String>) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.days_edit_period, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLabel: TextView = itemView.findViewById(R.id.dayLabel)

        fun bind(day: String) {
            dayLabel.text = day
        }
    }
}
