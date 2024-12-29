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
import com.google.firebase.firestore.SetOptions
import java.util.*

class DatesEditPeriod : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var periodDates: List<Date>
    private val dateToPeriodIdMap = mutableMapOf<Date, String>()

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
                    val periodId = document.id
                    val periodDates = periodDatesLong.map { normalizeDate(Date(it)) }

                    periodDates.forEach { date ->
                        dateToPeriodIdMap[date] = periodId
                    }

                    dates.addAll(periodDates)
                }

                // Perbarui adapter dengan semua tanggal
                (recyclerView.adapter as CalendarAdapter).setSelectedDates(dates)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
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

        val periodIdToDates = mutableMapOf<String, MutableList<Long>>()
        val newDates = mutableListOf<Date>()

        // Kelompokkan tanggal berdasarkan period ID
        dates.forEach { date ->
            val periodId = dateToPeriodIdMap[date]
            if (periodId != null) {
                // Tambahkan tanggal ke period ID yang ada
                periodIdToDates.getOrPut(periodId) { mutableListOf() }.add(date.time)
            } else {
                // Tanggal tanpa period ID dianggap baru
                newDates.add(date)
            }
        }

        // Tangani tanggal baru (tanpa period ID)
        newDates.forEach { newDate ->
            val nearestPeriodId = findNearestPeriodId(newDate)
            if (nearestPeriodId != null) {
                // Tambahkan tanggal baru ke period ID terdekat
                periodIdToDates.getOrPut(nearestPeriodId) { mutableListOf() }.add(newDate.time)
            } else {
                // Tidak ada period ID terdekat, tambahkan sebagai period baru
                val newPeriodId = UUID.randomUUID().toString()
                periodIdToDates[newPeriodId] = mutableListOf(newDate.time)
            }
        }

        // Update dokumen untuk setiap period ID
        periodIdToDates.forEach { (periodId, periodDates) ->
            db.collection("users")
                .document(userId)
                .collection("period")
                .document(periodId)
                .update("periodDates", periodDates)
                .addOnSuccessListener {
                    println("Successfully updated periodDates for periodId: $periodId")
                }
                .addOnFailureListener { exception ->
                    println("Failed to update periodDates for periodId: $periodId - ${exception.message}")
                }
        }

        // Setelah selesai, kembali ke halaman utama
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun findNearestPeriodId(newDate: Date): String? {
        val threshold = 1 // Batas maksimum hari untuk dianggap "bersebelahan"
        var nearestPeriodId: String? = null
        var minDiff = Int.MAX_VALUE

        dateToPeriodIdMap.forEach { (date, periodId) ->
            val diff = ((normalizeDate(newDate).time - normalizeDate(date).time) / (1000 * 60 * 60 * 24)).toInt()
            if (diff in -threshold..threshold && diff < minDiff) {
                nearestPeriodId = periodId
                minDiff = diff
            }
        }

        return nearestPeriodId
    }

}
