package com.example.htmlviewer.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * 收藏状态持久化管理器
 * 支持多重备份机制，确保数据不丢失
 */
class FavoritesManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "FavoritesManager"
        private const val PREFS_NAME = "app_favorites"
        private const val BACKUP_FILE_NAME = "favorites_backup.json"
        private const val EXTERNAL_BACKUP_DIR = "Paradise_Backups"
        private const val VERSION_KEY = "favorites_version"
        private const val CURRENT_VERSION = 1
        
        @Volatile
        private var INSTANCE: FavoritesManager? = null
        
        fun getInstance(context: Context): FavoritesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * 收藏数据结构
     */
    data class FavoritesData(
        val version: Int = CURRENT_VERSION,
        val timestamp: Long = System.currentTimeMillis(),
        val favorites: Map<String, Boolean> = emptyMap()
    )
    
    init {
        // 初始化时检查并迁移数据
        migrateDataIfNeeded()
        // 创建备份
        createBackup()
    }
    
    /**
     * 设置收藏状态
     */
    fun setFavorite(appName: String, isFavorite: Boolean) {
        try {
            // 1. 更新SharedPreferences
            sharedPrefs.edit().putBoolean(appName, isFavorite).apply()
            
            // 2. 创建备份
            createBackup()
            
            Log.d(TAG, "设置收藏状态: $appName = $isFavorite")
        } catch (e: Exception) {
            Log.e(TAG, "设置收藏状态失败", e)
        }
    }
    
    /**
     * 获取收藏状态
     */
    fun isFavorite(appName: String): Boolean {
        return try {
            sharedPrefs.getBoolean(appName, false)
        } catch (e: Exception) {
            Log.e(TAG, "获取收藏状态失败", e)
            false
        }
    }
    
    /**
     * 获取所有收藏的应用
     */
    fun getAllFavorites(): Map<String, Boolean> {
        return try {
            sharedPrefs.all.filterValues { it is Boolean }.mapValues { it.value as Boolean }
        } catch (e: Exception) {
            Log.e(TAG, "获取所有收藏失败", e)
            emptyMap()
        }
    }
    
    /**
     * 创建备份
     */
    private fun createBackup() {
        try {
            val favorites = getAllFavorites()
            val backupData = FavoritesData(
                version = CURRENT_VERSION,
                timestamp = System.currentTimeMillis(),
                favorites = favorites
            )
            
            // 1. 内部存储备份
            createInternalBackup(backupData)
            
            // 2. 外部存储备份（如果可用）
            createExternalBackup(backupData)
            
        } catch (e: Exception) {
            Log.e(TAG, "创建备份失败", e)
        }
    }
    
    /**
     * 内部存储备份
     */
    private fun createInternalBackup(data: FavoritesData) {
        try {
            val json = gson.toJson(data)
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            FileOutputStream(file).use { fos ->
                fos.write(json.toByteArray())
            }
            Log.d(TAG, "内部备份创建成功: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "内部备份失败", e)
        }
    }
    
    /**
     * 外部存储备份
     */
    private fun createExternalBackup(data: FavoritesData) {
        try {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val externalDir = File(context.getExternalFilesDir(null), EXTERNAL_BACKUP_DIR)
                if (!externalDir.exists()) {
                    externalDir.mkdirs()
                }
                
                val backupFile = File(externalDir, BACKUP_FILE_NAME)
                val json = gson.toJson(data)
                FileOutputStream(backupFile).use { fos ->
                    fos.write(json.toByteArray())
                }
                Log.d(TAG, "外部备份创建成功: ${backupFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "外部备份失败", e)
        }
    }
    
    /**
     * 恢复备份数据
     */
    fun restoreFromBackup(): Boolean {
        return try {
            // 1. 尝试从内部存储恢复
            if (restoreFromInternalBackup()) {
                Log.d(TAG, "从内部备份恢复成功")
                return true
            }
            
            // 2. 尝试从外部存储恢复
            if (restoreFromExternalBackup()) {
                Log.d(TAG, "从外部备份恢复成功")
                return true
            }
            
            Log.w(TAG, "没有找到可用的备份文件")
            false
        } catch (e: Exception) {
            Log.e(TAG, "恢复备份失败", e)
            false
        }
    }
    
    /**
     * 从内部存储恢复
     */
    private fun restoreFromInternalBackup(): Boolean {
        return try {
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            if (file.exists()) {
                val json = FileInputStream(file).use { fis ->
                    fis.readBytes().toString(Charsets.UTF_8)
                }
                restoreFromJson(json)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "内部恢复失败", e)
            false
        }
    }
    
    /**
     * 从外部存储恢复
     */
    private fun restoreFromExternalBackup(): Boolean {
        return try {
            val externalDir = File(context.getExternalFilesDir(null), EXTERNAL_BACKUP_DIR)
            val backupFile = File(externalDir, BACKUP_FILE_NAME)
            if (backupFile.exists()) {
                val json = FileInputStream(backupFile).use { fis ->
                    fis.readBytes().toString(Charsets.UTF_8)
                }
                restoreFromJson(json)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "外部恢复失败", e)
            false
        }
    }
    
    /**
     * 从JSON恢复数据
     */
    private fun restoreFromJson(json: String): Boolean {
        return try {
            val backupData = gson.fromJson(json, FavoritesData::class.java)
            
            // 检查版本兼容性
            if (backupData.version <= CURRENT_VERSION) {
                val editor = sharedPrefs.edit()
                backupData.favorites.forEach { (appName, isFavorite) ->
                    editor.putBoolean(appName, isFavorite)
                }
                editor.apply()
                
                Log.d(TAG, "恢复了 ${backupData.favorites.size} 个收藏记录")
                true
            } else {
                Log.w(TAG, "备份版本不兼容: ${backupData.version} > $CURRENT_VERSION")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON恢复失败", e)
            false
        }
    }
    
    /**
     * 数据迁移检查
     */
    private fun migrateDataIfNeeded() {
        try {
            val currentVersion = sharedPrefs.getInt(VERSION_KEY, 0)
            
            if (currentVersion < CURRENT_VERSION) {
                // 执行数据迁移
                performDataMigration(currentVersion)
                
                // 更新版本号
                sharedPrefs.edit().putInt(VERSION_KEY, CURRENT_VERSION).apply()
                Log.d(TAG, "数据迁移完成: $currentVersion -> $CURRENT_VERSION")
            }
        } catch (e: Exception) {
            Log.e(TAG, "数据迁移失败", e)
        }
    }
    
    /**
     * 执行数据迁移
     */
    private fun performDataMigration(fromVersion: Int) {
        when (fromVersion) {
            0 -> {
                // 从旧版本迁移数据
                migrateFromLegacyPrefs()
            }
            // 未来版本的迁移逻辑可以在这里添加
        }
    }
    
    /**
     * 从旧版SharedPreferences迁移
     */
    private fun migrateFromLegacyPrefs() {
        try {
            val legacyPrefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
            val legacyFavorites = legacyPrefs.all.filterValues { it is Boolean }
            
            if (legacyFavorites.isNotEmpty()) {
                val editor = sharedPrefs.edit()
                legacyFavorites.forEach { (key, value) ->
                    editor.putBoolean(key, value as Boolean)
                }
                editor.apply()
                
                Log.d(TAG, "从旧版本迁移了 ${legacyFavorites.size} 个收藏记录")
            }
        } catch (e: Exception) {
            Log.e(TAG, "旧版本迁移失败", e)
        }
    }
    
    /**
     * 清除所有收藏数据（用于测试或重置）
     */
    fun clearAllFavorites() {
        try {
            sharedPrefs.edit().clear().apply()
            createBackup() // 备份空状态
            Log.d(TAG, "已清除所有收藏数据")
        } catch (e: Exception) {
            Log.e(TAG, "清除收藏数据失败", e)
        }
    }
    
    /**
     * 获取备份信息
     */
    fun getBackupInfo(): String {
        return try {
            val internalFile = File(context.filesDir, BACKUP_FILE_NAME)
            val externalDir = File(context.getExternalFilesDir(null), EXTERNAL_BACKUP_DIR)
            val externalFile = File(externalDir, BACKUP_FILE_NAME)
            
            buildString {
                appendLine("收藏数据备份信息:")
                appendLine("内部备份: ${if (internalFile.exists()) "存在 (${internalFile.length()} bytes)" else "不存在"}")
                appendLine("外部备份: ${if (externalFile.exists()) "存在 (${externalFile.length()} bytes)" else "不存在"}")
                appendLine("当前收藏数量: ${getAllFavorites().count { it.value }}")
            }
        } catch (e: Exception) {
            "获取备份信息失败: ${e.message}"
        }
    }
}
