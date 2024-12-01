package com.example.veluna

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    // Variabel UI untuk layout Profile
    private lateinit var profileName: TextView
    private lateinit var profileWeight: TextView
    private lateinit var profileHeight: TextView
    private lateinit var profileBMI: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnLogOut: Button

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    private var weight: Double = 47.0
    private var height: Double = 155.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout awal (fragment_profile.xml)
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi elemen UI di layout profil
        profileName = view.findViewById(R.id.profileName)
        profileWeight = view.findViewById(R.id.profileWeight)
        profileHeight = view.findViewById(R.id.profileHeight)
        profileBMI = view.findViewById(R.id.profileBMI)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnLogOut = view.findViewById(R.id.btnLogOut)

        // Tombol Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Ikon Edit Profile
        view.findViewById<ImageView>(R.id.imgEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Tombol Logout
        btnLogOut.setOnClickListener {
            logoutUser()
        }

        // Muat data pengguna dari Firestore
        loadUserData()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // Reload user data whenever the fragment becomes active
    }

    private fun loadUserData() {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch user data from Firestore
        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Nama tidak tersedia"
                    val photoUrl = document.getString("photoUrl") ?: ""

                    // Debug Log
                    Log.d("ProfileFragment", "Nama: $name, Photo URL: $photoUrl")

                    // Update UI
                    profileName.text = name
                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.person) // Placeholder saat memuat
                            .error(R.drawable.person) // Fallback jika gagal memuat
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.person)
                    }

                    // Optionally update BMI if weight/height are part of Firestore data
                    document.getDouble("weight")?.let { weight = it }
                    document.getDouble("height")?.let { height = it }
                    updateBMI()
                } else {
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Gagal memuat data: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBMI() {
        val heightInMeters = height / 100
        val bmi = weight / (heightInMeters * heightInMeters)
        profileWeight.text = "Weight: $weight kg"
        profileHeight.text = "Height: $height cm"
        profileBMI.text = "BMI: %.2f".format(bmi)
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // Logout dari Firebase Authentication
        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show()

        // Navigasi ke WelcomePageFragment
        findNavController().navigate(R.id.action_profileFragment_to_WelcomePageFragment)
    }
}
