package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.MainActivity
import com.example.veluna.R
import com.example.veluna.databinding.FragmentInsertLastPeriodBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class InsertLastPeriodFragment : Fragment() {

    private var _binding: FragmentInsertLastPeriodBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsertLastPeriodBinding.inflate(inflater, container, false)

        // Inisialisasi tanggal default
        selectedDate = binding.calendarView.date
        updateSelectedDateDisplay(selectedDate)

        // Listener untuk perubahan tanggal di CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        }

        // Tombol OK untuk mengonfirmasi tanggal yang dipilih
        binding.okButton.setOnClickListener {
            updateSelectedDateDisplay(selectedDate)
            Toast.makeText(activity, "Tanggal berhasil dipilih", Toast.LENGTH_SHORT).show()
        }

        // Tombol Cancel
        binding.cancelButton.setOnClickListener {
            Toast.makeText(activity, "Pemilihan tanggal dibatalkan", Toast.LENGTH_SHORT).show()
        }

        // Tombol Next untuk menyimpan tanggal dan navigasi ke fragment berikutnya
        binding.nextButton.setOnClickListener {
            if (selectedDate == 0L) {
                Toast.makeText(activity, "Please select a date before proceeding", Toast.LENGTH_SHORT).show()
            } else {
                saveSelectedDate()
                findNavController().navigate(R.id.action_to_periodCycleFragment)
            }
        }

        // Tombol Skip langsung navigasi tanpa menyimpan tanggal
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_periodCycleFragment)
        }

        // Tombol Back
        binding.backContainer.setOnClickListener { onBackButtonClicked(it) }
        binding.backButton.setOnClickListener { onBackButtonClicked(it) }
        binding.backText.setOnClickListener { onBackButtonClicked(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()
    }

    // Update tampilan tanggal yang dipilih
    private fun updateSelectedDateDisplay(selectedDate: Long) {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(selectedDate))
        binding.selectDate.text = dateString
    }

    // Simpan tanggal yang dipilih ke Firestore
    private fun saveSelectedDate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))

        val updatedData = mapOf(
            "userId" to userId,
            "lastPeriod" to formattedDate
        )

        db.collection("users").document(userId)
            .set(updatedData, com.google.firebase.firestore.SetOptions.merge()) // Merge digunakan
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tanggal terakhir periode berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    // Tombol back
    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
