package com.example.htmlviewer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppItem(
    val appName: String,
    val appIcon: String,
    val appFile: String,
    var isFavorite: Boolean = false
) : Parcelable {
    
    fun getIconResourceName(): String {
        return appIcon.substringBeforeLast(".png")
    }
    
    fun getDisplayName(): String {
        return appName.trim()
    }
    
    fun getHtmlFileName(): String {
        return appFile
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as AppItem
        
        if (appName != other.appName) return false
        if (appIcon != other.appIcon) return false
        if (appFile != other.appFile) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = appName.hashCode()
        result = 31 * result + appIcon.hashCode()
        result = 31 * result + appFile.hashCode()
        return result
    }
}