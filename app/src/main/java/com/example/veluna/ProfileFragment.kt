package com.example.veluna

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
    private lateinit var imgProfile: ImageView
    private lateinit var btnLogOut: Button

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout awal (fragment_profile.xml)
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi elemen UI di layout profil
        profileName = view.findViewById(R.id.profileName)
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
        // Pastikan data terbaru ditampilkan saat fragment kembali terlihat
        loadUserData()
    }

    private fun loadUserData() {
        if (userId.isNullOrEmpty()) {
            Log.e("ProfileFragment", "User ID is null or empty.")
            Toast.makeText(requireContext(), "Anda belum login. Silakan login terlebih dahulu.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_WelcomePageFragment)
            return
        }

        // Fetch user data dari Firestore
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
                } else {
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Gagal memuat data: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // Logout dari Firebase Authentication
        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show()

        // Navigasi ke WelcomePageFragment
        findNavController().navigate(R.id.action_profileFragment_to_WelcomePageFragment)
    }
}
