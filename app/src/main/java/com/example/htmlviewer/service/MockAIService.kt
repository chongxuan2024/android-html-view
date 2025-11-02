package com.example.htmlviewer.service

import com.example.htmlviewer.data.QuestionBank
import com.example.htmlviewer.model.CompanionMessage
import com.example.htmlviewer.model.Question
import com.example.htmlviewer.model.QuestionResponse
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Mock AI服务 - 模拟智能分析和问题推荐
 * 实际生产环境中应该替换为真实的AI服务调用
 */
class MockAIService {
    
    private val emotionKeywords = mapOf(
        "焦虑" to listOf("担心", "紧张", "不安", "害怕", "压力", "恐惧"),
        "悲伤" to listOf("难过", "伤心", "失落", "沮丧", "痛苦", "孤独"),
        "愤怒" to listOf("生气", "愤怒", "恼火", "烦躁", "不满", "气愤"),
        "疲惫" to listOf("累", "疲惫", "困倦", "无力", "倦怠", "耗竭"),
        "快乐" to listOf("开心", "快乐", "高兴", "喜悦", "兴奋", "满足")
    )
    
    /**
     * 分析用户输入，返回下一个问题和陪伴语言
     */
    suspend fun analyzeAndGetNextQuestion(
        userInput: String,
        currentLevel: Int,
        previousResponses: List<QuestionResponse>
    ): Pair<Question?, CompanionMessage> {
        // 模拟AI处理延迟
        delay(500 + Random.nextLong(500))
        
        // 分析情绪
        val detectedEmotion = detectEmotion(userInput)
        
        // 根据层级返回不同的问题
        return when (currentLevel) {
            1 -> handleLevel1(userInput, detectedEmotion)
            2 -> handleLevel2(userInput, detectedEmotion, previousResponses)
            3 -> handleLevel3(userInput, detectedEmotion, previousResponses)
            else -> handleClosing(userInput, detectedEmotion)
        }
    }
    
    /**
     * 检测用户输入中的情绪
     */
    private fun detectEmotion(input: String): String {
        for ((emotion, keywords) in emotionKeywords) {
            if (keywords.any { input.contains(it, ignoreCase = true) }) {
                return emotion
            }
        }
        
        // 分析输入长度和标点
        return when {
            input.length < 10 -> "平静"
            input.contains("！！") || input.contains("!!") -> "激动"
            input.contains("...") || input.contains("。。。") -> "犹豫"
            else -> "中性"
        }
    }
    
    /**
     * 第一层：情绪识别阶段
     */
    private fun handleLevel1(input: String, emotion: String): Pair<Question, CompanionMessage> {
        val companionMessages = listOf(
            "我听到你了，能够表达出来已经很好了",
            "谢谢你愿意和我分享你的感受",
            "我在这里陪着你，慢慢来",
            "你的感受是完全正常的，不用担心"
        )
        
        val nextQuestion = QuestionBank.emotionIdentificationQuestions.getOrNull(1)
            ?: QuestionBank.emotionIdentificationQuestions.first()
        
        val message = CompanionMessage(
            message = companionMessages.random(),
            shouldSpeak = true,
            emotion = "caring"
        )
        
        return Pair(nextQuestion, message)
    }
    
    /**
     * 第二层：原因探索阶段
     */
    private fun handleLevel2(
        input: String,
        emotion: String,
        responses: List<QuestionResponse>
    ): Pair<Question?, CompanionMessage> {
        val questions = QuestionBank.getQuestionsByEmotion(emotion)
        
        // 如果已经回答了2个以上的探索问题，进入应对阶段
        val explorationCount = responses.count { it.questionId.startsWith("c") }
        
        if (explorationCount >= 2 || input.length > 50) {
            return handleLevel3(input, emotion, responses)
        }
        
        val nextQuestion = questions.firstOrNull { q ->
            responses.none { it.questionId == q.id }
        } ?: QuestionBank.deepExplorationQuestions.random()
        
        val companionMessages = when (emotion) {
            "焦虑" -> listOf(
                "我理解你的担心，让我们一起来看看",
                "焦虑是很常见的感觉，你不是一个人",
                "这种感觉一定不好受，我在这里陪你"
            )
            "悲伤" -> listOf(
                "我能感受到你的难过，允许自己悲伤是可以的",
                "你经历了很多，这些感受都是真实的",
                "我会一直在这里，陪你度过这段时光"
            )
            "愤怒" -> listOf(
                "我理解你为什么会有这样的感受",
                "愤怒告诉我们有些事情需要改变",
                "让我们一起来看看如何处理这种感觉"
            )
            "疲惫" -> listOf(
                "听起来你真的需要好好休息一下了",
                "照顾好自己是最重要的",
                "你已经很努力了，值得好好放松"
            )
            "快乐" -> listOf(
                "这真是太好了！快乐的时刻值得珍惜",
                "你的快乐也感染了我",
                "让我们把这份美好保存下来"
            )
            else -> listOf(
                "谢谢你的分享",
                "我在认真倾听你说的每一句话",
                "继续说下去，我会陪着你"
            )
        }
        
        val message = CompanionMessage(
            message = companionMessages.random(),
            shouldSpeak = true,
            emotion = "caring"
        )
        
        return Pair(nextQuestion, message)
    }
    
