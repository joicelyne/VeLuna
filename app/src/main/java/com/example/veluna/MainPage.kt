package com.example.veluna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class MainPage : Fragment() {

    private var firstDay: Date? = null // Store the first day of the period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_page, container, false)

        val markDay1Button: Button = view.findViewById(R.id.markDay1Button)
        val periodStartDate: TextView = view.findViewById(R.id.periodStartDate)

        markDay1Button.setOnClickListener {
            firstDay = Date() // Capture the current date
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            periodStartDate.text = "Day 1 marked: ${formatter.format(firstDay)}"
        }

        return view
    }
}