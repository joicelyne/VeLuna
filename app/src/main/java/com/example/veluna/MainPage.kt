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
import com.google.firebase.firestore.Query
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
    private lateinit var imgStatusCycle: ImageView
    private lateinit var statusCycle: TextView
    private lateinit var imgStatusPeriod: ImageView
    private lateinit var statusPeriod: TextView
    private lateinit var currentCycleHis: TextView
    private lateinit var currentPeriodHis: TextView
    private lateinit var perStartDate: TextView
    private lateinit var cycStartDate: TextView
    var isPeriodIdFound = false

    // Calendar instance to track the current week
    private val calendar = Calendar.getInstance()
    private var isLoved = false // Status awal love button
    private lateinit var gestureDetector: GestureDetectorCompat // Gesture detector
    private var periodDates: List<Date> = listOf()
    private var predictedDates: List<Date> = listOf()
    private val dateToPeriodIdMap = mutableMapOf<Date, String>()


    // ViewModel untuk sinkronisasi data
    private lateinit var userViewModel: UserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showBottomNavigation()
        loadUserData()
        loadLoveStatus()
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val dayItem = DayItem(today, "", isToday = true, fullDate = today)
        onDateClick(dayItem)
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
        imgStatusCycle = view.findViewById(R.id.ImgStatusCycle)
        statusCycle = view.findViewById(R.id.StatusCycle)
        imgStatusPeriod = view.findViewById(R.id.ImgStatusPeriod)
        statusPeriod = view.findViewById(R.id.StatusPeriod)
        currentCycleHis = view.findViewById(R.id.CurrentCycleHis)
        currentPeriodHis = view.findViewById(R.id.CurrentPeriodHis)
        perStartDate = view.findViewById(R.id.PerStartDate)
        cycStartDate = view.findViewById(R.id.CycStartDate)

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

                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(2) // Ambil dua dokumen teratas
                        .get()
                        .addOnSuccessListener { previousQuerySnapshot ->
                            if (previousQuerySnapshot != null && previousQuerySnapshot.documents.size > 1) {
                                // Ambil dokumen kedua untuk period ID sebelumnya
                                val previousPeriod = previousQuerySnapshot.documents[1]
                                val previousPeriodStartDate = previousPeriod.getDate("periodStart")
                                val periodLengthFromPrevious = previousPeriod.getLong("periodLength")?.toInt() ?: periodLength
                                val cycleLengthFromPrevious = previousPeriod.getLong("cycleLength")?.toInt() ?: cycleLength

                                // Log dan gunakan data dari period sebelumnya
                                Log.d("MainPage", "Data dari period ID sebelumnya digunakan.")
                                Log.d("MainPage", "Previous Period Start: $previousPeriodStartDate")
                                Log.d("MainPage", "Cycle Length: $cycleLengthFromPrevious, Period Length: $periodLengthFromPrevious")

                                prevCycleLenText.text = "$cycleLengthFromPrevious Days"
                                prevPeriodLenText.text = "$periodLengthFromPrevious Days"

                                // Atur status Cycle berdasarkan panjang siklus
                                if (cycleLengthFromPrevious > 30 || cycleLengthFromPrevious < 20) {
                                    imgStatusCycle.setImageResource(R.drawable.period_abnormal)
                                    statusCycle.text = "Abnormal"
                                } else {
                                    imgStatusCycle.setImageResource(R.drawable.period_normal)
                                    statusCycle.text = "Normal"
                                }

                                // Atur status Period berdasarkan panjang periode
                                if (periodLengthFromPrevious > 8 || periodLengthFromPrevious < 3) {
                                    imgStatusPeriod.setImageResource(R.drawable.period_abnormal)
                                    statusPeriod.text = "Abnormal"
                                } else {
                                    imgStatusPeriod.setImageResource(R.drawable.period_normal)
                                    statusPeriod.text = "Normal"
                                }


                            } else {
                                // Jika tidak ada period ID, fallback ke user data
                                prevCycleLenText.text = "$cycleLength Days"
                                prevPeriodLenText.text = "$periodLength Days"

                                // Status default untuk user-level data
                                if (cycleLength > 30 || cycleLength < 20) {
                                    imgStatusCycle.setImageResource(R.drawable.period_abnormal)
                                    statusCycle.text = "Abnormal"
                                } else {
                                    imgStatusCycle.setImageResource(R.drawable.period_normal)
                                    statusCycle.text = "Normal"
                                }

                                if (periodLength > 8 || periodLength < 3) {
                                    imgStatusPeriod.setImageResource(R.drawable.period_abnormal)
                                    statusPeriod.text = "Abnormal"
                                } else {
                                    imgStatusPeriod.setImageResource(R.drawable.period_normal)
                                    statusPeriod.text = "Normal"
                                }
                                Log.d("MainPage", "Tidak ada period ID sebelumnya ditemukan.")
                            }
                        }

                    // Periksa apakah ada period aktif di koleksi `period`
                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                // Ambil period ID terbaru
                                val latestPeriod = querySnapshot.documents.firstOrNull()
                                val periodStartDate = latestPeriod?.getDate("periodStart") // Tanggal dari `periodStart`
                                val periodLengthFromPeriod =
                                    latestPeriod?.getLong("periodLength")?.toInt() ?: periodLength
                                val cycleLengthFromPeriod =
                                    latestPeriod?.getLong("cycleLength")?.toInt() ?: cycleLength
                                val formattedPeriodStartDate = periodStartDate?.let {
                                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                                }

                                // Set text berdasarkan period ID
                                currentCycleHis.text = "Current Cycle: $cycleLengthFromPeriod Days"
                                currentPeriodHis.text = "Last Period Length: $periodLengthFromPeriod Days"
                                perStartDate.text = "Started $formattedPeriodStartDate"
                                cycStartDate.text = "Started $formattedPeriodStartDate"

                                Log.d("MainPage", "Menggunakan data dari period ID terbaru.")
                                Log.d("MainPage", "Period Start: $periodStartDate")
                                Log.d("MainPage", "Cycle Length: $cycleLengthFromPeriod, Period Length: $periodLengthFromPeriod")

                                // Prediksi berdasarkan periodStart jika ada
                                updatePredictedDates(
                                    cycleLengthFromPeriod,
                                    periodLengthFromPeriod,
                                    periodStartDate ?: userStartDate // Gunakan `startDate` jika `periodStart` null
                                )
                            } else {
                                val formattedUserStartDate = userStartDate?.let {
                                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                                }
                                // Jika tidak ada period ID, fallback ke user data
                                currentCycleHis.text = "Current Cycle: $cycleLength Days"
                                currentPeriodHis.text = "Last Period Length: $periodLength Days"
                                perStartDate.text = "Started $formattedUserStartDate"
                                cycStartDate.text = "Started $formattedUserStartDate"

                                Log.w("MainPage", "Tidak ada period ID ditemukan, menggunakan data pengguna.")
                                if (userStartDate != null) {
                                    updatePredictedDates(cycleLength, periodLength, userStartDate)
                                } else {
                                    Log.e("MainPage", "StartDate tidak ditemukan di dokumen pengguna.")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal memuat koleksi `period`: ${e.message}")
                            // Jika gagal, fallback ke user data
                            prevCycleLenText.text = "$cycleLength Days"
                            prevPeriodLenText.text = "$periodLength Days"
                            if (userStartDate != null) {
                                updatePredictedDates(cycleLength, periodLength, userStartDate)
                            } else {
                                Log.e("MainPage", "StartDate tidak ditemukan di dokumen pengguna.")
                            }
                        }
                } else {
                    Log.e("MainPage", "Dokumen pengguna tidak ditemukan.")
                    prevCycleLenText.text = "-"
                    prevPeriodLenText.text = "-"
                    imgStatusCycle.setImageResource(R.drawable.period_abnormal)
                    statusCycle.text = "Abnormal"
                    imgStatusPeriod.setImageResource(R.drawable.period_abnormal)
                    statusPeriod.text = "Abnormal"
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
        loadPeriodData()
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
                                db.collection("users")
                                    .document(currentUserId)
                                    .collection("period")
                                    .orderBy("periodStart", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { lastPeriodSnapshot ->
                                        val lastPeriod = lastPeriodSnapshot.documents.firstOrNull()
                                        val lastPeriodStartDate = lastPeriod?.getTimestamp("periodStart")?.toDate()

                                        val calculatedCycleLength = if (lastPeriodStartDate != null) {
                                            val diff = (timestamp.toDate().time - lastPeriodStartDate.time) / (1000 * 60 * 60 * 24)
                                            diff.toInt()
                                        } else {
                                            cycleLength
                                        }

                                        val newPeriodId = db.collection("users")
                                            .document(currentUserId)
                                            .collection("period")
                                            .document().id

                                        val startPeriod = today.time
                                        val periodDatesL = generateDatesBetween(startPeriod, periodLength)

                                        val periodData = mapOf(
                                            "isStart" to true,
                                            "periodStart" to startPeriod,
                                            "periodDates" to periodDatesL.map { it.time },
                                            "periodLength" to periodLength,
                                            "cycleLength" to calculatedCycleLength
                                        )

                                        db.collection("users")
                                            .document(currentUserId)
                                            .collection("period")
                                            .document(newPeriodId)
                                            .set(periodData)
                                            .addOnSuccessListener {
                                                Log.d("Debug", "Period Baru Disimpan: $periodData")

                                                val predictedDates = getPredictedPeriodDates(startPeriod, calculatedCycleLength, periodLength)
                                                updateCalendarUI(periodDatesL, predictedDates)

                                                currentWeekOffset = 0
                                                adapter.updateDays(
                                                    newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                                    newStartPeriod = periodDatesL.firstOrNull(),
                                                    newEndPeriod = periodDatesL.lastOrNull(),
                                                    isLoved = true,
                                                    predictedDates = predictedDates,
                                                    periodDates = periodDatesL
                                                )

                                                val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                                val dayItem = DayItem(today, "", isToday = true, fullDate = today)
                                                onDateClick(dayItem)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("MainPage", "Gagal menyimpan periode baru: ${e.message}")
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MainPage", "Gagal menghitung cycleLength: ${e.message}")
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
                                        // Hapus dokumen periode berdasarkan periodId
                                        document.reference.update(
                                            mapOf(
                                                "isStart" to false,
                                                "periodDates" to emptyList<Long>(),
                                                "periodStart" to null,
                                                "periodLength" to null,
                                                "cycleLength" to null
                                            )
                                        ).addOnSuccessListener {
                                            Log.d("Debug", "Period dihapus karena klik ulang pada hari yang sama")
                                            currentWeekOffset = 0
                                            periodDates = listOf()
                                            adapter.updateDays(
                                                newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                                newStartPeriod = null,
                                                newEndPeriod = null,
                                                isLoved = false,
                                                predictedDates = predictedDates,
                                                periodDates = periodDates
                                            )
                                            btnLove.setImageResource(R.drawable.heartgif)
                                            tvPeriodStatusText.text = "Not Started"
                                            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                                            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
                                        }.addOnFailureListener { e ->
                                            Log.e("MainPage", "Gagal menghapus periode: ${e.message}")
                                        }
                                    } else {
                                        val periodDatesLong = document.get("periodDates") as? List<Long> ?: return@forEach
                                        val periodDatesUnL = periodDatesLong.map { Date(it) }

                                        // Pastikan today dalam bentuk Date
                                        val todayDate = Calendar.getInstance().time

                                        // Filter periodDates untuk hanya menyertakan tanggal yang lebih kecil dari hari ini
                                        val updatedDates = periodDatesUnL.filter { it.before(todayDate) }

                                        // Hapus tanggal yang dihentikan (misalnya tanggal 1)
                                        val normalizedClickedDate = normalizeDate(todayDate)  // Misalnya, tanggal yang dihentikan adalah today
                                        val updatedDatesWithoutUnlovedDate = updatedDates.filter { normalizeDate(it) != normalizedClickedDate }

                                        // Hitung panjang periode berdasarkan updatedDates tanpa tanggal yang dihentikan
                                        val calculatedPeriodLength = updatedDatesWithoutUnlovedDate.size

                                        // Ambil periodEnd dari tanggal terakhir yang valid
                                        val periodEnd = updatedDatesWithoutUnlovedDate.lastOrNull()

                                        // Update Firestore untuk menghapus tanggal yang dihentikan
                                        document.reference.update(
                                            mapOf(
                                                "isStart" to false,
                                                "periodDates" to updatedDatesWithoutUnlovedDate.map { it.time },
                                                "periodEnd" to periodEnd?.time,
                                                "periodLength" to calculatedPeriodLength
                                            )
                                        ).addOnSuccessListener {
                                            Log.d("Debug", "Periode Dihentikan dengan periodLength yang valid: $calculatedPeriodLength")

                                            // Mendapatkan predictedDates berdasarkan periodEnd yang valid
                                            val predictedDates = getPredictedPeriodDates(
                                                updatedDatesWithoutUnlovedDate.lastOrNull() ?: todayDate,
                                                cycleLength,
                                                calculatedPeriodLength
                                            )
                                            updateCalendarUI(periodDatesUnL, predictedDates)

                                            currentWeekOffset = 0
                                            adapter.updateDays(
                                                newDays = getWeeklyDates(weekOffset = currentWeekOffset),
                                                newStartPeriod = updatedDatesWithoutUnlovedDate.firstOrNull(),
                                                newEndPeriod = updatedDatesWithoutUnlovedDate.lastOrNull(),
                                                isLoved = false,
                                                predictedDates = predictedDates,
                                                periodDates = updatedDatesWithoutUnlovedDate
                                            )
                                        }.addOnFailureListener { e ->
                                            Log.e("MainPage", "Gagal menghentikan periode berjalan: ${e.message}")
                                        }
                                    }
                                    val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                    val dayItem = DayItem(today, "", isToday = true, fullDate = today)
                                    onDateClick(dayItem)
                                }
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

        // Log debug untuk memantau klik
        Log.d("MainPage", "Clicked Date: $normalizedClickedDate")
        Log.d("MainPage", "Period Dates: ${periodDates.map { normalizeDate(it) }}")
        Log.d("MainPage", "Predicted Dates: ${predictedDates.map { normalizeDate(it) }}")

        // Ambil semua period ID yang terurut berdasarkan periodStart
        val periodData = mutableListOf<Pair<String, List<Date>>>()
        dateToPeriodIdMap.keys.sorted().forEach { date ->
            val periodId = dateToPeriodIdMap[date]
            if (periodId != null) {
                val datesForId = dateToPeriodIdMap.filterValues { it == periodId }.keys.toList()
                periodData.add(Pair(periodId, datesForId.sorted()))
            }
        }
        var isPeriodIdFound = false
        // Ambil startDate dari Firebase user
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .collection("period")
            .orderBy("periodStart", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val periodData = mutableListOf<Pair<String, List<Date>>>()

                // Proses setiap dokumen di koleksi "period"
                querySnapshot.documents.forEach { document ->
                    val periodId = document.id
                    val periodDatesLong = document.get("periodDates") as? List<Long> ?: return@forEach
                    val periodDates = periodDatesLong.map { normalizeDate(Date(it)) }
                    periodData.add(Pair(periodId, periodDates))
                    isPeriodIdFound = true
                }

                // Lakukan pengecekan periodData
                handlePeriodData(normalizedClickedDate, periodData, currentUserId)
                if (!isPeriodIdFound) {
                    // Jika tidak ditemukan di periodData, cek prediksi atau startDate
                    handlePredictionOrStartDate(normalizedClickedDate, currentUserId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Error fetching period data: ${e.message}")
            }

// Cari period ID terbaru (yang memiliki periodDates terakhir)
        val latestPeriod = periodData.lastOrNull()
        val latestPeriodStart = latestPeriod?.second?.firstOrNull()
        val latestPeriodEnd = latestPeriod?.second?.lastOrNull()

// Tanggal prediksi
        val normalizedPredictedDates = predictedDates.map { normalizeDate(it) }

// Cek apakah tanggal diklik ada di antara dua period ID
        var isPastCycle = false
        for (i in 1 until periodData.size) {
            val currentPeriod = periodData[i]
            val previousPeriod = periodData[i - 1]

            val currentStart = currentPeriod.second.first()
            val previousEnd = previousPeriod.second.last()

            if (normalizedClickedDate.after(previousEnd) && normalizedClickedDate.before(currentStart)) {
                // Tanggal berada di antara dua period ID
                val dayNumber = ((normalizedClickedDate.time - previousEnd.time) / (1000 * 60 * 60 * 24)).toInt() +
                        previousPeriod.second.size
                tvPeriodStatusText.text = "Past Cycle: Day $dayNumber"
                tvPeriodText.text = "Past Cycle"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                btnLove.setImageResource(R.drawable.heartgif)
                isPastCycle = true
                break
            }
        }

// Cek apakah tanggal sebelum period pertama
        if (!isPastCycle && periodData.isNotEmpty()) {
            val firstPeriodStart = periodData.first().second.first()
            if (normalizedClickedDate.before(firstPeriodStart)) {
                val daysToFirstPeriod = ((firstPeriodStart.time - normalizedClickedDate.time) / (1000 * 60 * 60 * 24)).toInt()
                tvPeriodStatusText.text = "Past Cycle: $daysToFirstPeriod Days Before"
                tvPeriodText.text = "Past Cycle"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                btnLove.setImageResource(R.drawable.heartgif)
                isPastCycle = true
            }
        }

        if (isPastCycle) return

// Cek apakah tanggal setelah period ID terbaru menuju prediction date
        if (latestPeriodEnd != null && normalizedClickedDate.after(latestPeriodEnd)) {
            if (normalizedPredictedDates.contains(normalizedClickedDate)) {
                // Tanggal adalah bagian dari predicted dates
                val dayNumber = normalizedPredictedDates.indexOf(normalizedClickedDate) + 1
                tvPeriodStatusText.text = "Day $dayNumber"
                tvPeriodText.text = "Prediction:"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                btnLove.setImageResource(R.drawable.heartgif)
            } else if (normalizedClickedDate.before(normalizedPredictedDates.first())) {
                // Tanggal menuju prediction date
                val daysToPrediction = ((normalizedPredictedDates.first().time - normalizedClickedDate.time) /
                        (1000 * 60 * 60 * 24)).toInt()
                tvPeriodStatusText.text = "$daysToPrediction Days"
                tvPeriodText.text = "Prediction in"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                btnLove.setImageResource(R.drawable.heartgif)
            } else {
                // Setelah prediction date
                tvPeriodStatusText.text = "Period Not Started"
                tvPeriodText.text = "Not Started"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
                btnLove.setImageResource(R.drawable.heartgif)
            }
            return
        }

// Jika tidak ada kecocokan
        tvPeriodStatusText.text = "Not Started"
        tvPeriodText.text = "Period"
        tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
        tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
        btnLove.setImageResource(R.drawable.heartgif)
    }

    private fun handlePeriodData(
        normalizedClickedDate: Date,
        periodData: List<Pair<String, List<Date>>>,
        currentUserId: String
    ) {

        for ((periodId, periodDates) in periodData) {
            if (periodDates.contains(normalizedClickedDate)) {
                // Tanggal ada dalam periodDates -> Hitung "Period: Day N"
                val dayNumber = periodDates.indexOf(normalizedClickedDate) + 1
                tvPeriodStatusText.text = "Day $dayNumber"
                tvPeriodText.text = "Period:"
                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
                tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
                btnLove.setImageResource(R.drawable.redheart)
                break
            }
        }
    }

    private fun handlePredictionOrStartDate(normalizedClickedDate: Date, currentUserId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val startDateString = document.getString("startDate")
                if (startDateString != null) {
                    val dateFormatFirebase = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startDate = dateFormatFirebase.parse(startDateString)?.let { normalizeDate(it) }

                    if (!isPeriodIdFound) {
                        if (startDate != null && normalizedClickedDate.before(startDate)) {
                            // Tanggal sebelum startDate
                            val daysToStartDate = ((startDate.time - normalizedClickedDate.time) / (1000 * 60 * 60 * 24)).toInt()
                            tvPeriodStatusText.text = "Period in $daysToStartDate Days"
                            tvPeriodText.text = "Prediction"
                            tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                            tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                            btnLove.setImageResource(R.drawable.heartgif)
                        } else if (startDate != null && normalizedClickedDate.after(startDate)) {
                            // Tanggal setelah startDate menuju prediction date
                            if (predictedDates.isNotEmpty() && normalizedClickedDate.before(predictedDates.first())) {
                                val daysToPrediction = ((predictedDates.first().time - normalizedClickedDate.time) /
                                        (1000 * 60 * 60 * 24)).toInt()
                                tvPeriodStatusText.text = "Prediction in $daysToPrediction Days"
                                tvPeriodText.text = "Prediction"
                                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color3))
                                tvPeriodText.setTextColor(requireContext().getColor(R.color.color3))
                                btnLove.setImageResource(R.drawable.heartgif)
                            } else if (predictedDates.isNotEmpty() && normalizedClickedDate.after(predictedDates.last())) {
                                // Setelah prediction date
                                tvPeriodStatusText.text = "Period Not Started"
                                tvPeriodText.text = "Not Started"
                                tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.white))
                                tvPeriodText.setTextColor(requireContext().getColor(R.color.white))
                                btnLove.setImageResource(R.drawable.heartgif)
                            }
                        }
                        }
                } else {
                    // Jika tidak ada startDate di Firebase
                    tvPeriodStatusText.text = "Start Date Not Set"
                    tvPeriodText.text = "Error"
                    tvPeriodStatusText.setTextColor(requireContext().getColor(R.color.color4))
                    tvPeriodText.setTextColor(requireContext().getColor(R.color.color4))
                    btnLove.setImageResource(R.drawable.heartgif)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Error fetching user startDate: ${e.message}")
            }
    }


    private fun loadPeriodData() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).collection("period")
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { document ->
                    val periodId = document.id
                    val periodDatesLong = document.get("periodDates") as? List<Long> ?: listOf()

                    // Konversi Long ke Date dan simpan dalam map
                    periodDatesLong.forEach { dateLong ->
                        val date = normalizeDate(Date(dateLong))
                        dateToPeriodIdMap[date] = periodId
                    }
                }

                Log.d("MainPage", "dateToPeriodIdMap: $dateToPeriodIdMap")
            }
            .addOnFailureListener { exception ->
                Log.e("MainPage", "Failed to load period data: ${exception.message}")
            }
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
        loadLoveStatus()
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

}
