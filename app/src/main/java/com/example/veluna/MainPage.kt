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
    private lateinit var tvMonthYear: TextView
    private lateinit var btnLove: ImageView
    private lateinit var tvPeriodStatusText: TextView
    private lateinit var tvPeriodText: TextView
    private lateinit var recyclerViewWeek: RecyclerView
    private lateinit var adapter: DayAdapter

    // Calendar instance to track the current week
    private val calendar = Calendar.getInstance()
    private var isLoved = false // Status awal love button
    private lateinit var gestureDetector: GestureDetectorCompat // Gesture detector

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
            .orderBy("periodStart", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan berdasarkan periodStart terbaru
            .limit(1) // Ambil hanya 1 data terbaru
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val periodStart = document.getTimestamp("periodStart")?.toDate() ?: return@addOnSuccessListener
                    val maxDays = document.getLong("maxDays")?.toInt() ?: 5
                    val currentDate = Calendar.getInstance().time

                    // Hitung selisih hari antara periodStart dan hari ini
                    val daysDiff = ((currentDate.time - periodStart.time) / (1000 * 60 * 60 * 24)).toInt()

                    if (daysDiff <= maxDays) {
                        // Periode masih aktif
                        isLoved = true
                        btnLove.setImageResource(R.drawable.redheart)
                        tvPeriodStatusText.text = "Started"
                        tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
                        tvPeriodText.setTextColor(resources.getColor(R.color.white))
                    } else {
                        // Periode sudah selesai, update status di Firebase
                        document.reference.update("isStart", false)
                        isLoved = false
                        btnLove.setImageResource(R.drawable.heartgif)
                        tvPeriodStatusText.text = "Not Started"
                        tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
                        tvPeriodText.setTextColor(resources.getColor(R.color.color4))
                    }
                } else {
                    // Tidak ada periode aktif
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
            // Cek apakah ada periode aktif
            db.collection("users")
                .document(currentUserId)
                .collection("period")
                .whereEqualTo("isStart", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null && querySnapshot.isEmpty) {
                        // Tidak ada periode aktif, buat period_id baru
                        val newPeriodId = db.collection("users")
                            .document(currentUserId)
                            .collection("period")
                            .document().id

                        val periodData = mapOf(
                            "isStart" to true,
                            "periodStart" to timestamp,
                            "maxDays" to 5 // Default 5 hari
                        )

                        db.collection("users")

                            .document(currentUserId)
                            .collection("period")
                            .document(newPeriodId)
                            .set(periodData)
                            .addOnSuccessListener {
                                // Update UI saat LoveStatus diaktifkan
                                btnLove.setImageResource(R.drawable.redheart)
                                tvPeriodStatusText.text = "Started"
                                tvPeriodStatusText.setTextColor(resources.getColor(R.color.white))
                                tvPeriodText.setTextColor(resources.getColor(R.color.white))
                                trackPeriodDays(newPeriodId, 1) // Mulai mencatat dari PeriodDay1
                            }
                            .addOnFailureListener { e ->
                                Log.e("MainPage", "Gagal menyimpan period_id: ${e.message}")
                            }
                    } else {
                        Log.d("MainPage", "Periode aktif sudah ada, tidak membuat ID baru.")
                    }
                }
        } else {
            // Jika LoveStatus dinonaktifkan sebelum 5 hari
            db.collection("users")
                .document(currentUserId)
                .collection("period")
                .whereEqualTo("isStart", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        val periodStart = document.getTimestamp("periodStart")?.toDate() ?: return@forEach
                        val currentDate = Calendar.getInstance().time

                        // Hitung durasi hari saat ini
                        val daysDiff = ((currentDate.time - periodStart.time) / (1000 * 60 * 60 * 24)).toInt() + 1

                        // Perbarui durasi sebenarnya
                        document.reference.update(
                            mapOf(
                                "isStart" to false,
                                "maxDays" to daysDiff
                            )
                        )
                    }
                }

            // Update UI
            btnLove.setImageResource(R.drawable.heartgif)
            tvPeriodStatusText.text = "Not Started"
            tvPeriodStatusText.setTextColor(resources.getColor(R.color.color4))
            tvPeriodText.setTextColor(resources.getColor(R.color.color4))
        }
    }

    private fun trackPeriodDays(periodId: String, dayNumber: Int) {
        if (!isLoved) return // Hentikan jika LoveStatus dimatikan secara manual

        if (dayNumber > 5) {
            // Auto-hentikan setelah 5 hari
            updateLoveStatus(false)
            return
        }

        val currentUserId = userId ?: return
        val dayDate = Calendar.getInstance().apply {
            add(Calendar.DATE, dayNumber - 1)
        }.time

        db.collection("users")
            .document(currentUserId)
            .collection("period")
            .document(periodId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && document.getBoolean("isStart") == true) {
                    val periodDayData = mapOf(
                        "PeriodDay$dayNumber" to SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(dayDate)
                    )

                    db.collection("users")
                        .document(currentUserId)
                        .collection("period")
                        .document(periodId)
                        .update(periodDayData)
                        .addOnSuccessListener {
                            Log.d("MainPage", "PeriodDay$dayNumber berhasil disimpan.")
                            // Lanjutkan ke hari berikutnya jika LoveStatus masih aktif
                            recyclerViewWeek.postDelayed({
                                trackPeriodDays(periodId, dayNumber + 1)
                            }, 24 * 60 * 60 * 1000) // Delay 24 jam
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainPage", "Gagal mencatat PeriodDay$dayNumber: ${e.message}")
                        }
                } else {
                    Log.d("MainPage", "LoveStatus sudah tidak aktif. Menghentikan pencatatan.")
                }
            }
    }


    private fun setupRecyclerView() {
        adapter = DayAdapter(getWeeklyDates()) { dayItem ->
            if (dayItem.isToday) {
                findNavController().navigate(R.id.action_MainPage_to_moodNotes)
            }
        }

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

        for (i in 0 until 7) { // Generate 7 days
            val date = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)[0].toString()
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            weekDates.add(DayItem(date, dayName, isToday))
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
                                    adapter.updateDays(getWeeklyDates())
                                    recyclerViewWeek.animate().translationX(0f).setDuration(300).start()
                                    updateMonthYear()
                                }.start()
                        } else {
                            // Geser ke kiri (minggu berikutnya)
                            recyclerViewWeek.animate().translationX(-recyclerViewWeek.width.toFloat())
                                .setDuration(300).withEndAction {
                                    recyclerViewWeek.translationX = recyclerViewWeek.width.toFloat()
                                    calendar.add(Calendar.DATE, 7)
                                    adapter.updateDays(getWeeklyDates())
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

}
