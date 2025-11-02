package com.example.htmlviewer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    val id: String,
    val category: String,  // 问题类别：情绪识别、原因探索、应对策略等
    val level: Int,        // 问题层次：1-基础情绪，2-深层原因，3-解决方案
    val text: String,      // 问题文本
    val options: List<String>? = null,  // 可选答案（如果是选择题）
    val isOpenEnded: Boolean = false,   // 是否是开放式问题
    val tags: List<String> = listOf()   // 标签：焦虑、压力、悲伤、愤怒等
) : Parcelable

@Parcelize
data class QuestionResponse(
    val questionId: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class TherapySession(
    val sessionId: String,
    val startTime: Long,
    val responses: MutableList<QuestionResponse> = mutableListOf(),
    val emotionScore: Int = 50,  // 情绪分数 0-100
    var finalGuidance: String = ""  // 改为var以便后续赋值
) : Parcelable

data class CompanionMessage(
    val message: String,
    val shouldSpeak: Boolean = true,
    val emotion: String = "neutral"  // neutral, happy, caring, encouraging
)

data class VoiceRecord(
    val id: String,
    val filePath: String,
    val transcription: String,
    val timestamp: Long,
    val emotionTag: String,
    val duration: Long
)

