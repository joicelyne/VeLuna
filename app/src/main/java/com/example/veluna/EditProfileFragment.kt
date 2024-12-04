package com.example.veluna

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class EditProfileFragment : Fragment() {

    // UI Elements
    private lateinit var etUsername: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangeProfilePicture: Button
    private lateinit var btnUpdateProfile: Button
    private lateinit var profileName: TextView
    private lateinit var btnBackEditProfile: ImageView

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private var updatedPhotoUrl: String = ""

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Initialize UI elements
        etUsername = view.findViewById(R.id.etUsername)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnChangeProfilePicture = view.findViewById(R.id.btnChangeProfilePicture)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        profileName = view.findViewById(R.id.ProfileName)
        btnBackEditProfile = view.findViewById(R.id.back_button_edit_profile)

        // Load user data from Firestore
        loadUserData()

        // Button actions
        btnChangeProfilePicture.setOnClickListener {
            checkAndRequestCameraPermission()
        }
        btnUpdateProfile.setOnClickListener {
            reauthenticateAndUpdatePassword()
        }
        btnBackEditProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun checkAndRequestCameraPermission() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Izin diberikan, buka kamera
            openCamera()
        } else {
            // Minta izin kamera
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Gagal membuka kamera: ${e.message}")
            Toast.makeText(requireContext(), "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                imgProfile.setImageBitmap(photo)
                uploadPhotoToFirebase(photo)
            } else {
                Toast.makeText(requireContext(), "Gagal mengambil foto.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Kamera ditutup tanpa mengambil foto.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhotoToFirebase(bitmap: Bitmap) {
        deleteOldPhotoFromFirebase() // Hapus foto lama

        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) return

        val fileName = "profile_pictures/$currentUserId.jpg"
        val photoRef = storage.child(fileName)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        photoRef.putBytes(data)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    updatedPhotoUrl = uri.toString()

                    // Update URL di Firestore
                    db.collection("users").document(currentUserId)
                        .update("photoUrl", updatedPhotoUrl)
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(updatedPhotoUrl)
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person)
                                .into(imgProfile)

                            Toast.makeText(requireContext(), "Foto berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditProfileFragment", "Gagal memperbarui URL foto di Firestore: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Gagal mengunggah foto ke Firebase Storage: ${e.message}")
            }
    }

    private fun deleteOldPhotoFromFirebase() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) return

        val oldPhotoRef = storage.child("profile_pictures/$currentUserId.jpg")
        oldPhotoRef.delete()
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "Foto lama berhasil dihapus dari Firebase Storage.")
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Gagal menghapus foto lama: ${e.message}")
            }
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val email = currentUser.email ?: ""
        etEmail.setText(email)
        etEmail.isEnabled = false // Disable editing for email
        etEmail.isFocusable = false // Prevent focus

        if (userId.isNullOrEmpty()) return

        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""

                    etUsername.setText(name)
                    etPhoneNumber.setText(phoneNumber)
                    etPassword.setText("") // Empty password for security
                    profileName.text = name

                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.person)
                    }
                } else {
                    Log.e("EditProfileFragment", "Dokumen tidak ditemukan.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Gagal memuat data: ${e.message}")
            }
    }


    private fun reauthenticateAndUpdatePassword() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val newPassword = etPassword.text.toString().trim()

        if (newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Password field cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val email = currentUser?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPasswordInput = EditText(requireContext())
        currentPasswordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reauthenticate")
            .setMessage("Enter your current password:")
            .setView(currentPasswordInput)
            .setPositiveButton("Reauthenticate") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString().trim()
                if (currentPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Current password cannot be empty!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        currentUser.updatePassword(newPassword)
                            .addOnSuccessListener {
                                updateFirestorePassword(hashPassword(newPassword))
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditProfileFragment", "Failed to update password: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("EditProfileFragment", "Reauthentication failed: ${e.message}")
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateFirestorePassword(hashedPassword: String) {
        if (userId.isNullOrEmpty()) return

        db.collection("users").document(userId!!)
            .update("password", hashedPassword)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_editprofileFragment_to_profileFragment)
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to update Firestore: ${e.message}")
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
