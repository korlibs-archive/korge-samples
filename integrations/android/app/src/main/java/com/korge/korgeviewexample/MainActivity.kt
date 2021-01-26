package com.korge.korgeviewexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.korge.androidviewexample.CustomModule
import com.korge.korgeviewexample.databinding.ActivityMainBinding
import com.soywiz.korge.android.KorgeAndroidView

class MainActivity : AppCompatActivity() {

    private lateinit var korgeAndroidView: KorgeAndroidView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        korgeAndroidView = KorgeAndroidView(this)
        binding.toolContainer.addView(korgeAndroidView)

        binding.loadViewButton.setOnClickListener {
            binding.loadViewButton.isEnabled = false
            binding.unloadViewButton.isEnabled = true
            loadToolModule()
        }

        binding.unloadViewButton.setOnClickListener {
            binding.loadViewButton.isEnabled = true
            binding.unloadViewButton.isEnabled = false
            unloadToolModule()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.loadViewButton.isEnabled = true
        binding.unloadViewButton.isEnabled = false

    }

    override fun onPause() {
        super.onPause()
        unloadToolModule()
    }

    private fun loadToolModule() {
        korgeAndroidView.loadModule(CustomModule(width = 1920, height = 1080, callback = {
            println("Callback from android app")
        }))
    }

    private fun unloadToolModule() {
        korgeAndroidView.unloadModule()
    }
}