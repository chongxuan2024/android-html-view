package com.example.htmlviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htmlviewer.R
import com.example.htmlviewer.databinding.ItemAppBinding
import com.example.htmlviewer.model.AppItem

class AppAdapter(
    private val appList: List<AppItem>,
    private val onItemClick: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(appList[position])
    }
    
    override fun getItemCount(): Int = appList.size
    
    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(appItem: AppItem) {
            binding.tvAppName.text = appItem.getDisplayName()
            
            // Set app icon and tropical gradient background based on app type
            val (iconRes, gradientRes) = when (appItem.getIconResourceName()) {
                "weather_app_icon" -> Pair(R.drawable.weather_app_icon, R.drawable.tropical_gradient_1)
                "calculator_app_icon" -> Pair(R.drawable.calculator_app_icon, R.drawable.tropical_gradient_2)
                "todo_app_icon" -> Pair(R.drawable.todo_app_icon, R.drawable.tropical_gradient_3)
                "music_app_icon" -> Pair(R.drawable.music_app_icon, R.drawable.tropical_gradient_4)
                "gallery_app_icon" -> Pair(R.drawable.gallery_app_icon, R.drawable.tropical_gradient_5)
                "notes_app_icon" -> Pair(R.drawable.notes_app_icon, R.drawable.tropical_gradient_6)
                else -> Pair(android.R.drawable.ic_menu_info_details, R.drawable.tropical_gradient_1)
            }
            
            binding.ivAppIcon.setImageResource(iconRes)
            binding.iconContainer.setBackgroundResource(gradientRes)
            
            // Show/hide favorite badge
            binding.ivFavoriteBadge.visibility = if (appItem.isFavorite) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(appItem)
            }
        }
    }
}