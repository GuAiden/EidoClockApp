package com.example.clockofeidolon.clock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClockViewModel: ViewModel() {

    private val clock: Clock = Clock()
    private val currentTime: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getTime(): LiveData<String> {
        return currentTime
    }

    private suspend fun loadTime() {
        viewModelScope.launch {
            val time = clock.getEventTime()
            delay(1_000)
            currentTime.postValue(time)
        }
    }

}