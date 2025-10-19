package com.example.htmlviewer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htmlviewer.WebActivity
import com.example.htmlviewer.adapter.StatsAdapter
import com.example.htmlviewer.data.FavoritesManager
import com.example.htmlviewer.data.UserStatsManager
import com.example.htmlviewer.databinding.FragmentProfileBinding
import com.example.htmlviewer.model.AppItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var userStatsManager: UserStatsManager
    
    // 适配器
    private lateinit var top5Adapter: StatsAdapter<UserStatsManager.AppOpenStats>
    private lateinit var scoresAdapter: StatsAdapter<UserStatsManager.AppScoreStats>
    private lateinit var authorsAdapter: StatsAdapter<UserStatsManager.AuthorStats>
    
    companion object {
        fun newInstance() = ProfileFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化管理器
        favoritesManager = FavoritesManager.getInstance(requireContext())
        userStatsManager = UserStatsManager.getInstance(requireContext())
        
        setupUserInfo()
        setupRecyclerViews()
        loadStatisticsData()
    }
    
    private fun setupUserInfo() {
        binding.tvUserName.text = "Paradise 冲浪者"
        // 统计摘要将在loadStatisticsData中更新
    }
    
    private fun setupRecyclerViews() {
        // Top 5 最常玩应用
        binding.recyclerViewTop5.layoutManager = LinearLayoutManager(requireContext())
        
        // 成绩排行榜
        binding.recyclerViewScores.layoutManager = LinearLayoutManager(requireContext())
        
        // 作者鸣谢列表
        binding.recyclerViewAuthors.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun loadStatisticsData() {
        try {
            // 1. 加载统计摘要
            val statsSummary = userStatsManager.getStatsSummary()
            binding.tvStatsSummary.text = statsSummary
            
            // 2. 加载Top5最常玩应用
            loadTop5MostPlayed()
            
            // 3. 加载成绩排行榜
            loadScoreRanking()
            
            // 4. 加载作者鸣谢列表
            loadAuthorThanks()
            
        } catch (e: Exception) {
            binding.tvStatsSummary.text = "📊 统计数据加载失败"
        }
    }
    
    private fun loadTop5MostPlayed() {
        val top5Apps = userStatsManager.getTop5MostPlayedApps()
        
        if (top5Apps.isEmpty()) {
            binding.recyclerViewTop5.visibility = View.GONE
            binding.emptyViewTop5.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTop5.visibility = View.VISIBLE
            binding.emptyViewTop5.visibility = View.GONE
            
            top5Adapter = StatsAdapter(
                items = top5Apps,
                itemType = StatsAdapter.StatsItemType.TOP_PLAYED
            ) { appStats ->
                // 点击事件：可以跳转到对应应用
                openAppByName(appStats.appName)
            }
            binding.recyclerViewTop5.adapter = top5Adapter
        }
    }
    
    private fun loadScoreRanking() {
        val scoreRanking = userStatsManager.getAppsByScore()
        
        binding.tvScoreCount.text = "${scoreRanking.size} 款"
        
        if (scoreRanking.isEmpty()) {
            binding.recyclerViewScores.visibility = View.GONE
            binding.emptyViewScores.visibility = View.VISIBLE
        } else {
            binding.recyclerViewScores.visibility = View.VISIBLE
            binding.emptyViewScores.visibility = View.GONE
            
            scoresAdapter = StatsAdapter(
                items = scoreRanking,
                itemType = StatsAdapter.StatsItemType.SCORE_RANKING
            ) { scoreStats ->
                // 点击事件：可以跳转到对应应用
                openAppByName(scoreStats.appName)
            }
            binding.recyclerViewScores.adapter = scoresAdapter
        }
    }
    
    private fun loadAuthorThanks() {
        try {
            // 获取所有应用数据
            val allApps = loadAllAppsFromJson()
            val authorStats = userStatsManager.getAuthorStats(allApps)
            
            binding.tvAuthorCount.text = "${authorStats.size} 位"
            
            if (authorStats.isEmpty()) {
                binding.recyclerViewAuthors.visibility = View.GONE
                binding.emptyViewAuthors.visibility = View.VISIBLE
            } else {
                binding.recyclerViewAuthors.visibility = View.VISIBLE
                binding.emptyViewAuthors.visibility = View.GONE
                
                authorsAdapter = StatsAdapter(
                    items = authorStats,
                    itemType = StatsAdapter.StatsItemType.AUTHOR_THANKS
                )
                binding.recyclerViewAuthors.adapter = authorsAdapter
            }
        } catch (e: Exception) {
            binding.recyclerViewAuthors.visibility = View.GONE
            binding.emptyViewAuthors.visibility = View.VISIBLE
        }
    }
    
    private fun loadAllAppsFromJson(): List<AppItem> {
        return try {
            val jsonString = loadJSONFromAsset(requireContext(), "apps.json")
            if (jsonString != null) {
                val gson = Gson()
                val listType = object : TypeToken<List<AppItem>>() {}.type
                gson.fromJson(jsonString, listType) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun openAppByName(appName: String) {
        try {
            val allApps = loadAllAppsFromJson()
            val targetApp = allApps.find { it.appName == appName }
            
            if (targetApp != null) {
                val intent = WebActivity.newIntent(requireContext(), targetApp)
                startActivity(intent)
            }
        } catch (e: Exception) {
            // 忽略错误，用户体验优先
        }
    }
    
    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 刷新统计数据
        loadStatisticsData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}