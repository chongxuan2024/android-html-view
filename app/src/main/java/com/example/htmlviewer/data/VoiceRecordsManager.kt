package com.example.htmlviewer.data

import android.content.Context
import com.example.htmlviewer.model.VoiceRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VoiceRecordsManager private constructor(context: Context) {
    
    private val prefs = context.getSharedPreferences("voice_records", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_RECORDS = "records"
        
        @Volatile
        private var instance: VoiceRecordsManager? = null
        
        fun getInstance(context: Context): VoiceRecordsManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceRecordsManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun saveRecord(record: VoiceRecord) {
        val records = getAllRecords().toMutableList()
        records.add(0, record)  // 添加到列表开头
        saveRecords(records)
    }
    
    fun getAllRecords(): List<VoiceRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<VoiceRecord>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun deleteRecord(recordId: String) {
        val records = getAllRecords().toMutableList()
        records.removeAll { it.id == recordId }
        saveRecords(records)
    }
    
    fun getRecord(recordId: String): VoiceRecord? {
        return getAllRecords().find { it.id == recordId }
    }
    
    private fun saveRecords(records: List<VoiceRecord>) {
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

