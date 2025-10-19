package com.example.htmlviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htmlviewer.data.UserStatsManager
import com.example.htmlviewer.databinding.ItemStatsBinding

/**
 * 统计数据适配器
 * 支持多种统计数据类型的显示
 */
class StatsAdapter<T>(
    private val items: List<T>,
    private val itemType: StatsItemType,
    private val onItemClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<StatsAdapter<T>.StatsViewHolder>() {
    
    enum class StatsItemType {
        TOP_PLAYED,    // Top5最常玩
        SCORE_RANKING, // 成绩排行
        AUTHOR_THANKS  // 作者鸣谢
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val binding = ItemStatsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StatsViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(items[position], position)
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class StatsViewHolder(private val binding: ItemStatsBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: T, position: Int) {
            when (itemType) {
                StatsItemType.TOP_PLAYED -> bindTopPlayed(item as UserStatsManager.AppOpenStats, position)
                StatsItemType.SCORE_RANKING -> bindScoreRanking(item as UserStatsManager.AppScoreStats, position)
                StatsItemType.AUTHOR_THANKS -> bindAuthorThanks(item as UserStatsManager.AuthorStats, position)
            }
            
            // 设置点击事件
            onItemClick?.let { clickListener ->
                binding.root.setOnClickListener { clickListener(item) }
            }
        }
        
        private fun bindTopPlayed(item: UserStatsManager.AppOpenStats, position: Int) {
            binding.apply {
                // 排名
                tvRank.text = "${position + 1}"
                
                // 应用名称
                tvTitle.text = item.appName
                
                // 打开次数
                tvSubtitle.text = "游玩 ${item.openCount} 次"
                
                // 排名图标
                tvIcon.text = when (position) {
                    0 -> "🥇"
                    1 -> "🥈" 
                    2 -> "🥉"
                    else -> "🎮"
                }
                
                // 设置背景色
                root.setBackgroundResource(
                    when (position) {
                        0 -> android.R.color.holo_orange_light
                        1 -> android.R.color.darker_gray
                        2 -> android.R.color.holo_orange_dark
                        else -> android.R.color.transparent
                    }
                )
            }
        }
        
        private fun bindScoreRanking(item: UserStatsManager.AppScoreStats, position: Int) {
            binding.apply {
                // 排名
                tvRank.text = "${position + 1}"
                
                // 应用名称
                tvTitle.text = item.appName
                
                // 最高分数
                tvSubtitle.text = "最高分: ${item.highestScore} 分"
                
                // 成绩图标
                tvIcon.text = when {
                    item.highestScore >= 1000 -> "👑"
                    item.highestScore >= 500 -> "🏆"
                    item.highestScore >= 100 -> "🥇"
                    else -> "🎯"
                }
                
                // 根据分数设置背景色
                root.setBackgroundResource(
                    when {
                        item.highestScore >= 1000 -> android.R.color.holo_purple
                        item.highestScore >= 500 -> android.R.color.holo_orange_light
                        item.highestScore >= 100 -> android.R.color.holo_blue_light
                        else -> android.R.color.transparent
                    }
                )
            }
        }
        
        private fun bindAuthorThanks(item: UserStatsManager.AuthorStats, position: Int) {
            binding.apply {
                // 排名
                tvRank.text = "${position + 1}"
                
                // 作者名称
                tvTitle.text = item.authorName
                
                // 统计信息
                tvSubtitle.text = "${item.appCount} 款游戏 • 总游玩 ${item.totalPlays} 次"
                
                // 作者图标
                tvIcon.text = when {
                    item.totalPlays >= 100 -> "🌟"
                    item.totalPlays >= 50 -> "⭐"
                    item.appCount >= 3 -> "👨‍💻"
                    else -> "👤"
                }
                
                // 根据贡献度设置背景色
                root.setBackgroundResource(
                    when {
                        item.totalPlays >= 100 -> android.R.color.holo_green_light
                        item.totalPlays >= 50 -> android.R.color.holo_blue_light
                        item.appCount >= 3 -> android.R.color.holo_orange_light
                        else -> android.R.color.transparent
                    }
                )
            }
        }
    }
}
