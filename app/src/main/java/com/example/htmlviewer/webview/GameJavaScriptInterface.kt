package com.example.htmlviewer.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.example.htmlviewer.data.UserStatsManager

/**
 * JavaScript接口类
 * 为HTML游戏提供与Android应用交互的接口
 */
class GameJavaScriptInterface(
    private val context: Context,
    private val appName: String
) {
    
    companion object {
        private const val TAG = "GameJSInterface"
        const val INTERFACE_NAME = "ParadiseGame"
    }
    
    private val userStatsManager = UserStatsManager.getInstance(context)
    
    /**
     * 提交游戏分数
     * HTML游戏调用此方法提交分数
     * 
     * @param score 游戏分数
     * @param gameInfo 可选的游戏信息（JSON格式）
     */
    @JavascriptInterface
    fun submitScore(score: Int, gameInfo: String = "") {
        try {
            Log.d(TAG, "收到游戏分数: $appName = $score 分")
            
            // 验证分数有效性
            if (score < 0) {
                Log.w(TAG, "无效分数: $score，分数不能为负数")
                showToast("分数无效：不能为负数")
                return
            }
            
            if (score > 999999) {
                Log.w(TAG, "分数过高: $score，可能存在异常")
                showToast("分数过高，请检查游戏逻辑")
                return
            }
            
            // 记录分数
            userStatsManager.recordGameScore(appName, score)
            
            // 显示成功提示
            val message = if (score > 0) {
                "🎉 恭喜！获得 $score 分"
            } else {
                "📊 分数已记录"
            }
            showToast(message)
            
            Log.d(TAG, "分数记录成功: $appName = $score 分")
            
        } catch (e: Exception) {
            Log.e(TAG, "记录分数失败", e)
            showToast("分数记录失败，请重试")
        }
    }
    
    /**
     * 获取当前游戏的最高分
     * HTML游戏可以调用此方法获取历史最高分
     * 
     * @return 最高分数
     */
    @JavascriptInterface
    fun getHighScore(): Int {
        return try {
            val scores = userStatsManager.getAppsByScore()
            val currentGameScore = scores.find { it.appName == appName }
            val highScore = currentGameScore?.highestScore ?: 0
            
            Log.d(TAG, "获取最高分: $appName = $highScore 分")
            highScore
        } catch (e: Exception) {
            Log.e(TAG, "获取最高分失败", e)
            0
        }
    }
    
    /**
     * 获取游戏游玩次数
     * HTML游戏可以调用此方法获取游玩统计
     * 
     * @return 游玩次数
     */
    @JavascriptInterface
    fun getPlayCount(): Int {
        return try {
            val playStats = userStatsManager.getTop5MostPlayedApps()
            val currentGameStats = playStats.find { it.appName == appName }
            val playCount = currentGameStats?.openCount ?: 0
            
            Log.d(TAG, "获取游玩次数: $appName = $playCount 次")
            playCount
        } catch (e: Exception) {
            Log.e(TAG, "获取游玩次数失败", e)
            0
        }
    }
    
    /**
     * 游戏开始通知
     * HTML游戏开始时调用此方法
     */
    @JavascriptInterface
    fun gameStart() {
        try {
            Log.d(TAG, "游戏开始: $appName")
            showToast("🎮 游戏开始，加油！")
        } catch (e: Exception) {
            Log.e(TAG, "游戏开始通知失败", e)
        }
    }
    
    /**
     * 游戏结束通知
     * HTML游戏结束时调用此方法
     * 
     * @param finalScore 最终分数
     * @param gameTime 游戏时长（秒）
     */
    @JavascriptInterface
    fun gameEnd(finalScore: Int, gameTime: Int = 0) {
        try {
            Log.d(TAG, "游戏结束: $appName，最终分数: $finalScore，游戏时长: ${gameTime}秒")
            
            // 自动提交最终分数
            submitScore(finalScore)
            
            // 显示游戏结束信息
            val timeInfo = if (gameTime > 0) "，用时 ${gameTime}秒" else ""
            showToast("🏁 游戏结束！最终得分: $finalScore$timeInfo")
            
        } catch (e: Exception) {
            Log.e(TAG, "游戏结束通知失败", e)
        }
    }
    
    /**
     * 显示提示信息
     * HTML游戏可以调用此方法显示自定义提示
     * 
     * @param message 提示信息
     */
    @JavascriptInterface
    fun showMessage(message: String) {
        try {
            Log.d(TAG, "显示消息: $message")
            showToast(message)
        } catch (e: Exception) {
            Log.e(TAG, "显示消息失败", e)
        }
    }
    
    /**
     * 获取应用信息
     * 返回当前应用的基本信息（JSON格式）
     * 
     * @return JSON格式的应用信息
     */
    @JavascriptInterface
    fun getAppInfo(): String {
        return try {
            val appInfo = """
                {
                    "appName": "$appName",
                    "version": "1.0",
                    "platform": "Paradise HTML Viewer",
                    "highScore": ${getHighScore()},
                    "playCount": ${getPlayCount()}
                }
            """.trimIndent()
            
            Log.d(TAG, "获取应用信息: $appInfo")
            appInfo
        } catch (e: Exception) {
            Log.e(TAG, "获取应用信息失败", e)
            "{\"error\": \"获取信息失败\"}"
        }
    }
    
    /**
     * 在主线程显示Toast
     */
    private fun showToast(message: String) {
        try {
            // 确保在主线程显示Toast
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "显示Toast失败", e)
        }
    }
}
