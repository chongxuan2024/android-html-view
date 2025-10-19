package com.example.htmlviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.htmlviewer.databinding.ActivityMainBinding
import com.example.htmlviewer.fragment.HallFragment
import com.example.htmlviewer.fragment.ProfileFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.htmlviewer.model.AppItem

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        updateAppCount()
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
    
    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_hall)
                1 -> getString(R.string.tab_profile)
                else -> ""
            }
        }.attach()
    }
    
    private class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HallFragment.newInstance()
                1 -> ProfileFragment.newInstance()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}