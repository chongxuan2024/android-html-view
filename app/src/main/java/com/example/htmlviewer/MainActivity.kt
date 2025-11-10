package com.example.htmlviewer

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.htmlviewer.databinding.ActivityMainBinding
import com.example.htmlviewer.fragment.HallFragment
import com.example.htmlviewer.fragment.ProfileFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.htmlviewer.model.AppItem

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var isInProfileMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupStatusBar()
        setupUI()
        updateAppCount()
        loadHallFragment()
    }
    
    private fun setupStatusBar() {
        // Set status bar color to match paradise header (left side blue)
        window.statusBarColor = getColor(R.color.tropical_gradient_2_start)
        
        // Make status bar icons dark for better visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }
    }
    
    private fun setupUI() {
        // Setup profile icon click listener
        binding.profileIcon.setOnClickListener {
            if (isInProfileMode) {
                loadHallFragment()
            } else {
                loadProfileFragment()
            }
        }
    }
    
    private fun updateIcon() {
        val iconText = if (isInProfileMode) "🏠" else "👤"
        binding.iconText.text = iconText
    }
    
    private fun updateAppCount() {
        try {
            val jsonString = assets.open("apps.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = object : TypeToken<List<AppItem>>() {}.type
            val appList: List<AppItem> = gson.fromJson(jsonString, listType)
            
            binding.tvAppCount.text = "${appList.size} Apps Available"
        } catch (e: Exception) {
            binding.tvAppCount.text = "Apps Available"
        }
    }
    
    private fun loadHallFragment() {
        isInProfileMode = false
        updateIcon()
        val fragment = HallFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    private fun loadProfileFragment() {
        isInProfileMode = true
        updateIcon()
        val fragment = ProfileFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onBackPressed() {
        if (isInProfileMode) {
            loadHallFragment()
        } else {
            super.onBackPressed()
        }
    }
}