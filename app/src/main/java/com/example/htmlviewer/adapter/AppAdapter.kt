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
            
            // Set app icon and game gradient background based on app type
            val (iconRes, gradientRes) = when (appItem.getIconResourceName()) {
                "weather_icon" -> Pair(R.drawable.ic_menu_sunny_day, R.drawable.bg_game_gradient_1)
                "calculator_icon" -> Pair(android.R.drawable.ic_menu_add, R.drawable.bg_game_gradient_2)
                "todo_icon" -> Pair(android.R.drawable.ic_menu_agenda, R.drawable.bg_game_gradient_3)
                "music_icon" -> Pair(android.R.drawable.ic_media_play, R.drawable.bg_game_gradient_4)
                "gallery_icon" -> Pair(android.R.drawable.ic_menu_gallery, R.drawable.bg_game_gradient_5)
                "notes_icon" -> Pair(android.R.drawable.ic_menu_edit, R.drawable.bg_game_gradient_6)
                else -> Pair(android.R.drawable.ic_menu_info_details, R.drawable.bg_game_gradient_1)
            }
            
            binding.ivAppIcon.setImageResource(iconRes)
            binding.iconContainer.setBackgroundResource(gradientRes)
            
            // Show/hide game badge for favorites
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