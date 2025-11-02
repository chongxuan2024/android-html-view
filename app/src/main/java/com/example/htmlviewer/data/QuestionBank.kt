package com.example.htmlviewer.data

import com.example.htmlviewer.model.Question

/**
 * 心理疗愈问题题库
 * 基于认知行为疗法(CBT)和积极心理学原则设计
 */
object QuestionBank {
    
    // 第一层：情绪识别问题
    val emotionIdentificationQuestions = listOf(
        Question(
            id = "e1",
            category = "情绪识别",
            level = 1,
            text = "此刻，你的心情是怎样的呢？",
            options = listOf(
                "感到平静和放松",
                "有些焦虑不安",
                "感到悲伤或失落",
                "有些烦躁或生气",
                "感到疲惫无力",
                "充满活力和快乐"
            ),
            tags = listOf("基础评估", "情绪状态")
        ),
        Question(
            id = "e2",
            category = "情绪识别",
            level = 1,
            text = "这种感觉持续多久了？",
            options = listOf(
                "刚刚才开始",
                "几个小时",
                "一两天",
                "好几天了",
                "已经很长时间了"
            ),
            tags = listOf("时间评估", "情绪持续性")
        ),
        Question(
            id = "e3",
            category = "情绪识别",
            level = 1,
            text = "用0到10来描述，你现在的情绪强度有多大？",
            isOpenEnded = true,
            tags = listOf("情绪强度", "量化评估")
        )
    )
    
    // 第二层：原因探索问题
    val causeExplorationQuestions = mapOf(
        "焦虑" to listOf(
            Question(
                id = "c1_anxiety",
                category = "原因探索",
                level = 2,
                text = "是什么让你感到焦虑呢？可以跟我说说吗？",
                isOpenEnded = true,
                tags = listOf("焦虑", "原因探索")
            ),
            Question(
                id = "c2_anxiety",
                category = "原因探索",
                level = 2,
                text = "当焦虑出现时，你的身体有什么感觉吗？",
                options = listOf(
                    "心跳加速",
                    "呼吸急促",
                    "手心出汗",
                    "肌肉紧张",
                    "头晕或头痛",
                    "没有特别感觉"
                ),
                tags = listOf("焦虑", "身体感受")
            )
        ),
        "悲伤" to listOf(
            Question(
                id = "c1_sadness",
                category = "原因探索",
                level = 2,
                text = "能告诉我是什么让你感到难过吗？",
                isOpenEnded = true,
                tags = listOf("悲伤", "原因探索")
            ),
            Question(
                id = "c2_sadness",
                category = "原因探索",
                level = 2,
                text = "在这种情况下，你最想要的是什么？",
                isOpenEnded = true,
                tags = listOf("悲伤", "需求探索")
            )
        ),
        "愤怒" to listOf(
            Question(
                id = "c1_anger",
                category = "原因探索",
                level = 2,
                text = "是什么触发了你的这种感觉？",
                isOpenEnded = true,
                tags = listOf("愤怒", "触发因素")
            ),
            Question(
                id = "c2_anger",
                category = "原因探索",
                level = 2,
                text = "在这个情况中，你觉得什么是最不公平的？",
                isOpenEnded = true,
                tags = listOf("愤怒", "公平感")
            )
        ),
        "疲惫" to listOf(
            Question(
                id = "c1_tired",
                category = "原因探索",
                level = 2,
                text = "最近是什么消耗了你的精力？",
                isOpenEnded = true,
                tags = listOf("疲惫", "能量消耗")
            ),
            Question(
                id = "c2_tired",
                category = "原因探索",
                level = 2,
                text = "你最近有好好休息吗？",
                options = listOf(
                    "睡眠充足",
                    "睡眠不足",
                    "睡眠质量不好",
                    "很难入睡",
                    "容易醒来"
                ),
                tags = listOf("疲惫", "睡眠质量")
            )
        ),
        "快乐" to listOf(
            Question(
                id = "c1_happy",
                category = "原因探索",
                level = 2,
                text = "太好了！是什么让你感到快乐呢？",
                isOpenEnded = true,
                tags = listOf("快乐", "积极事件")
            ),
            Question(
                id = "c2_happy",
                category = "原因探索",
                level = 2,
                text = "你想和谁分享这份快乐？",
                isOpenEnded = true,
                tags = listOf("快乐", "社会支持")
            )
        )
    )
    
