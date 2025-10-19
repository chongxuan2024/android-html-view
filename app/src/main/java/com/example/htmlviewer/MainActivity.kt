package com.example.htmlviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.htmlviewer.databinding.ActivityMainBinding
import com.example.htmlviewer.fragment.HallFragment
import com.example.htmlviewer.fragment.ProfileFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.htmlviewer.model.AppItem

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        updateAppCount()
        loadHallFragment()
    }
    
    private fun setupUI() {
        // Setup profile icon click listener
        binding.profileIcon.setOnClickListener {
            loadProfileFragment()
        }
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
        val fragment = HallFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    private fun loadProfileFragment() {
        val fragment = ProfileFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}