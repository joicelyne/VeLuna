package com.example.veluna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    private lateinit var btnLove : ImageView
    private lateinit var tvPeriodStatusText: TextView
    private lateinit var tvPeriodText : TextView

    private var isLoved = false // status awal love butt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_page, container, false)

        // Navigation
        view.findViewById<ImageView>(R.id.editDatePeriodIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_datesEditPeriod)
        }

        view.findViewById<ImageView>(R.id.moodEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_moodNotes)
        }

        view.findViewById<ImageView>(R.id.history_cycle_button).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_cycleHistory)
        }

        view.findViewById<ImageView>(R.id.history_cycle_button2).setOnClickListener {
            findNavController().navigate(R.id.action_MainPage_to_cycleHistory)
        }

        // Heartbeat Animation
        btnLove = view.findViewById(R.id.imgHeart)
        val heartbeatAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.heartbeat)
        btnLove.startAnimation(heartbeatAnimation)



        // Initialize UI elements
        tvName = view.findViewById(R.id.tvName)
        cycleName = view.findViewById(R.id.cycleName)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvPeriodStatusText = view.findViewById(R.id.tvPeriodStatusText)
        tvPeriodText = view.findViewById(R.id.tvPeriodText)

        // Set bulan dan tahun
        setMonthYear()

        // Load user data
        loadUserData()
        loadLoveStatus()

        //Handle love button click
        btnLove.setOnClickListener{
            isLoved = !isLoved
            updateLoveStatus(isLoved)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showBottomNavigation()
    }

    private fun setMonthYear() {
        // Format bulan dan tahun
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYear = dateFormat.format(calendar.time)

        // Set teks ke tvMonthYear
        tvMonthYear.text = monthYear
    }

    private fun loadUserData() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Log.e("MainPage", "User ID tidak ditemukan.")
            return
        }

        // Fetch user data from Firestore
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val cycleNameText = "$name's Cycle"

                    // Update UI elements
                    tvName.text = name
                    cycleName.text = cycleNameText
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
            .document("loveStatus")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    isLoved = document.getBoolean("isStart") ?: false
                    btnLove.setImageResource(if (isLoved) R.drawable.redheart else R.drawable.heartgif)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal memuat love status: ${e.message}")
            }
    }

    private fun updateLoveStatus(isLoved: Boolean) {
        val timestamp = Timestamp.now()
        val periodData = mapOf(
            "isStart" to isLoved,
            "periodStart" to timestamp
        )

        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Log.e("MainPage", "User ID tidak ditemukan.")
            return
        }

        // Simpan ke Firebase
        db.collection("users")
            .document(currentUserId)
            .collection("period")
            .document("loveStatus")
            .set(periodData)
            .addOnSuccessListener {
                // Ubah warna icon love & tulisan period started
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
            }
            .addOnFailureListener { e ->
                Log.e("MainPage", "Gagal menyimpan love status: ${e.message}")
            }
    }
}
