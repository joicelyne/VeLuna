package com.example.veluna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DatesEditPeriod : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var periodDates: List<Date>

    // Fungsi untuk memuat tanggal dari Firebase
    private fun loadPeriodDates() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).collection("period")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dates = mutableListOf<Date>()
                querySnapshot.forEach { document ->
                    val periodDatesLong = document.get("periodDates") as? List<Long> ?: listOf()
                    // Normalisasi semua tanggal agar hanya memperhatikan hari, bulan, dan tahun
                    dates.addAll(periodDatesLong.map { normalizeDate(Date(it)) })
                }

                // Perbarui list tanggal di adapter
                (recyclerView.adapter as CalendarAdapter).setSelectedDates(dates)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Log jika terjadi error
            }
    }

    // Fungsi untuk menormalkan tanggal (menghapus waktu)
    private fun normalizeDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    // Fungsi untuk menyimpan tanggal yang dipilih ke Firebase
    private fun savePeriodDates(dates: List<Date>) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val datesToSave = dates.map { it.time } // Konversi ke format Long untuk Firebase

        db.collection("users").document(userId).collection("period")
            .add(mapOf("periodDates" to datesToSave))
            .addOnSuccessListener {
                // Kembali ke halaman utama
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Log jika terjadi error
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_edit_period)

        recyclerView = findViewById(R.id.calendar_recycler_view)

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Membuat daftar bulan dan tahun
        val monthYearList = mutableListOf<Pair<Int, Int>>()

        // Tambahkan semua bulan dari tahun-tahun sebelumnya
        for (year in 2010..currentYear) { // Ganti `1900` dengan tahun awal yang diinginkan
            for (month in 0..11) { // Semua bulan
                if (year < currentYear || (year == currentYear && month <= currentMonth)) {
                    monthYearList.add(Pair(month, year))
                }
            }
        }

        // Tambahkan 1 bulan setelah bulan dan tahun sekarang
        val nextMonth = if (currentMonth == 11) 0 else currentMonth + 1 // Jika Desember, lompat ke Januari
        val nextYear = if (currentMonth == 11) currentYear + 1 else currentYear // Jika Desember, tahun bertambah
        monthYearList.add(Pair(nextMonth, nextYear))

        // Menentukan posisi bulan saat ini
        val currentIndex = monthYearList.indexOf(Pair(currentMonth, currentYear))

        // Mengatur RecyclerView
        val adapter = CalendarAdapter(monthYearList)
        val layoutManager = GridLayoutManager(this, 1) // 1 bulan per baris
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // **Scroll ke bulan dan tahun saat ini di tengah layar**
        recyclerView.post {
            val offsetPx = recyclerView.height / 2 // Offset agar bulan ini ada di tengah
            if (currentIndex != -1) { // Pastikan indeks valid
                (recyclerView.layoutManager as GridLayoutManager).scrollToPositionWithOffset(currentIndex, offsetPx)
            }
        }

        // Load period dates from Firebase
        loadPeriodDates()

        // Handle tombol Apply
        findViewById<Button>(R.id.applyButton).setOnClickListener {
            val selectedDates = (recyclerView.adapter as CalendarAdapter).getSelectedDates()
            savePeriodDates(selectedDates)
        }

        // Handle tombol Back
        findViewById<ImageButton>(R.id.back_button_edit_perioddate).setOnClickListener {
            finish()
        }
    }


}
