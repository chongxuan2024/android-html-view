package com.example.htmlviewer.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htmlviewer.R
import com.example.htmlviewer.databinding.ItemAppBinding
import com.example.htmlviewer.model.AppItem
import java.io.IOException

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
            
            // Load image from assets
            try {
                val inputStream = binding.root.context.assets.open(appItem.appIcon)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    binding.ivAppIcon.setImageBitmap(bitmap)
                } else {
                    // Fallback to default icon
                    binding.ivAppIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                }
                inputStream.close()
            } catch (e: IOException) {
                // Fallback to default icon
                binding.ivAppIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            }
            
            // Set tropical gradient background based on position
            val gradientRes = when (bindingAdapterPosition % 6) {
                0 -> R.drawable.tropical_gradient_1
                1 -> R.drawable.tropical_gradient_2
                2 -> R.drawable.tropical_gradient_3
                3 -> R.drawable.tropical_gradient_4
                4 -> R.drawable.tropical_gradient_5
                5 -> R.drawable.tropical_gradient_6
                else -> R.drawable.tropical_gradient_1
            }
            
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