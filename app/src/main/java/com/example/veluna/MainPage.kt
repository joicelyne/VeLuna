package com.example.veluna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class MainPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_page, container, false)

        view.findViewById<ImageView>(R.id.editDatePeriodIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_datesEditPeriod)
        }

        view.findViewById<ImageView>(R.id.moodEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_moodNotes)
        }

        return view
    }
}