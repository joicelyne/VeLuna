package com.example.veluna

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class ProfileFragment : Fragment() {

    // Variabel UI untuk layout Profile
    private lateinit var profileName: TextView
    private lateinit var profileWeight: TextView
    private lateinit var profileHeight: TextView
    private lateinit var profileBMI: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnLogOut: Button
    private lateinit var btnEditWeightHeight: Button

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    // ViewModel untuk sinkronisasi data
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout awal (fragment_profile.xml)
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi ViewModel
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        // Inisialisasi elemen UI di layout profil
        profileName = view.findViewById(R.id.profileName)
        profileWeight = view.findViewById(R.id.profileWeight)
        profileHeight = view.findViewById(R.id.profileHeight)
        profileBMI = view.findViewById(R.id.profileBMI)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnLogOut = view.findViewById(R.id.btnLogOut)
        btnEditWeightHeight = view.findViewById(R.id.btnEditWeightHeight)

        // Observasi perubahan data di ViewModel
        observeUserData()

        // Tombol Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Ikon Edit Profile
        view.findViewById<ImageView>(R.id.imgEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Edit Weight & Height Navigation
        btnEditWeightHeight.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editWeightHeightFragment)
        }

        // Logout Button
        btnLogOut.setOnClickListener {
            logoutUser()
        }

        // Muat data pengguna dari Firestore (jika pertama kali dibuka)
        loadUserData()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Tidak perlu memuat ulang karena ViewModel sudah memegang data yang disinkronkan
    }

    private fun observeUserData() {
        userViewModel.name.observe(viewLifecycleOwner) { name ->
            profileName.text = name
        }

        userViewModel.photoUrl.observe(viewLifecycleOwner) { photoUrl ->
            if (photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.person) // Placeholder saat memuat
                    .error(R.drawable.person) // Fallback jika gagal memuat
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.person)
            }
        }
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
                    val weight = document.get("weight")?.toString()?.toDoubleOrNull() ?: 0.0
                    val height = document.get("height")?.toString()?.toDoubleOrNull() ?: 0.0

                    // Update ViewModel untuk sinkronisasi name dan photoUrl
                    userViewModel.updateName(name)
                    userViewModel.updatePhotoUrl(photoUrl)

                    // Update UI langsung untuk weight dan height (tanpa sinkronisasi)
                    profileWeight.text = "$weight kg"
                    profileHeight.text = "$height cm"
                    updateBMI(weight, height)
                } else {
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Gagal memuat data: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBMI(weight: Double, height: Double) {
        if (height > 0) {
            val heightInMeters = height / 100
            val bmi = weight / (heightInMeters * heightInMeters)
            profileBMI.text = "BMI: %.2f".format(bmi)
        } else {
            profileBMI.text = "BMI: N/A"
        }
    }

    private fun logoutUser() {
        // Logout dari Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Hapus data lokal (opsional)
        clearLocalData()

        // Hapus token Firebase Messaging (opsional)
        removeFirebaseMessagingToken()

        // Tampilkan pesan sukses
        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show()

        // Navigasi ke WelcomePageFragment
        findNavController().navigate(R.id.action_profileFragment_to_WelcomePageFragment)
    }

    private fun clearLocalData() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // Menghapus semua data lokal
    }

    private fun removeFirebaseMessagingToken() {
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Logout", "Firebase Messaging Token berhasil dihapus")
                } else {
                    Log.e("Logout", "Gagal menghapus Firebase Messaging Token: ${task.exception?.message}")
                }
            }
    }




}
