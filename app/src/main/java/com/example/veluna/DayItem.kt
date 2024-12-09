package com.example.veluna

data class DayItem(
    val date: String,
    val day: String,
    val isToday: Boolean,
    val fullDate: String // Tanggal lengkap dalam format "dd MMM yyyy"
)
