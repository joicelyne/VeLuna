package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.R
import com.example.veluna.databinding.FragmentInsertLastPeriodBinding
import java.text.SimpleDateFormat
import java.util.*

class InsertLastPeriodFragment : Fragment() {

    private var _binding: FragmentInsertLastPeriodBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInsertLastPeriodBinding.inflate(inflater, container, false)

        selectedDate = binding.calendarView.date
        updateSelectedDateDisplay(selectedDate)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        }

        binding.okButton.setOnClickListener {
            updateSelectedDateDisplay(selectedDate)
        }

        binding.cancelButton.setOnClickListener {
            Toast.makeText(activity, "Date selection cancelled", Toast.LENGTH_SHORT).show()
        }

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_periodCycleFragment)
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_periodCycleFragment)
        }

        binding.backContainer.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backButton.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backText.setOnClickListener {
            onBackButtonClicked(it)
        }

        return binding.root
    }

    private fun updateSelectedDateDisplay(selectedDate: Long) {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(selectedDate))
        binding.selectDate.text = dateString
    }

    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
