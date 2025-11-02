package com.example.htmlviewer.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.htmlviewer.R
import com.example.htmlviewer.data.QuestionBank
import com.example.htmlviewer.databinding.ActivityMentalHealingBinding
import com.example.htmlviewer.model.CompanionMessage
import com.example.htmlviewer.model.Question
import com.example.htmlviewer.model.QuestionResponse
import com.example.htmlviewer.model.TherapySession
import com.example.htmlviewer.service.MockAIService
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MentalHealingActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMentalHealingBinding
    private lateinit var textToSpeech: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private val aiService = MockAIService()
    private var currentQuestion: Question? = null
    private var currentLevel = 1
    private var detectedEmotion = "中性"
    
    private val session = TherapySession(
        sessionId = UUID.randomUUID().toString(),
        startTime = System.currentTimeMillis()
    )
    
    companion object {
        private const val REQUEST_RECORD_AUDIO = 101
        
        fun newIntent(context: Context): Intent {
            return Intent(context, MentalHealingActivity::class.java)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMentalHealingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        initializeTextToSpeech()
        setupSpeechRecognizer()
        startTherapySession()
    }
    
    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnVoiceRecords.setOnClickListener {
            startActivity(VoiceRecordsActivity.newIntent(this))
        }
        
        binding.btnVoiceInput.setOnClickListener {
            if (checkAudioPermission()) {
                startListening()
            } else {
                requestAudioPermission()
            }
        }
        
        binding.btnSend.setOnClickListener {
            val input = binding.etInput.text.toString().trim()
            if (input.isNotEmpty()) {
                handleUserInput(input)
                binding.etInput.text.clear()
            }
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "语音不支持中文", Toast.LENGTH_SHORT).show()
            } else {
                // 设置语音参数（使用系统默认的甜美声音）
                textToSpeech.setPitch(1.2f)  // 稍微提高音调
                textToSpeech.setSpeechRate(0.9f)  // 稍微慢一点，更温柔
            }
        } else {
            Toast.makeText(this, "语音初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    binding.btnVoiceInput.setImageResource(android.R.drawable.ic_media_pause)
                }
                
                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    isListening = false
                    binding.btnVoiceInput.setImageResource(android.R.drawable.ic_btn_speak_now)
                }
                
                override fun onError(error: Int) {
                    isListening = false
                    binding.btnVoiceInput.setImageResource(android.R.drawable.ic_btn_speak_now)
                    Toast.makeText(this@MentalHealingActivity, "语音识别出错", Toast.LENGTH_SHORT).show()
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        binding.etInput.setText(text)
                        handleUserInput(text)
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }
    
    private fun startListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "说出你的感受...")
            }
            speechRecognizer?.startListening(intent)
        }
    }
    
    private fun startTherapySession() {
        showWelcomeMessage()
        loadFirstQuestion()
    }
    
    private fun showWelcomeMessage() {
        val welcomeMessage = """
            欢迎来到心灵树洞 🌳
            
            这是一个安全、温暖的空间
            你可以放心地表达任何感受
            
            我会陪伴你，一起探索内心
            让我们开始吧...
        """.trimIndent()
        
        showCompanionMessage(
            CompanionMessage(
                message = welcomeMessage,
                shouldSpeak = true,
                emotion = "caring"
            )
        )
    }
    
    private fun loadFirstQuestion() {
        currentQuestion = QuestionBank.emotionIdentificationQuestions.first()
        displayQuestion(currentQuestion!!)
    }
    
    private fun displayQuestion(question: Question) {
        binding.tvQuestion.text = question.text
        binding.optionsContainer.removeAllViews()
        
        // 如果有选项，显示选项按钮
        question.options?.forEach { option ->
            val button = createOptionButton(option)
            binding.optionsContainer.addView(button)
        }
        
        // 滚动到底部
        binding.scrollContent.post {
            binding.scrollContent.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun createOptionButton(option: String): Button {
        return Button(this).apply {
            text = option
            setBackgroundResource(R.drawable.bg_option_button)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setPadding(32, 24, 32, 24)
            setOnClickListener {
                handleUserInput(option)
            }
        }
    }
    
    private fun handleUserInput(input: String) {
        // 记录用户回答
        currentQuestion?.let { question ->
            val response = QuestionResponse(
                questionId = question.id,
                answer = input
            )
            session.responses.add(response)
            
            // 添加到对话记录
            addToConversation("你", input, isUser = true)
        }
        
        // 隐藏当前问题
        binding.questionCard.visibility = View.GONE
        
        // 显示加载状态
        showLoading(true)
        
        // 调用AI服务获取下一个问题
        lifecycleScope.launch {
            try {
                val (nextQuestion, companionMsg) = aiService.analyzeAndGetNextQuestion(
                    input,
                    currentLevel,
                    session.responses
                )
                
                showLoading(false)
                
                // 显示陪伴者的回应
                showCompanionMessage(companionMsg)
                addToConversation("小树", companionMsg.message, isUser = false)
                
                if (nextQuestion != null) {
                    // 更新当前问题和层级
                    currentQuestion = nextQuestion
                    currentLevel = nextQuestion.level
                    
                    // 延迟显示下一个问题，让用户有时间看陪伴者的消息
                    binding.questionCard.postDelayed({
                        binding.questionCard.visibility = View.VISIBLE
                        displayQuestion(nextQuestion)
                    }, 2000)
                } else {
                    // 会话结束
                    endSession()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@MentalHealingActivity, "处理出错: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showCompanionMessage(message: CompanionMessage) {
        binding.companionCard.visibility = View.VISIBLE
        binding.tvCompanionMessage.text = message.message
        binding.companionRobot.setEmotion(message.emotion)
        
        if (message.shouldSpeak) {
            binding.companionRobot.setSpeaking(true)
            speakMessage(message.message)
        }
        
        // 自动隐藏（如果不需要语音）
        if (!message.shouldSpeak) {
            binding.companionCard.postDelayed({
                binding.companionCard.visibility = View.GONE
            }, 5000)
        }
    }
    
    private fun speakMessage(message: String) {
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                runOnUiThread {
                    binding.companionRobot.setSpeaking(true)
                }
            }
            
            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    binding.companionRobot.setSpeaking(false)
                    binding.companionCard.postDelayed({
                        binding.companionCard.visibility = View.GONE
                    }, 2000)
                }
            }
            
            override fun onError(utteranceId: String?) {
                runOnUiThread {
                    binding.companionRobot.setSpeaking(false)
                }
            }
        })
        
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "companion_message")
    }
    
    private fun addToConversation(speaker: String, message: String, isUser: Boolean) {
        val textView = TextView(this).apply {
            text = "$speaker: $message"
            textSize = 16f
            setTextColor(if (isUser) 
                ContextCompat.getColor(context, R.color.black)
            else 
                ContextCompat.getColor(context, R.color.purple_700)
            )
            setPadding(16, 12, 16, 12)
            setBackgroundResource(if (isUser) 
                R.drawable.bg_user_message
            else 
                R.drawable.bg_companion_message
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(if (isUser) 48 else 0, 0, if (isUser) 0 else 48, 16)
                gravity = if (isUser) Gravity.END else Gravity.START
            }
        }
        
        binding.conversationContainer.addView(textView)
        
        // 滚动到底部
        binding.scrollContent.post {
            binding.scrollContent.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun endSession() {
        lifecycleScope.launch {
            val guidance = aiService.generateFinalGuidance(detectedEmotion, session.responses)
            session.finalGuidance = guidance
            
            showCompanionMessage(
                CompanionMessage(
                    message = guidance,
                    shouldSpeak = true,
                    emotion = "happy"
                )
            )
            
            // 显示结束按钮
            binding.questionCard.postDelayed({
                showEndSessionOptions()
            }, 5000)
        }
    }
    
    private fun showEndSessionOptions() {
        binding.questionCard.visibility = View.VISIBLE
        binding.tvQuestion.text = "感谢你的信任和分享 💚"
        binding.optionsContainer.removeAllViews()
        
        val optionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val btnNewSession = createOptionButton("开始新的对话")
        btnNewSession.setOnClickListener {
            recreate()
        }
        
        val btnVoiceRecords = createOptionButton("查看我的留音路")
        btnVoiceRecords.setOnClickListener {
            startActivity(VoiceRecordsActivity.newIntent(this))
        }
        
        val btnExit = createOptionButton("离开")
        btnExit.setOnClickListener {
            finish()
        }
        
        optionsLayout.addView(btnNewSession)
        optionsLayout.addView(btnVoiceRecords)
        optionsLayout.addView(btnExit)
        
        binding.optionsContainer.addView(optionsLayout)
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音输入", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer?.destroy()
    }
}

