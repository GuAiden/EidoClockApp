package com.example.clockofeidolon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.clockofeidolon.clock.Clock
import com.example.clockofeidolon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val clock: Clock = Clock()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        updateTime(binding)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateTime(binding: ActivityMainBinding) {
        binding.clockTime = clock.getEventTime()
    }

}

