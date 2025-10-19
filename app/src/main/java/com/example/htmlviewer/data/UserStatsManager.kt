package com.example.htmlviewer.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 用户统计数据管理器
 * 记录应用打开次数、游戏成绩等统计信息
 */
class UserStatsManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "UserStatsManager"
        private const val PREFS_NAME = "user_stats"
        private const val KEY_APP_OPEN_COUNT = "app_open_count"
        private const val KEY_APP_SCORES = "app_scores"
        
        @Volatile
        private var INSTANCE: UserStatsManager? = null
        
        fun getInstance(context: Context): UserStatsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserStatsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * 应用打开统计数据
     */
    data class AppOpenStats(
        val appName: String,
        val openCount: Int
    )
    
    /**
     * 应用成绩数据
     */
    data class AppScoreStats(
        val appName: String,
        val highestScore: Int,
        val lastPlayTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 作者统计数据
     */
    data class AuthorStats(
        val authorName: String,
        val appCount: Int,
        val totalPlays: Int
    )
    
    /**
     * 记录应用打开
     */
    fun recordAppOpen(appName: String) {
        try {
            val openCounts = getAppOpenCounts().toMutableMap()
            openCounts[appName] = (openCounts[appName] ?: 0) + 1
            
            val json = gson.toJson(openCounts)
            sharedPrefs.edit().putString(KEY_APP_OPEN_COUNT, json).apply()
            
            Log.d(TAG, "记录应用打开: $appName, 总次数: ${openCounts[appName]}")
        } catch (e: Exception) {
            Log.e(TAG, "记录应用打开失败", e)
        }
    }
    
    /**
     * 记录游戏成绩
     * @param appName 应用名称
     * @param score 游戏分数（必须 >= 0）
     */
    fun recordGameScore(appName: String, score: Int) {
        try {
            // 验证分数有效性
            if (score < 0) {
                Log.w(TAG, "无效分数: $score，分数不能为负数")
                return
            }
            
            val scores = getAppScores().toMutableMap()
            val currentScore = scores[appName]
            
            // 只记录更高的成绩，或者首次记录（包括0分）
            if (currentScore == null || score > currentScore.highestScore) {
                scores[appName] = AppScoreStats(appName, score, System.currentTimeMillis())
                
                val json = gson.toJson(scores)
                sharedPrefs.edit().putString(KEY_APP_SCORES, json).apply()
                
                Log.d(TAG, "记录游戏成绩: $appName, 分数: $score")
            } else {
                Log.d(TAG, "分数未更新: $appName, 当前分数: $score, 历史最高: ${currentScore.highestScore}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "记录游戏成绩失败", e)
        }
    }
    
    /**
     * 获取应用打开次数统计
     */
    private fun getAppOpenCounts(): Map<String, Int> {
        return try {
            val json = sharedPrefs.getString(KEY_APP_OPEN_COUNT, "{}")
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "获取应用打开次数失败", e)
            emptyMap()
        }
    }
    
    /**
     * 获取应用成绩统计
     */
    private fun getAppScores(): Map<String, AppScoreStats> {
        return try {
            val json = sharedPrefs.getString(KEY_APP_SCORES, "{}")
            val type = object : TypeToken<Map<String, AppScoreStats>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "获取应用成绩失败", e)
            emptyMap()
        }
    }
    
    /**
     * 获取Top5最常玩的应用
     */
    fun getTop5MostPlayedApps(): List<AppOpenStats> {
        return try {
            getAppOpenCounts()
                .map { (appName, count) -> AppOpenStats(appName, count) }
                .sortedByDescending { it.openCount }
                .take(5)
        } catch (e: Exception) {
            Log.e(TAG, "获取Top5应用失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取按成绩排序的应用列表（最多100个）
     */
    fun getAppsByScore(): List<AppScoreStats> {
        return try {
            getAppScores()
                .values
                .sortedByDescending { it.highestScore }
                .take(100)
        } catch (e: Exception) {
            Log.e(TAG, "获取成绩排行失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取作者统计信息
     */
    fun getAuthorStats(allApps: List<com.example.htmlviewer.model.AppItem>): List<AuthorStats> {
        return try {
            val openCounts = getAppOpenCounts()
            
            // 按作者分组统计
            val authorMap = mutableMapOf<String, AuthorStats>()
            
            allApps.forEach { app ->
                val playCount = openCounts[app.appName] ?: 0
                val currentStats = authorMap[app.author]
                
                if (currentStats == null) {
                    authorMap[app.author] = AuthorStats(app.author, 1, playCount)
                } else {
                    authorMap[app.author] = currentStats.copy(
                        appCount = currentStats.appCount + 1,
                        totalPlays = currentStats.totalPlays + playCount
                    )
                }
            }
            
            // 按总游玩次数排序，取前100
            authorMap.values
                .sortedByDescending { it.totalPlays }
                .take(100)
        } catch (e: Exception) {
            Log.e(TAG, "获取作者统计失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取统计摘要
     */
    fun getStatsSummary(): String {
        return try {
            val totalApps = getAppOpenCounts().size
            val totalPlays = getAppOpenCounts().values.sum()
            val totalScores = getAppScores().size
            val highestScore = getAppScores().values.maxOfOrNull { it.highestScore } ?: 0
            
            buildString {
                appendLine("📊 游戏统计摘要")
                appendLine("🎮 已玩游戏: $totalApps 款")
                appendLine("🔥 总游玩次数: $totalPlays 次")
                appendLine("🏆 有成绩游戏: $totalScores 款")
                appendLine("⭐ 最高分数: $highestScore 分")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取统计摘要失败", e)
            "统计数据获取失败"
        }
    }
    
    /**
     * 清除所有统计数据（用于测试或重置）
     */
    fun clearAllStats() {
        try {
            sharedPrefs.edit().clear().apply()
            Log.d(TAG, "已清除所有统计数据")
        } catch (e: Exception) {
            Log.e(TAG, "清除统计数据失败", e)
        }
    }
}
