import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.veluna.R

data class Cycle(val dateRange: String, val cycleLength: Int, val periodLength: Int)

class CycleHistoryAdapter :
    ListAdapter<Cycle, CycleHistoryAdapter.CycleViewHolder>(CycleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CycleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cycle_history_item, parent, false)
        return CycleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CycleViewHolder, position: Int) {
        val cycle = getItem(position)
        holder.bind(cycle)
    }

    class CycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateRange: TextView = itemView.findViewById(R.id.tv_cycle_date_range)
        private val tvCycleLength: TextView = itemView.findViewById(R.id.tv_cycle_length)
        private val pbPeriodLength: ProgressBar = itemView.findViewById(R.id.pb_period_length)

        fun bind(cycle: Cycle) {
            tvDateRange.text = cycle.dateRange
            tvCycleLength.text = "${cycle.cycleLength} Days"
            pbPeriodLength.progress = calculateProgress(cycle.periodLength, cycle.cycleLength)
        }

        private fun calculateProgress(periodLength: Int, cycleLength: Int): Int {
            return if (cycleLength != 0) {
                (periodLength.toFloat() / cycleLength * 100).toInt()
            } else 0
        }
    }

    class CycleDiffCallback : DiffUtil.ItemCallback<Cycle>() {
        override fun areItemsTheSame(oldItem: Cycle, newItem: Cycle): Boolean {
            // Compare by unique properties
            return oldItem.dateRange == newItem.dateRange
        }

        override fun areContentsTheSame(oldItem: Cycle, newItem: Cycle): Boolean {
            // Compare all contents
            return oldItem == newItem
        }
    }
}