    /**
     * 第三层：应对策略阶段
     */
    private fun handleLevel3(
        input: String,
        emotion: String,
        responses: List<QuestionResponse>
    ): Pair<Question?, CompanionMessage> {
        val copingQuestions = QuestionBank.getCopingQuestions(emotion)
        
        val nextQuestion = copingQuestions.firstOrNull { q ->
            responses.none { it.questionId == q.id }
        }
        
        // 如果应对策略问题都问完了，准备结束
        if (nextQuestion == null) {
            return handleClosing(input, emotion)
        }
        
        val companionMessages = listOf(
            "让我们来想想如何让你感觉更好",
            "我有一些想法可能会帮到你",
            "一起来试试这些方法吧",
            "你觉得哪种方式比较适合你？"
        )
        
        val message = CompanionMessage(
            message = companionMessages.random(),
            shouldSpeak = true,
            emotion = "encouraging"
        )
        
        return Pair(nextQuestion, message)
    }
    
    /**
     * 结束阶段
     */
    private fun handleClosing(input: String, emotion: String): Pair<Question?, CompanionMessage> {
        val closingMessages = listOf(
            "今天我们聊了很多，希望你感觉好一些了。记住，我随时都在这里",
            "你很勇敢，愿意面对和表达自己的感受。照顾好自己，好吗？",
            "每一天都是新的开始。相信你能够找到适合自己的方式",
            "谢谢你信任我，和我分享你的故事。祝你一切都好"
        )
        
        val message = CompanionMessage(
            message = closingMessages.random(),
            shouldSpeak = true,
            emotion = "happy"
        )
        
        // 返回null表示可以结束会话了
        return Pair(null, message)
    }
    
    /**
     * 生成个性化的总结和建议
     */
    suspend fun generateFinalGuidance(
        emotion: String,
        responses: List<QuestionResponse>
    ): String {
        delay(1000) // 模拟AI处理
        
        return when (emotion) {
            "焦虑" -> """
                |✨ 给你的温馨建议：
                |
                |🌸 焦虑是我们身体的保护机制，它提醒我们关注重要的事情
                |
                |💫 试试这些方法：
                |• 每天练习5分钟深呼吸
                |• 将担心的事情写下来
                |• 专注于当下可以控制的事情
                |• 适当运动，释放紧张情绪
                |
                |🌈 记住：你比想象中更有力量应对挑战
            """.trimMargin()
            
            "悲伤" -> """
                |✨ 给你的温馨建议：
                |
                |🌸 悲伤是疗愈过程的一部分，允许自己感受它
                |
                |💫 试试这些方法：
                |• 找信任的人倾诉
                |• 写日记记录你的感受
                |• 做一些让你感到舒适的事情
                |• 保持规律的作息
                |
                |🌈 时间会帮助疗愈，而你并不孤单
            """.trimMargin()
            
            "愤怒" -> """
                |✨ 给你的温馨建议：
                |
                |🌸 愤怒告诉我们，有些边界被侵犯了，这是正常的
                |
                |💫 试试这些方法：
                |• 暂停，深呼吸几次
                |• 运动发泄情绪（跑步、打球）
                |• 用"我"开头表达感受，而不是指责
                |• 给自己时间冷静
                |
                |🌈 学会表达愤怒，而不是压抑或爆发
            """.trimMargin()
            
            "疲惫" -> """
                |✨ 给你的温馨建议：
                |
                |🌸 你的身体在提醒你：是时候好好照顾自己了
                |
                |💫 试试这些方法：
                |• 保证充足的睡眠
                |• 学会说"不"，设定界限
                |• 做一些不费力的放松活动
                |• 寻求他人的帮助和支持
                |
                |🌈 休息不是懒惰，而是为了更好地前行
            """.trimMargin()
            
            "快乐" -> """
                |✨ 给你的温馨建议：
                |
                |🌸 美好的时刻值得记录和分享
                |
                |💫 试试这些方法：
                |• 写下这个快乐时刻
                |• 和重要的人分享喜悦
                |• 思考是什么带来了这份快乐
                |• 计划更多类似的活动
                |
                |🌈 培养感恩的心，让快乐更持久
            """.trimMargin()
            
            else -> """
                |✨ 给你的温馨建议：
                |
                |🌸 感谢你的信任和分享
                |
                |💫 记住：
                |• 你的感受都是真实和有价值的
                |• 寻求帮助是勇敢的表现
                |• 每一天都是新的开始
                |• 照顾好自己，你值得被爱
                |
                |🌈 愿你找到内心的平静与力量
            """.trimMargin()
        }
    }
}

