package com.example.clockofeidolon

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.clockofeidolon.clock.Clock
import com.example.clockofeidolon.clock.ClockViewModel
import com.example.clockofeidolon.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private val model: ClockViewModel by viewModels()
    private val clock: Clock = Clock()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        val nameObserver = Observer<String> {clockTime -> binding.clockTime=clockTime}
//        model.currentTime.observe(this, nameObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    suspend fun updateTime(binding: ActivityMainBinding) {
        binding.clockTime = clock.getEventTime()
    }

}

