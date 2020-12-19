package com.example.clockofeidolon.clock

import androidx.lifecycle.LiveData

class ClockData(): LiveData<String>() {
    private val clock: Clock = Clock()
    private var time: String = "00:00:00"

    override fun onActive() {
        time = clock.getEventTime()
    }

    override fun onInactive() {
        time = clock.getEventTime()
    }

}