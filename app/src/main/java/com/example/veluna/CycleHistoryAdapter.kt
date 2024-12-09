package com.example.veluna

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class Cycle(val dateRange: String, val cycleLength: Int, val periodLength: Int)

class CycleHistoryAdapter(private var cycles: List<Cycle>) :
    RecyclerView.Adapter<CycleHistoryAdapter.CycleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CycleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cycle_history_item, parent, false)
        return CycleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CycleViewHolder, position: Int) {
        val cycle = cycles[position]
        holder.bind(cycle)
    }

    override fun getItemCount(): Int = cycles.size

    fun updateData(newCycles: List<Cycle>) {
        cycles = newCycles
        notifyDataSetChanged() // Perbarui tampilan RecyclerView
    }

    inner class CycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateRange: TextView = itemView.findViewById(R.id.tv_cycle_date_range)
        private val tvCycleLength: TextView = itemView.findViewById(R.id.tv_cycle_length)
        private val pbPeriodLength: ProgressBar = itemView.findViewById(R.id.pb_period_length)

        fun bind(cycle: Cycle) {
            tvDateRange.text = cycle.dateRange
            tvCycleLength.text = "${cycle.cycleLength} Days"
            pbPeriodLength.progress = calculateProgress(cycle.periodLength, cycle.cycleLength)
        }

        private fun calculateProgress(periodLength: Int, cycleLength: Int): Int {
            return (periodLength.toFloat() / cycleLength * 100).toInt()
        }
    }
}
