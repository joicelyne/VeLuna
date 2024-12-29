package com.example.veluna

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainPage : Fragment() {

    // Firebase References
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    // UI Elements
    private lateinit var tvName: TextView
    private lateinit var cycleName: TextView
    private lateinit var insightName: TextView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnLove: ImageView
    private lateinit var tvPeriodStatusText: TextView
    private lateinit var tvPeriodText: TextView
    private lateinit var recyclerViewWeek: RecyclerView
    private lateinit var adapter: DayAdapter
    private lateinit var imgInsight1: ImageView
    private lateinit var txtInsight1: TextView
    private lateinit var imgInsight2: ImageView
    private lateinit var txtInsight2: TextView
    private lateinit var imgInsight3: ImageView
    private lateinit var txtInsight3: TextView
    private lateinit var prevCycleLenText: TextView
    private lateinit var prevPeriodLenText: TextView

    // Calendar instance to track the current week
    private val calendar = Calendar.getInstance()
    private var isLoved = false // Status awal love button
    private lateinit var gestureDetector: GestureDetectorCompat // Gesture detector
    private var periodDates: List<Date> = listOf()
    private var predictedDates: List<Date> = listOf()

    // ViewModel untuk sinkronisasi data
    private lateinit var userViewModel: UserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_page, container, false)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        // Initialize UI elements
        tvName = view.findViewById(R.id.tvName)
        cycleName = view.findViewById(R.id.cycleName)
        insightName = view.findViewById(R.id.insightName)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvPeriodStatusText = view.findViewById(R.id.tvPeriodStatusText)
        tvPeriodText = view.findViewById(R.id.tvPeriodText)
        recyclerViewWeek = view.findViewById(R.id.recyclerViewWeek)
        prevCycleLenText = view.findViewById(R.id.prevCycleLenDays)
        prevPeriodLenText = view.findViewById(R.id.prevPeriodLenDays)

        // Observasi perubahan data
        observeUserData()

        // Set bulan dan tahun
        setMonthYear()

        // Load user data
        loadUserData()
        loadLoveStatus()

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        setupRecyclerView()
        setupGestureDetection()

        // Navigation
        view.findViewById<ImageView>(R.id.editDatePeriodIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_datesEditPeriod)
        }

        view.findViewById<ImageView>(R.id.history_cycle_button).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_cycleHistory)
        }

        imgInsight1 = view.findViewById(R.id.imgInsight1)
        txtInsight1 = view.findViewById(R.id.txtInsight1)
        imgInsight2 = view.findViewById(R.id.imgInsight2)
        txtInsight2 = view.findViewById(R.id.txtInsight2)
        imgInsight3 = view.findViewById(R.id.imgInsight3)
        txtInsight3 = view.findViewById(R.id.txtInsight3)

        // Heartbeat Animation
        btnLove = view.findViewById(R.id.imgHeart)
        val heartbeatAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.heartbeat)
        btnLove.startAnimation(heartbeatAnimation)

        // Handle love button click
        btnLove.setOnClickListener {
            isLoved = !isLoved
            updateLoveStatus(isLoved)
        }


        // Handle Image & Text Insights
        imgInsight1.setOnClickListener {
            openLink("https://www.fertile-gut.com/blogs/news/unlocking-the-secrets-of-your-menstrual-cycle?srsltid=AfmBOopS0nHNuq1ps2EUBoX0t7pQBOJ15ICkec9d_DgYcFWPT9L947BQ")
        }

        txtInsight1.setOnClickListener {
            openLink("https://www.fertile-gut.com/blogs/news/unlocking-the-secrets-of-your-menstrual-cycle?srsltid=AfmBOopS0nHNuq1ps2EUBoX0t7pQBOJ15ICkec9d_DgYcFWPT9L947BQ")
        }

        imgInsight2.setOnClickListener {
            openLink("https://www.rainbowhospitals.in/blog/women-mental-health")
        }

        txtInsight2.setOnClickListener {
            openLink("https://www.rainbowhospitals.in/blog/women-mental-health")
        }

        imgInsight3.setOnClickListener {
            openLink("https://www.invitra.com/en/sperms-journey-to-the-egg/")
        }

        txtInsight3.setOnClickListener {
            openLink("https://www.invitra.com/en/sperms-journey-to-the-egg/")
        }

        return view
    }

    private fun observeUserData() {
        userViewModel.name.observe(viewLifecycleOwner) { name ->
            tvName.text = name
            cycleName.text = "$name's Cycle"
            insightName.text = "$name's Insight"
        }
    }

    private fun setMonthYear() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYear = dateFormat.format(calendar.time)
        tvMonthYear.text = monthYear
    }

    private fun loadUserData() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Log.e("MainPage", "User ID tidak ditemukan.")
            return
        }

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val periodLength = document.getLong("periodLength")?.toInt() ?: 5 // Default 5 hari
                    val cycleLength = document.getLong("cycleLength")?.toInt() ?: 28 // Default 28 hari
                    val userStartDate = parseDate(document.getString("startDate") ?: "") // Ambil startDate dari dokumen `users`

                    tvName.text = name
                    cycleName.text = "$name's Cycle"
                    insightName.text = "$name's Insight"
                    prevCycleLenText.text = "$cycleLength Days"
                    prevPeriodLenText.text = "$periodLength Days"

                    // Periksa apakah ada period aktif di koleksi `period`
                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(1) // Ambil period terbaru
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot != null && !querySnapshot.isEmpty) {
                                // Ambil periodStart dari period terbaru
                                val latestPeriod = querySnapshot.documents.firstOrNull()
                                val periodStartDate = latestPeriod?.getDate("periodStart") // Tanggal dari `periodStart`
                                val periodLengthFromPeriod =
                                    latestPeriod?.getLong("periodLength")?.toInt() ?: periodLength
                                val cycleLengthFromPeriod =
                                    latestPeriod?.getLong("cycleLength")?.toInt() ?: cycleLength

                                // Prediksi berdasarkan periodStart jika ada
                                updatePredictedDates(
                                    cycleLengthFromPeriod,
                                    periodLengthFromPeriod,
                                    periodStartDate ?: userStartDate // Gunakan `startDate` jika `periodStart` null
                                )
                            } else {
                                // Jika tidak ada `period`, gunakan `startDate` dari dokumen `users`
                                updatePredictedDates(cycleLength, periodLength, userStartDate)
                            }
                            val todayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            val dayItem = DayItem(todayDate, "", isToday = true, fullDate = todayDate)
                            onDateClick(dayItem)
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal memuat koleksi `period`: ${e.message}")
                            // Jika gagal, fallback ke `startDate` dari dokumen `users`
                            updatePredictedDates(cycleLength, periodLength, userStartDate)
                        }
                } else {
                    Log.e("MainPage", "Dokumen pengguna tidak ditemukan.")
                    prevCycleLenText.text = "-"
                    prevPeriodLenText.text = "-"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainPage", "Gagal mengambil data: ${exception.message}")
            }
    }

    private fun loadLoveStatus() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Log.e("MainPage", "User ID tidak ditemukan.")
            return
        }

        val userDocRef = db.collection("users").document(currentUserId)

        // Ambil seluruh data periode dari koleksi `period`
        userDocRef.collection("period")
            .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val allPeriodDates = mutableListOf<Date>()
                    var latestStartDate: Date? = null
                    var latestPeriodLength = 5 // Default periodLength
                    var latestCycleLength = 28 // Default cycleLength
                    var latestPeriodId: String? = null // Tambahkan untuk menyimpan periodId terbaru

                    // Proses semua dokumen dalam koleksi `period`
                    querySnapshot.forEach { document ->
                        val periodDatesLong = document.get("periodDates") as? List<Long> ?: listOf()
                        val periodDates = periodDatesLong.map { normalizeDate(Date(it)) }
                        allPeriodDates.addAll(periodDates)

                        // Jika ini periode terbaru (`isStart = true`), simpan informasinya
                        if (document.getBoolean("isStart") == true) {
                            latestStartDate = document.getDate("periodStart")
                            latestPeriodLength = document.getLong("periodLength")?.toInt() ?: 5
                            latestCycleLength = document.getLong("cycleLength")?.toInt() ?: 28
                            latestPeriodId = document.id // Simpan periodId terbaru
                        }
                    }

                    // Hapus duplikat tanggal dari `allPeriodDates`
                    val uniquePeriodDates = allPeriodDates.distinct()

                    // Log periodId terbaru
                    Log.d("Debug", "Latest Period ID: $latestPeriodId")

                    // Hitung tanggal prediksi berdasarkan `latestStartDate`
                    val predictedDates = if (latestStartDate != null) {
                        getPredictedPeriodDates(latestStartDate!!, latestCycleLength, latestPeriodLength)
                    } else {
                        listOf()
                    }

                    // Log prediksi
                    Log.d("Debug", "Predicted Dates from Latest PeriodStart: $predictedDates")

                    // Perbarui UI dengan semua tanggal periode dan tanggal prediksi
                    updateCalendarUI(uniquePeriodDates, predictedDates)
                    val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    val dayItem = DayItem(today, "", isToday = true, fullDate = today)
                    onDateClick(dayItem)
                } else {
                    Log.e("MainPage", "Koleksi `period` kosong, memuat default data.")
                    loadDefaultPeriodData(userDocRef)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal memuat data `period`: ${e.message}")
                updateCalendarUI(listOf(), listOf()) // Tidak ada prediksi jika gagal
            }
        loadUserData()
    }

    // Fungsi untuk memuat data default jika koleksi `period` kosong
    private fun loadDefaultPeriodData(userDocRef: DocumentReference) {
        userDocRef.get()
            .addOnSuccessListener { userDocument ->
                val startDateString = userDocument.getString("startDate")

                // Jika `startDate` tidak ada, langsung perbarui UI tanpa prediksi
                if (startDateString.isNullOrEmpty()) {
                    Log.e("StartDateNull", "startDate tidak ditemukan pada dokumen pengguna.")
                    updateCalendarUI(listOf(), listOf()) // Tidak ada prediksi
                    return@addOnSuccessListener
                }

                // Jika `startDate` ada, hitung prediksi
                val startDate = parseDate(startDateString) ?: return@addOnSuccessListener
                val periodLength = userDocument.getLong("periodLength")?.toInt() ?: 5
                val cycleLength = userDocument.getLong("cycleLength")?.toInt() ?: 28

                // Hitung *predictedDates* berdasarkan `startDate`
                val predictedDates = getPredictedPeriodDates(startDate, cycleLength, periodLength)

                // Log prediksi dari `startDate`
                Log.d("StartDatePredict", "Predicted Dates from StartDate: $predictedDates")

                updateCalendarUI(listOf(), predictedDates)
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal memuat data default dari dokumen pengguna: ${e.message}")
                updateCalendarUI(listOf(), listOf()) // Tidak ada prediksi jika gagal
            }
    }

    // Fungsi untuk memperbarui UI kalender
    private fun updateCalendarUI(periodDates: List<Date>, predictedDates: List<Date>) {
        val currentDate = Date()
        val normalizedCurrentDate = normalizeDate(currentDate)
        val normalizedPeriodDates = periodDates.map { normalizeDate(it) }

        isLoved = normalizedPeriodDates.any { it >= normalizedCurrentDate }

        if (isLoved) {
            btnLove.setImageResource(R.drawable.redheart)

            // Find the normalized period day index
            val periodDayIndex = normalizedPeriodDates.indexOfFirst { it == normalizedCurrentDate } + 1
            tvPeriodStatusText.text = "Day $periodDayIndex"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
            tvPeriodText.setTextColor(resources.getColor(R.color.white))
        } else {
            btnLove.setImageResource(R.drawable.heartgif)
            tvPeriodStatusText.text = "Not Started"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
        }

        // Kombinasikan tanggal dari periode sebelumnya dengan periode baru
        val allPeriodDates = (this.periodDates + periodDates).distinctBy { normalizeDate(it) }
        this.periodDates = allPeriodDates // Simpan semua tanggal untuk referensi berikutnya

        // Perbarui RecyclerView
        adapter.updateDays(
            newDays = getWeeklyDates(),
            newStartPeriod = allPeriodDates.firstOrNull(),
            newEndPeriod = allPeriodDates.lastOrNull(),
            isLoved = isLoved,
            predictedDates = predictedDates,
            periodDates = periodDates
        )
    }

    // Fungsi untuk memparse tanggal dari string
    private fun parseDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun getPredictedPeriodDates(periodStart: Date, cycleLength: Int, periodLength: Int): List<Date> {
        val predictedDates = mutableListOf<Date>()
        val calendar = Calendar.getInstance().apply { time = periodStart }

        // Tambahkan cycleLength dari periodStart
        calendar.time = periodStart
        calendar.add(Calendar.DATE, cycleLength)

        // Tambahkan tanggal untuk periodLength hari ke depan
        for (i in 0 until periodLength) {
            predictedDates.add(calendar.time)
            calendar.add(Calendar.DATE, 1) // Tambah 1 hari
        }

        Log.d("Debug", "Predicted Period Dates: $predictedDates")
        return predictedDates
    }


    private fun updateLoveStatus(isLoved: Boolean) {
        val currentUserId = userId ?: return
        val timestamp = Timestamp.now()
        val today = Calendar.getInstance().apply { time = timestamp.toDate() }

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { userDocument ->
                val periodLength = userDocument.getLong("periodLength")?.toInt() ?: 5
                val cycleLength = userDocument.getLong("cycleLength")?.toInt() ?: 28

                if (isLoved) {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .whereEqualTo("isStart", true)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                val newPeriodId = db.collection("users")
                                    .document(currentUserId)
                                    .collection("period")
                                    .document().id

                                val startPeriod = today.time
                                val periodDates = generateDatesBetween(startPeriod, periodLength)

                                val periodData = mapOf(
                                    "isStart" to true,
                                    "periodStart" to startPeriod,
                                    "periodDates" to periodDates.map { it.time },
                                    "periodLength" to periodLength,
                                    "cycleLength" to cycleLength
                                )

                                // Simpan periode baru
                                db.collection("users")
                                    .document(currentUserId)
                                    .collection("period")
                                    .document(newPeriodId)
                                    .set(periodData)
                                    .addOnSuccessListener {
                                        Log.d("Debug", "Period Baru Disimpan: $periodData")

                                        val predictedDates = getPredictedPeriodDates(startPeriod, cycleLength, periodLength)
                                        updateCalendarUI(periodDates, predictedDates)

                                        currentWeekOffset = 0
                                        this.periodDates = periodDates
                                        adapter.updateDays(
                                            newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                            newStartPeriod = periodDates.firstOrNull(),
                                            newEndPeriod = periodDates.lastOrNull(),
                                            isLoved = true,
                                            predictedDates = predictedDates,
                                            periodDates = periodDates
                                        )

                                        btnLove.setImageResource(R.drawable.redheart)
                                        tvPeriodStatusText.text = "Started"
                                        tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
                                        tvPeriodText.setTextColor(resources.getColor(R.color.white))

                                        loadLoveStatus()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MainPage", "Gagal menyimpan periode baru: ${e.message}")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal memeriksa periode berjalan: ${e.message}")
                        }
                } else {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .whereEqualTo("isStart", true)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            querySnapshot.documents.forEach { document ->
                                val periodStart = document.getTimestamp("periodStart")?.toDate()
                                if (periodStart != null) {
                                    val periodStartCal = Calendar.getInstance().apply { time = periodStart }

                                    val isSameDay = today.get(Calendar.YEAR) == periodStartCal.get(Calendar.YEAR) &&
                                            today.get(Calendar.DAY_OF_YEAR) == periodStartCal.get(Calendar.DAY_OF_YEAR)

                                    if (isSameDay) {
                                        // Hapus data periode dan set isStart ke false
                                        document.reference.update(
                                            mapOf(
                                                "isStart" to false,
                                                "periodDates" to emptyList<Long>(), // Hapus semua tanggal
                                                "periodStart" to null // Set periodStart ke null
                                            )
                                        ).addOnSuccessListener {
                                            Log.d("Debug", "Period dihapus karena klik ulang pada hari yang sama")

                                            btnLove.setImageResource(R.drawable.heartgif)
                                            tvPeriodStatusText.text = "Not Started"
                                            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                                            tvPeriodText.setTextColor(resources.getColor(R.color.color4))

                                            currentWeekOffset = 0
                                            periodDates = listOf() // Kosongkan daftar
                                            adapter.updateDays(
                                                newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                                newStartPeriod = null,
                                                newEndPeriod = null,
                                                isLoved = false,
                                                predictedDates = predictedDates,
                                                periodDates = periodDates
                                            )
                                        }.addOnFailureListener { e ->
                                            Log.e("MainPage", "Gagal menghapus periode: ${e.message}")
                                        }
                                    } else {
                                        val periodDatesLong = document.get("periodDates") as? List<Long> ?: return@forEach
                                        val periodDates = periodDatesLong.map { Date(it) }
                                        val updatedDates = periodDates.filter { it <= today.time }
                                        val periodEnd = updatedDates.lastOrNull()

                                        document.reference.update(
                                            mapOf(
                                                "isStart" to false,
                                                "periodDates" to updatedDates.map { it.time },
                                                "periodEnd" to periodEnd
                                            )
                                        ).addOnSuccessListener {
                                            Log.d("Debug", "Periode Dihentikan")

                                            val predictedDates = getPredictedPeriodDates(updatedDates.lastOrNull() ?: today.time, cycleLength, periodLength)
                                            updateCalendarUI(updatedDates, predictedDates)

                                            currentWeekOffset = 0
                                            adapter.updateDays(
                                                newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                                newStartPeriod = updatedDates.firstOrNull(),
                                                newEndPeriod = updatedDates.lastOrNull(),
                                                isLoved = false,
                                                predictedDates = predictedDates,
                                                periodDates = updatedDates
                                            )

                                            btnLove.setImageResource(R.drawable.heartgif)
                                            tvPeriodStatusText.text = "Not Started"
                                            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                                            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
                                        }.addOnFailureListener { e ->
                                            Log.e("MainPage", "Gagal menghentikan periode berjalan: ${e.message}")
                                        }
                                    }
                                }
                                loadLoveStatus()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal menghentikan periode berjalan: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainPage", "Gagal mengambil periodLength: ${exception.message}")
            }
    }

    private fun generateDatesBetween(startDate: Date, periodLength: Int = 5): List<Date> {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance().apply { time = startDate }

        // Tambahkan tanggal sebanyak periodLength
        for (i in 0 until periodLength) {
            dates.add(calendar.time)
            calendar.add(Calendar.DATE, 1) // Tambah 1 hari
        }

        return dates
    }

    private fun updateMonthYear() {
        // Gunakan currentWeekOffset untuk menghitung tanggal pertama minggu
        val calendarForWeek = Calendar.getInstance()
        calendarForWeek.time = Date() // Mulai dari tanggal hari ini
        calendarForWeek.add(Calendar.WEEK_OF_YEAR, currentWeekOffset) // Terapkan offset minggu
        calendarForWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // Set ke awal minggu

        // Format bulan dan tahun
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYear = dateFormat.format(calendarForWeek.time)
        tvMonthYear.text = monthYear
    }


    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun updatePredictedDates(cycleLength: Int, periodLength: Int, startDate: Date?) {
        if (startDate == null) {
            Log.e("MainPage", "StartDate tidak ditemukan untuk prediksi.")
            return
        }

        // Gunakan fungsi getPredictedPeriodDates
        val predicted = getPredictedPeriodDates(startDate, cycleLength, periodLength)

        this.predictedDates = predicted
        Log.d("PredictedDates", "Updated Predicted Dates: $predictedDates") // Debug

        // Perbarui RecyclerView
        adapter.updateDays(
            newDays = getWeeklyDates(),
            newStartPeriod = periodDates.firstOrNull(),
            newEndPeriod = periodDates.lastOrNull(),
            isLoved = isLoved,
            predictedDates = predictedDates,
            periodDates = periodDates
        )
    }

    private fun onDateClick(dayItem: DayItem) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val clickedDate = dateFormat.parse(dayItem.fullDate) ?: return
        val normalizedClickedDate = normalizeDate(clickedDate)

        Log.d("MainPage", "Clicked Date: $normalizedClickedDate")
        Log.d("MainPage", "Period Dates: ${periodDates.map { normalizeDate(it) }}")
        Log.d("MainPage", "Predicted Dates: ${predictedDates.map { normalizeDate(it) }}")

        // Normalize and sort period dates
        val normalizedPeriodDates = periodDates.map { normalizeDate(it) }.sorted()

        // Ambil periodStart dari period id terakhir
        val latestPeriodStart = periodDates.lastOrNull()?.let { normalizeDate(it) }
        val normalizedPredictedDates = predictedDates.map { normalizeDate(it) }

        // Group period dates into cycles
        val periodCycles = groupDatesIntoCycles(normalizedPeriodDates)

        // Find the cycle that contains the clicked date
        var currentCycle: List<Date>? = null
        for (cycle in periodCycles) {
            if (cycle.contains(normalizedClickedDate)) {
                currentCycle = cycle
                break
            }
        }

        if (currentCycle != null) {
            // Tanggal dalam `periodDates`
            val firstDate = currentCycle.first()
            val dayOffset = ((normalizedClickedDate.time - firstDate.time) / (1000 * 60 * 60 * 24)).toInt()
            val dayNumber = if (dayOffset >= 0) dayOffset + 1 else dayOffset

            tvPeriodStatusText.text = if (dayNumber > 0) "Day $dayNumber" else "Day $dayNumber (Before Cycle)"
            btnLove.setImageResource(R.drawable.redheart)
            tvPeriodText.text = "Period"
            tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
            tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
        } else if (normalizedPredictedDates.contains(normalizedClickedDate)) {
            // Tanggal dalam predictedDates
            val dayNumber = normalizedPredictedDates.indexOf(normalizedClickedDate) + 1
            tvPeriodStatusText.text = "Day $dayNumber"
            tvPeriodText.text = "Prediction: Period"
            btnLove.setImageResource(R.drawable.heartgif)
            tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
            tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
        } else if (latestPeriodStart != null && normalizedClickedDate.after(latestPeriodStart)) {
            // Tanggal setelah predictedDates terakhir
            val nextPredictionDate = normalizedPredictedDates.firstOrNull { it.after(latestPeriodStart) }
            if (nextPredictionDate != null && normalizedClickedDate.before(nextPredictionDate)) {
                val daysBeforePrediction = ((nextPredictionDate.time - normalizedClickedDate.time) / (1000 * 60 * 60 * 24)).toInt()
                tvPeriodStatusText.text = "$daysBeforePrediction Days"
                tvPeriodText.text = "Period in"
                btnLove.setImageResource(R.drawable.heartgif)
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
            } else {
                // Tanggal setelah predictedDates terakhir
                tvPeriodStatusText.text = "Not Started"
                tvPeriodText.text = "Period"
                btnLove.setImageResource(R.drawable.heartgif)
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
            }
        } else if (latestPeriodStart != null && normalizedClickedDate.before(latestPeriodStart)) {
            // Tanggal sebelum periodStart dari periode terakhir
            val daysBeforeFirstCycle = ((latestPeriodStart.time - normalizedClickedDate.time) / (1000 * 60 * 60 * 24)).toInt()
            tvPeriodStatusText.text = "Period in $daysBeforeFirstCycle Days"
            tvPeriodText.text = "Past Cycle"
            tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
            tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
            btnLove.setImageResource(R.drawable.heartgif)
        }else {
            // Tanggal tidak teridentifikasi
            tvPeriodStatusText.text = "Not Started"
            tvPeriodText.text = "Period"
            btnLove.setImageResource(R.drawable.heartgif)
        }
    }

    // Helper function to group dates into cycles
    private fun groupDatesIntoCycles(dates: List<Date>): List<List<Date>> {
        val cycles = mutableListOf<MutableList<Date>>()
        if (dates.isEmpty()) return cycles

        var currentCycle = mutableListOf<Date>()
        currentCycle.add(dates[0])

        for (i in 1 until dates.size) {
            val previousDate = dates[i - 1]
            val currentDate = dates[i]

            // Check if the current date is contiguous with the previous date
            val diffInDays = ((currentDate.time - previousDate.time) / (1000 * 60 * 60 * 24)).toInt()
            if (diffInDays > 1) {
                // Start a new cycle
                cycles.add(currentCycle)
                currentCycle = mutableListOf()
            }
            currentCycle.add(currentDate)
        }
        // Add the last cycle
        cycles.add(currentCycle)

        return cycles
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

    private fun setupRecyclerView() {
        adapter = DayAdapter(
            days = getWeeklyDates(),
            isLoved = isLoved,
            periodDates = periodDates,
            onMoodEditClick = { dayItem ->
                findNavController().navigate(R.id.action_MainPage_to_moodNotes)
            },
            onDateClick = { dayItem ->
                onDateClick(dayItem)
            }
        )


        recyclerViewWeek.layoutManager = GridLayoutManager(requireContext(), 7) // 7 items per row
        recyclerViewWeek.adapter = adapter

        // Pastikan animasi
        recyclerViewWeek.itemAnimator?.apply {
            addDuration = 300
            removeDuration = 300
            moveDuration = 300
            changeDuration = 300
        }

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        // Perbarui bulan dan tahun saat pertama kali RecyclerView dimuat
        updateMonthYear()
    }

    private fun getWeeklyDates(targetDate: Date? = null, weekOffset: Int = 0): List<DayItem> {
        val calendar = Calendar.getInstance()
        if (targetDate != null) calendar.time = targetDate

        // Tambahkan offset minggu sebelum reset ke hari Minggu
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weekDates = mutableListOf<DayItem>()
        val today = Calendar.getInstance() // Untuk menandai `isToday`

        val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (i in 0 until 7) { // Generate 7 hari dalam seminggu
            val date = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)[0].toString()
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            val fullDate = fullDateFormat.format(calendar.time)

            weekDates.add(
                DayItem(
                    date = date,
                    day = dayName,
                    isToday = isToday,
                    fullDate = fullDate
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
        return weekDates
    }


    private fun setupGestureDetection() {
        gestureDetector = GestureDetectorCompat(requireContext(), object :
            GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 50
            private val SWIPE_VELOCITY_THRESHOLD = 50

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d("Gesture", "Swiped Right: Loading Previous Week")
                            loadPreviousWeek()
                        } else {
                            Log.d("Gesture", "Swiped Left: Loading Next Week")
                            loadNextWeek()
                        }
                        return true
                    }
                }
                return false
            }
        })

        recyclerViewWeek.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }
    private var currentWeekOffset = 0
    private fun loadPreviousWeek() {
        currentWeekOffset -= 1 // Mundur ke minggu sebelumnya
        recyclerViewWeek.animate().translationX(recyclerViewWeek.width.toFloat())
            .setDuration(300).withEndAction {
                recyclerViewWeek.translationX = -recyclerViewWeek.width.toFloat()
                adapter.updateDays(
                    newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                    newStartPeriod = periodDates.firstOrNull(),
                    newEndPeriod = periodDates.lastOrNull(),
                    isLoved = isLoved,
                    predictedDates = predictedDates,
                    periodDates = periodDates
                )
                recyclerViewWeek.animate().translationX(0f).setDuration(300).start()
                updateMonthYear()
            }.start()
    }

    private fun loadNextWeek() {
        currentWeekOffset += 1 // Maju ke minggu berikutnya
        recyclerViewWeek.animate().translationX(-recyclerViewWeek.width.toFloat())
            .setDuration(300).withEndAction {
                recyclerViewWeek.translationX = recyclerViewWeek.width.toFloat()
                adapter.updateDays(
                    newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                    newStartPeriod = periodDates.firstOrNull(),
                    newEndPeriod = periodDates.lastOrNull(),
                    isLoved = isLoved,
                    predictedDates = predictedDates,
                    periodDates = periodDates
                )
                recyclerViewWeek.animate().translationX(0f).setDuration(300).start()
                updateMonthYear()
            }.start()
    }


//    private fun updatePeriodDates(periodLength: Int) {
//        if (periodDates.isEmpty()) return
//
//        val startPeriod = periodDates.first()
//
//        // Generate dates langsung berdasarkan startPeriod dan periodLength
//        val updatedPeriodDates = generateDatesBetween(startPeriod, periodLength)
//        this.periodDates = updatedPeriodDates
//
//        // Update adapter dengan tanggal baru
//        adapter.updateDays(
//            newDays = getWeeklyDates(),
//            newStartPeriod = updatedPeriodDates.firstOrNull(),
//            newEndPeriod = updatedPeriodDates.lastOrNull(),
//            isLoved = isLoved,
//            predictedDates = predictedDates,
//            periodDates = periodDates
//        )
//    }
}
