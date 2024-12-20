package com.example.veluna

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

        // Setup RecyclerView for weekly calendar
        setupRecyclerView()

        // Setup gesture detection
        setupGestureDetection()

        // Navigation
        view.findViewById<ImageView>(R.id.editDatePeriodIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_datesEditPeriod)
        }

        view.findViewById<ImageView>(R.id.history_cycle_button).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_cycleHistory)
        }

        // Heartbeat Animation
        btnLove = view.findViewById(R.id.imgHeart)
        val heartbeatAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.heartbeat)
        btnLove.startAnimation(heartbeatAnimation)

        // Handle love button click
        btnLove.setOnClickListener {
            isLoved = !isLoved
            updateLoveStatus(isLoved)
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
                    updatePeriodDates(periodLength)
                    updatePredictedDates(cycleLength, periodLength)
                    tvName.text = name
                    cycleName.text = "$name's Cycle"
                    insightName.text = "$name's Insight"
                    prevCycleLenText.text = "$cycleLength Days"
                    prevPeriodLenText.text = "$periodLength Days"

                    // Simpan periodLength untuk digunakan dalam logika lainnya
                    updatePeriodDates(periodLength)
                } else {
                    Log.e("MainPage", "Dokumen pengguna tidak ditemukan.")
                    updatePeriodDates(5) // Gunakan default jika dokumen tidak ditemukan
                    updatePredictedDates(28, 5)
                    prevCycleLenText.text = "-" // Default jika tidak ada data
                    prevPeriodLenText.text = "-" // Default jika tidak ada data
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainPage", "Gagal mengambil data: ${exception.message}")
                updatePeriodDates(5) // Gunakan default jika gagal mengambil data
                updatePredictedDates(28, 5)
            }
    }

    private fun loadLoveStatus() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Log.e("MainPage", "User ID tidak ditemukan.")
            return
        }

        val userDocRef = db.collection("users").document(currentUserId)

        // Ambil data period terbaru
        userDocRef.collection("period")
            .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    // Ambil startDate dari period terbaru
                    val document = querySnapshot.documents.first()
                    val periodDatesLong = document.get("periodDates") as? List<Long> ?: listOf()
                    val periodDates = periodDatesLong.map { Date(it) }
                    this.periodDates = periodDates

                    val startDate = periodDates.firstOrNull() ?: Date() // StartDate dari period terbaru
                    val periodLength = document.getLong("periodLength")?.toInt() ?: 5
                    val cycleLength = document.getLong("cycleLength")?.toInt() ?: 28

                    val predictedDates = getPredictedPeriodDates(startDate, cycleLength, periodLength)
                    updateCalendarUI(periodDates, predictedDates)
                } else {
                    // Jika collection 'period' kosong, ambil startDate dari root user document
                    userDocRef.get()
                        .addOnSuccessListener { userDocument ->
                            val startDateString = userDocument.getString("startDate") ?: ""
                            val startDate = parseDate(startDateString) ?: Date() // Default ke hari ini
                            val periodLength = userDocument.getLong("periodLength")?.toInt() ?: 5
                            val cycleLength = userDocument.getLong("cycleLength")?.toInt() ?: 28

                            val predictedDates = getPredictedPeriodDates(startDate, cycleLength, periodLength)
                            this.predictedDates = predictedDates
                            updateCalendarUI(periodDates, predictedDates)

                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal mengambil startDate dari user document: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal memuat period: ${e.message}")
            }
    }

    // Fungsi untuk memperbarui UI kalender
    private fun updateCalendarUI(periodDates: List<Date>, predictedDates: List<Date>) {
        val currentDate = Date()
        isLoved = periodDates.any { it >= currentDate } // Tentukan status isLoved

        // Update UI berdasarkan isLoved
        if (isLoved) {
            btnLove.setImageResource(R.drawable.redheart)
            tvPeriodStatusText.text = "Started"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
            tvPeriodText.setTextColor(resources.getColor(R.color.white))
        } else {
            btnLove.setImageResource(R.drawable.heartgif)
            tvPeriodStatusText.text = "Not Started"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
        }

        // Perbarui RecyclerView
        adapter.updateDays(
            newDays = getWeeklyDates(),
            newStartPeriod = periodDates.firstOrNull(),
            newEndPeriod = periodDates.lastOrNull(),
            isLoved = isLoved,
            predictedDates = predictedDates
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

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { userDocument ->
                // Ambil nilai periodLength dan cycleLength dari Firebase atau gunakan default
                val periodLength = userDocument.getLong("periodLength")?.toInt() ?: 5
                val cycleLength = userDocument.getLong("cycleLength")?.toInt() ?: 28

                if (isLoved) {
                    // Cek jika tidak ada periode yang sedang berjalan
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

                                val startPeriod = timestamp.toDate()
                                val periodDates = generateDatesBetween(startPeriod, periodLength)

                                val periodData = mapOf(
                                    "isStart" to true,
                                    "periodStart" to startPeriod,
                                    "periodDates" to periodDates.map { it.time } // Simpan sebagai Long
                                )

                                // Simpan periode baru
                                db.collection("users")
                                    .document(currentUserId)
                                    .collection("period")
                                    .document(newPeriodId)
                                    .set(periodData)
                                    .addOnSuccessListener {
                                        Log.d("Debug", "Period Baru Disimpan: $periodData")

                                        // Ambil startDate terbaru dan hitung prediksi tanggal
                                        val predictedDates = getPredictedPeriodDates(startPeriod, cycleLength, periodLength)

                                        // Update UI
                                        updateCalendarUI(periodDates, predictedDates)

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
                    // Hentikan periode yang sedang berjalan
                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .whereEqualTo("isStart", true)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            querySnapshot.documents.forEach { document ->
                                val periodDatesLong = document.get("periodDates") as? List<Long> ?: return@forEach
                                val periodDates = periodDatesLong.map { Date(it) } // Konversi Long ke Date

                                val updatedDates = periodDates.filter { it <= Date() }

                                document.reference.update(
                                    mapOf(
                                        "isStart" to false,
                                        "periodDates" to updatedDates.map { it.time }
                                    )
                                ).addOnSuccessListener {
                                    Log.d("Debug", "Periode Dihentikan")

                                    // Update UI setelah periode dihentikan
                                    val predictedDates = getPredictedPeriodDates(updatedDates.lastOrNull() ?: Date(), cycleLength, periodLength)
                                    updateCalendarUI(updatedDates, predictedDates)

                                    btnLove.setImageResource(R.drawable.heartgif)
                                    tvPeriodStatusText.text = "Not Started"
                                    tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                                    tvPeriodText.setTextColor(resources.getColor(R.color.color4))
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

    private fun setupRecyclerView() {
        adapter = DayAdapter(
            days = getWeeklyDates(),
            isLoved = isLoved, // Pass isLoved to the adapter
            onMoodEditClick = { dayItem ->
                // Callback untuk edit
                findNavController().navigate(R.id.action_MainPage_to_moodNotes)
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

        // Perbarui bulan dan tahun saat pertama kali RecyclerView dimuat
        updateMonthYear()
    }

    private fun getWeeklyDates(): List<DayItem> {
        val today = Calendar.getInstance()
        val weekDates = mutableListOf<DayItem>()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // Start week on Sunday

        val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Format untuk fullDate

        for (i in 0 until 7) { // Generate 7 days
            val date = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)[0].toString()
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            val fullDate = fullDateFormat.format(calendar.time) // Generate fullDate

            weekDates.add(DayItem(date, dayName, isToday, fullDate)) // Tambahkan fullDate di sini
            calendar.add(Calendar.DATE, 1)
        }
        calendar.add(Calendar.DATE, -7) // Reset calendar to start of week
        return weekDates
    }

    private fun setupGestureDetection() {
        gestureDetector = GestureDetectorCompat(requireContext(), object :
            GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - (e1?.x ?: 0f)
                val diffY = e2.y - (e1?.y ?: 0f)

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Geser ke kanan (minggu sebelumnya)
                            recyclerViewWeek.animate().translationX(recyclerViewWeek.width.toFloat())
                                .setDuration(300).withEndAction {
                                    recyclerViewWeek.translationX = -recyclerViewWeek.width.toFloat()
                                    calendar.add(Calendar.DATE, -7)
                                    adapter.updateDays(
                                        newDays = getWeeklyDates(),
                                        newStartPeriod = periodDates.firstOrNull(),
                                        newEndPeriod = periodDates.lastOrNull(),
                                        isLoved = isLoved,
                                        predictedDates = predictedDates
                                    )
                                    recyclerViewWeek.animate().translationX(0f).setDuration(300).start()
                                    updateMonthYear()
                                }.start()
                        } else {
                            // Geser ke kiri (minggu berikutnya)
                            recyclerViewWeek.animate().translationX(-recyclerViewWeek.width.toFloat())
                                .setDuration(300).withEndAction {
                                    recyclerViewWeek.translationX = recyclerViewWeek.width.toFloat()
                                    calendar.add(Calendar.DATE, 7)
                                    adapter.updateDays(
                                        newDays = getWeeklyDates(),
                                        newStartPeriod = periodDates.firstOrNull(),
                                        newEndPeriod = periodDates.lastOrNull(),
                                        isLoved = isLoved,
                                        predictedDates = predictedDates
                                    )
                                    recyclerViewWeek.animate().translationX(0f).setDuration(300).start()
                                    updateMonthYear()
                                }.start()
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

    private fun updateMonthYear() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYear = dateFormat.format(calendar.time)
        tvMonthYear.text = monthYear
    }

    private fun updatePeriodDates(periodLength: Int) {
        if (periodDates.isEmpty()) return

        val startPeriod = periodDates.first()

        // Generate dates langsung berdasarkan startPeriod dan periodLength
        val updatedPeriodDates = generateDatesBetween(startPeriod, periodLength)
        this.periodDates = updatedPeriodDates

        // Update adapter dengan tanggal baru
        adapter.updateDays(
            newDays = getWeeklyDates(),
            newStartPeriod = updatedPeriodDates.firstOrNull(),
            newEndPeriod = updatedPeriodDates.lastOrNull(),
            isLoved = isLoved,
            predictedDates = predictedDates
        )
    }

    private fun updatePredictedDates(cycleLength: Int, periodLength: Int) {
        val startDate = periodDates.firstOrNull() ?: Date()

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
            predictedDates = predictedDates
        )
    }

}
