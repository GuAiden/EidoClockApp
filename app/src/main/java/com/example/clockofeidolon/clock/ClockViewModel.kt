package com.example.clockofeidolon.clock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClockViewModel: ViewModel() {

    private val clock: Clock = Clock()
    val currentTime: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getTime(): LiveData<String> {
        currentTime.value = loadTime()
        return currentTime
    }

    private fun loadTime(): String {
        return clock.getEventTime()
    }

}