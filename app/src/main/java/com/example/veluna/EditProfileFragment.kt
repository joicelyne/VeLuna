package com.example.veluna

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileFragment : Fragment() {

    // UI Elements
    private lateinit var etUsername: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etEmail: TextView
    private lateinit var etPassword: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var profileName: TextView // Username di bawah profile picture
    private lateinit var btnChangeProfilePicture: Button
    private lateinit var btnUpdateProfile: Button
    private lateinit var btnBackEditProfile: ImageView

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    // Current photo URL
    private var updatedPhotoUrl: String = ""

    // ViewModel
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        // Initialize UI elements
        etUsername = view.findViewById(R.id.etUsername)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        imgProfile = view.findViewById(R.id.imgProfile)
        profileName = view.findViewById(R.id.ProfileName) // Username di bawah profile picture
        btnChangeProfilePicture = view.findViewById(R.id.btnChangeProfilePicture)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        btnBackEditProfile = view.findViewById(R.id.back_button_edit_profile)

        // Load user data from Firestore
        loadUserData()

        // Button actions
        btnChangeProfilePicture.setOnClickListener {
            openCameraOrGallery()
        }
        btnUpdateProfile.setOnClickListener {
            reauthenticateAndUpdatePassword()
        }
        btnBackEditProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun loadUserData() {
        if (userId.isNullOrEmpty()) return

        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: "Email tidak tersedia"
        etEmail.text = email // Set email pengguna

        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""

                    etUsername.setText(name)
                    etPhoneNumber.setText(phoneNumber)
                    profileName.text = name // Tampilkan username di bawah profile picture

                    // Store the current photo URL for updates
                    updatedPhotoUrl = photoUrl

                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.person)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to load data: ${e.message}")
            }
    }

    private fun reauthenticateAndUpdatePassword() {
        val currentPasswordInput = EditText(requireContext())
        currentPasswordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reauthenticate")
            .setMessage("Masukkan password saat ini Anda:")
            .setView(currentPasswordInput)
            .setPositiveButton("Submit") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString().trim()
                if (currentPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newPassword = etPassword.text.toString().trim()
                val newName = etUsername.text.toString().trim()
                val newPhoneNumber = etPhoneNumber.text.toString().trim()

                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "Username tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Password baru tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val email = FirebaseAuth.getInstance().currentUser?.email
                if (email.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Gagal menemukan email!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener {
                        updateFirestoreData(newName, newPhoneNumber, newPassword)
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("EditProfileFragment", "Reautentikasi gagal: ${e.message}")
                        Toast.makeText(requireContext(), "Reautentikasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateFirestoreData(newName: String, newPhoneNumber: String, newPassword: String) {
        if (userId.isNullOrEmpty()) return

        // Perbarui data di Firestore
        val updates = mapOf(
            "name" to newName,
            "phoneNumber" to newPhoneNumber,
            "photoUrl" to updatedPhotoUrl
        )

        db.collection("users").document(userId!!)
            .update(updates)
            .addOnSuccessListener {
                // Perbarui password di Firebase Authentication
                FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        // Sinkronisasi ke ViewModel
                        userViewModel.updateName(newName)
                        userViewModel.updatePhotoUrl(updatedPhotoUrl)

                        Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_editprofileFragment_to_profileFragment)
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("EditProfileFragment", "Gagal memperbarui password: ${e.message}")
                        Toast.makeText(requireContext(), "Gagal memperbarui password.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Gagal memperbarui Firestore: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openCameraOrGallery() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Update Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera() // Pilih kamera
                    1 -> openGallery() // Pilih galeri
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, open the camera
            launchCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                launchCamera()
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        imgProfile.setImageBitmap(photo)
                        uploadPhotoToFirebase(photo)
                    } else {
                        Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        Glide.with(this)
                            .load(imageUri)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(imgProfile)

                        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                        uploadPhotoToFirebase(bitmap)
                    } else {
                        Toast.makeText(requireContext(), "Failed to select photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun uploadPhotoToFirebase(bitmap: Bitmap) {
        if (userId.isNullOrEmpty()) return

        // Delete old photo before uploading a new one
        deleteOldPhotoFromFirebase()

        val fileName = "profile_pictures/$userId.jpg"
        val photoRef = storage.child(fileName)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        photoRef.putBytes(data)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    updatedPhotoUrl = uri.toString()

                    Glide.with(this)
                        .load(updatedPhotoUrl)
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(imgProfile)

                    Toast.makeText(requireContext(), "Photo updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to upload photo: ${e.message}")
            }
    }

    private fun deleteOldPhotoFromFirebase() {
        if (updatedPhotoUrl.isEmpty()) return

        val fileName = "profile_pictures/$userId.jpg"
        val oldPhotoRef = storage.child(fileName)

        oldPhotoRef.delete()
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "Old photo deleted successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to delete old photo: ${e.message}")
            }
    }
}
