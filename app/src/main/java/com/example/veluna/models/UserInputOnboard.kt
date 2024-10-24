package com.example.veluna.models

data class UserInputOnboard(
    var startDate: String? = null,
    var lastPeriod: String? = null,
    var periodLength: Int? = null,
    var cycleLength: Int? = null,
    var birthdate: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null
)

