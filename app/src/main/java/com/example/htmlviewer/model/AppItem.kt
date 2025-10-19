package com.example.htmlviewer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppItem(
    val title: String,
    val author: String,
    val image: String,
    val type: String,
    val url: String,
    var isFavorite: Boolean = false
) : Parcelable {
    
    // 为了兼容性，保留旧的属性名访问方式
    val appName: String get() = title
    val appIcon: String get() = image
    val appFile: String get() = url
    
    fun getIconResourceName(): String {
        return image.substringBeforeLast(".")
    }
    
    fun getDisplayName(): String {
        return title.trim()
    }
    
    fun getHtmlFileName(): String {
        return url
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as AppItem
        
        if (title != other.title) return false
        if (author != other.author) return false
        if (image != other.image) return false
        if (type != other.type) return false
        if (url != other.url) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}