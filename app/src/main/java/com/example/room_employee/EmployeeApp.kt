package com.example.room_employee

import android.app.Application

class EmployeeApp : Application() {

    val db by lazy {
        EmployeeDatabase.getInstance(this)
    }
}