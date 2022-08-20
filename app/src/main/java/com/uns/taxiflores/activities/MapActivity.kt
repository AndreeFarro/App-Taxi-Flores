package com.uns.taxiflores.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }
}