    // 第三层：应对策略和引导
    val copingStrategyQuestions = mapOf(
        "焦虑" to listOf(
            Question(
                id = "s1_anxiety",
                category = "应对策略",
                level = 3,
                text = "让我们一起做个深呼吸练习好吗？",
                options = listOf("好的，开始吧", "稍后再做"),
                tags = listOf("焦虑", "呼吸练习")
            ),
            Question(
                id = "s2_anxiety",
                category = "应对策略",
                level = 3,
                text = "以前遇到类似情况，什么方法帮助过你？",
                isOpenEnded = true,
                tags = listOf("焦虑", "应对经验")
            )
        ),
        "悲伤" to listOf(
            Question(
                id = "s1_sadness",
                category = "应对策略",
                level = 3,
                text = "现在有人可以陪伴你吗？",
                options = listOf("有的", "暂时没有", "我想一个人待会儿"),
                tags = listOf("悲伤", "社会支持")
            ),
            Question(
                id = "s2_sadness",
                category = "应对策略",
                level = 3,
                text = "想想看，有什么小事能让你感觉好一点？",
                isOpenEnded = true,
                tags = listOf("悲伤", "积极行为")
            )
        ),
        "愤怒" to listOf(
            Question(
                id = "s1_anger",
                category = "应对策略",
                level = 3,
                text = "让我们暂停一下，你可以试着：",
                options = listOf(
                    "深呼吸几次",
                    "离开现场冷静一下",
                    "运动发泄情绪",
                    "写下你的感受"
                ),
                tags = listOf("愤怒", "情绪管理")
            )
        ),
        "疲惫" to listOf(
            Question(
                id = "s1_tired",
                category = "应对策略",
                level = 3,
                text = "你觉得现在最需要的是：",
                options = listOf(
                    "好好睡一觉",
                    "放松休息",
                    "运动活动",
                    "和朋友聊天",
                    "独处静心"
                ),
                tags = listOf("疲惫", "恢复策略")
            )
        ),
        "快乐" to listOf(
            Question(
                id = "s1_happy",
                category = "应对策略",
                level = 3,
                text = "如何让这份快乐持续下去？",
                options = listOf(
                    "记录这个美好时刻",
                    "分享给重要的人",
                    "计划更多类似活动",
                    "表达感恩"
                ),
                tags = listOf("快乐", "正向强化")
            )
        )
    )
    
    // 深度探索问题
    val deepExplorationQuestions = listOf(
        Question(
            id = "d1",
            category = "深度探索",
            level = 2,
            text = "这个情况让你想起过去的什么经历吗？",
            isOpenEnded = true,
            tags = listOf("深度探索", "过往经历")
        ),
        Question(
            id = "d2",
            category = "深度探索",
            level = 2,
            text = "如果你的好朋友遇到同样的情况，你会对TA说什么？",
            isOpenEnded = true,
            tags = listOf("深度探索", "自我关怀")
        ),
        Question(
            id = "d3",
            category = "深度探索",
            level = 2,
            text = "在这个情况中，有什么是你可以控制的吗？",
            isOpenEnded = true,
            tags = listOf("深度探索", "控制感")
        )
    )
    
    // 积极结束问题
    val closingQuestions = listOf(
        Question(
            id = "close1",
            category = "总结",
            level = 3,
            text = "现在的感觉比刚才好些了吗？",
            options = listOf("好多了", "稍微好一点", "差不多", "还是不太好"),
            tags = listOf("评估", "结束")
        ),
        Question(
            id = "close2",
            category = "总结",
            level = 3,
            text = "今天我们的对话，有什么让你印象深刻的吗？",
            isOpenEnded = true,
            tags = listOf("反思", "结束")
        ),
        Question(
            id = "close3",
            category = "总结",
            level = 3,
            text = "接下来你想做些什么呢？",
            isOpenEnded = true,
            tags = listOf("行动计划", "结束")
        )
    )
    
    fun getQuestionsByEmotion(emotion: String): List<Question> {
        return when (emotion) {
            "焦虑" -> causeExplorationQuestions["焦虑"] ?: emptyList()
            "悲伤" -> causeExplorationQuestions["悲伤"] ?: emptyList()
            "愤怒" -> causeExplorationQuestions["愤怒"] ?: emptyList()
            "疲惫" -> causeExplorationQuestions["疲惫"] ?: emptyList()
            "快乐" -> causeExplorationQuestions["快乐"] ?: emptyList()
            else -> deepExplorationQuestions
        }
    }
    
    fun getCopingQuestions(emotion: String): List<Question> {
        return copingStrategyQuestions[emotion] ?: emptyList()
    }
}

