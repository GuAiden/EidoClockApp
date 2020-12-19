package com.example.clockofeidolon.clock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClockViewModel: ViewModel() {

    private val clock: Clock = Clock()
    private val currentTime: ClockData() by lazy {
        ClockData()
    }

    fun getUsers(): LiveData<String> {
        return loadTime()
    }

    private fun loadTime(): String {
        return clock.getEventTime()
    }

}