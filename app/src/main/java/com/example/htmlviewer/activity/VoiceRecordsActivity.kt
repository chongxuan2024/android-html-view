package com.example.htmlviewer.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htmlviewer.adapter.VoiceRecordAdapter
import com.example.htmlviewer.databinding.ActivityVoiceRecordsBinding
import com.example.htmlviewer.model.VoiceRecord
import com.example.htmlviewer.data.VoiceRecordsManager
import java.io.File
import java.util.*

class VoiceRecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceRecordsBinding
    private lateinit var voiceRecordsManager: VoiceRecordsManager
    private lateinit var adapter: VoiceRecordAdapter
    
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null
    
    private var isRecording = false
    private var recordStartTime = 0L
    private var currentRecordPath: String? = null
    
    companion object {
        private const val REQUEST_PERMISSIONS = 201
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        fun newIntent(context: Context): Intent {
            return Intent(context, VoiceRecordsActivity::class.java)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        voiceRecordsManager = VoiceRecordsManager.getInstance(this)
        
        setupViews()
        setupRecyclerView()
        setupSpeechRecognizer()
        loadVoiceRecords()
    }
    
    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.fabRecord.setOnClickListener {
            if (checkPermissions()) {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            } else {
                requestPermissions()
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = VoiceRecordAdapter(
            onPlayClick = { record -> playRecord(record) },
            onDeleteClick = { record -> confirmDelete(record) }
        )
        
        binding.recyclerVoiceRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerVoiceRecords.adapter = adapter
    }
    
    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
    }
    
    private fun loadVoiceRecords() {
        val records = voiceRecordsManager.getAllRecords()
        adapter.submitList(records)
        
        binding.emptyView.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun startRecording() {
        try {
            val recordsDir = File(getExternalFilesDir(null), "voice_records")
            if (!recordsDir.exists()) {
                recordsDir.mkdirs()
            }
            
            val fileName = "record_${System.currentTimeMillis()}.m4a"
            val filePath = File(recordsDir, fileName).absolutePath
            currentRecordPath = filePath
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(filePath)
                prepare()
                start()
            }
            
            isRecording = true
            recordStartTime = System.currentTimeMillis()
            
            // 更新UI
            binding.fabRecord.setImageResource(android.R.drawable.ic_media_pause)
            binding.tvRecordStatus.visibility = View.VISIBLE
            binding.tvRecordStatus.text = "录音中..."
            
            Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val duration = System.currentTimeMillis() - recordStartTime
            
            // 语音转文字
            currentRecordPath?.let { path ->
                transcribeAudio(path, duration)
            }
            
            isRecording = false
            
            // 更新UI
            binding.fabRecord.setImageResource(android.R.drawable.ic_btn_speak_now)
            binding.tvRecordStatus.visibility = View.GONE
            
            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "停止录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun transcribeAudio(filePath: String, duration: Long) {
        // 由于Android的SpeechRecognizer只能实时识别，不能识别音频文件
        // 这里我们使用一个简化的方案：让用户选择情绪标签
        showEmotionTagDialog(filePath, duration)
    }
    
    private fun showEmotionTagDialog(filePath: String, duration: Long) {
        val emotions = arrayOf("平静", "快乐", "焦虑", "悲伤", "愤怒", "疲惫", "希望")
        
        AlertDialog.Builder(this)
            .setTitle("为这段录音添加标签")
            .setItems(emotions) { dialog, which ->
                val selectedEmotion = emotions[which]
                
                // 保存录音记录
                val record = VoiceRecord(
                    id = UUID.randomUUID().toString(),
                    filePath = filePath,
                    transcription = "录音内容（点击播放收听）",
                    timestamp = System.currentTimeMillis(),
                    emotionTag = selectedEmotion,
                    duration = duration
                )
                
                voiceRecordsManager.saveRecord(record)
                loadVoiceRecords()
                
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun playRecord(record: VoiceRecord) {
        try {
            // 停止之前的播放
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(record.filePath)
                prepare()
                start()
                
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
            
            Toast.makeText(this, "播放中...", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "播放失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun confirmDelete(record: VoiceRecord) {
        AlertDialog.Builder(this)
            .setTitle("删除录音")
            .setMessage("确定要删除这段录音吗？")
            .setPositiveButton("删除") { dialog, _ ->
                deleteRecord(record)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun deleteRecord(record: VoiceRecord) {
        // 删除文件
        File(record.filePath).delete()
        
        // 从数据库删除
        voiceRecordsManager.deleteRecord(record.id)
        
        // 刷新列表
        loadVoiceRecords()
        
        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
    }
    
    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要权限才能录音", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // 停止播放
        mediaPlayer?.release()
        mediaPlayer = null
        
        // 如果正在录音，停止录音
        if (isRecording) {
            stopRecording()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
        speechRecognizer?.destroy()
    }
}

