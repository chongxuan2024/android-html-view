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
            
            // Set app icon based on app type
            val iconRes = when (appItem.getIconResourceName()) {
                "weather_icon" -> R.drawable.ic_menu_sunny_day
                "calculator_icon" -> android.R.drawable.ic_menu_add
                "todo_icon" -> android.R.drawable.ic_menu_agenda
                "music_icon" -> android.R.drawable.ic_media_play
                "gallery_icon" -> android.R.drawable.ic_menu_gallery
                "notes_icon" -> android.R.drawable.ic_menu_edit
                else -> android.R.drawable.ic_menu_info_details
            }
            binding.ivAppIcon.setImageResource(iconRes)
            
            // Set favorite status
            val favoriteIcon = if (appItem.isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            binding.ivFavorite.setImageResource(favoriteIcon)
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(appItem)
            }
        }
    }
}