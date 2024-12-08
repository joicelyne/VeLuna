package com.example.veluna

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _photoUrl = MutableLiveData<String>()
    val photoUrl: LiveData<String> get() = _photoUrl

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updatePhotoUrl(newPhotoUrl: String) {
        _photoUrl.value = newPhotoUrl
    }
}
