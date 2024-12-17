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

    // Calendar instance to track the current week
    private val calendar = Calendar.getInstance()
    private var isLoved = false // Status awal love button
    private lateinit var gestureDetector: GestureDetectorCompat // Gesture detector
    private var periodDates: List<Date> = listOf()

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
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvPeriodStatusText = view.findViewById(R.id.tvPeriodStatusText)
        tvPeriodText = view.findViewById(R.id.tvPeriodText)
        recyclerViewWeek = view.findViewById(R.id.recyclerViewWeek)

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
                    tvName.text = name
                    cycleName.text = "$name's Cycle"
                } else {
                    Log.e("MainPage", "Dokumen pengguna tidak ditemukan.")
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

        db.collection("users")
            .document(currentUserId)
            .collection("period")
            .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val periodDatesLong = document.get("periodDates") as? List<Long> ?: listOf()
                    val periodDates = periodDatesLong.map { Date(it) } // Konversi Long ke Date
                    this.periodDates = periodDates // Simpan sebagai List<Date>

                    val currentDate = Date()
                    isLoved = periodDates.any { it >= currentDate } // Tentukan status isLoved

                    // Tentukan rentang periode
                    val startPeriod = periodDates.firstOrNull()
                    val endPeriod = periodDates.lastOrNull()

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

                    // Perbarui tampilan kalender
                    adapter.updateDays(
                        newDays = getWeeklyDates(),
                        newStartPeriod = periodDates.firstOrNull(),
                        newEndPeriod = periodDates.lastOrNull(),
                        isLoved = isLoved
                    )
                } else {
                    isLoved = false
                    btnLove.setImageResource(R.drawable.heartgif)
                    tvPeriodStatusText.text = "Not Started"
                    tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                    tvPeriodText.setTextColor(resources.getColor(R.color.color4))
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal memuat love status: ${e.message}")
            }
    }

    private fun updateLoveStatus(isLoved: Boolean) {
        val currentUserId = userId ?: return
        val timestamp = Timestamp.now()

        if (isLoved) {
            db.collection("users")
                .document(currentUserId)
                .collection("period")
                .whereEqualTo("isStart", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null && querySnapshot.isEmpty) {
                        val newPeriodId = db.collection("users")
                            .document(currentUserId)
                            .collection("period")
                            .document().id

                        // Generate tanggal dari start hingga end
                        val startPeriod = timestamp.toDate()
                        val endPeriod = Calendar.getInstance().apply {
                            time = startPeriod
                            add(Calendar.DATE, 4) // 5 hari default
                        }.time
                        val periodDates = generateDatesBetween(startPeriod, endPeriod)

                        val periodData = mapOf(
                            "isStart" to true,
                            "periodStart" to timestamp,
                            "periodDates" to periodDates.map { it.time } // Simpan sebagai Long
                        )

                        db.collection("users")
                            .document(currentUserId)
                            .collection("period")
                            .document(newPeriodId)
                            .set(periodData)
                            .addOnSuccessListener {
                                btnLove.setImageResource(R.drawable.redheart)
                                tvPeriodStatusText.text = "Started"
                                tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
                                tvPeriodText.setTextColor(resources.getColor(R.color.white))

                                adapter.updateDays(
                                    newDays = getWeeklyDates(),
                                    newStartPeriod = periodDates.firstOrNull(),
                                    newEndPeriod = periodDates.lastOrNull(),
                                    isLoved = isLoved
                                )
                            }
                    }
                }
        } else {
            db.collection("users")
                .document(currentUserId)
                .collection("period")
                .whereEqualTo("isStart", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        val periodDatesLong = document.get("periodDates") as? List<Long> ?: return@forEach
                        val periodDates = periodDatesLong.map { Date(it) } // Konversi Long ke Date

                        val updatedDates = periodDates.filter { it <= Date() } // Hanya ambil tanggal <= hari ini

                        document.reference.update(
                            mapOf(
                                "isStart" to false,
                                "periodDates" to updatedDates.map { it.time } // Simpan kembali sebagai Long
                            )
                        )

                        adapter.updateDays(
                            newDays = getWeeklyDates(),
                            newStartPeriod = updatedDates.firstOrNull(),
                            newEndPeriod = updatedDates.lastOrNull(),
                            isLoved = isLoved
                        )
                    }
                }

            btnLove.setImageResource(R.drawable.heartgif)
            tvPeriodStatusText.text = "Not Started"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
        }
    }

    private fun generateDatesBetween(startDate: Date, endDate: Date): List<Date> {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance().apply { time = startDate }

        while (calendar.time <= endDate) {
            dates.add(calendar.time)
            calendar.add(Calendar.DATE, 1)
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
                                        isLoved = isLoved
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
                                        isLoved = isLoved
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

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
