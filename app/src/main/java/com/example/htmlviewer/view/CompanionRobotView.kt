package com.example.htmlviewer.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

/**
 * 陪伴机器人自定义View
 * 类似大白/荒野机器人的可爱形象
 */
class CompanionRobotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var animationProgress = 0f
    private var blinkProgress = 0f
    private var breathProgress = 0f
    private var isSpeaking = false
    
    private val bodyAnimator: ValueAnimator
    private val blinkAnimator: ValueAnimator
    
    init {
        // 身体颜色 - 温暖的白色
        bodyPaint.color = Color.parseColor("#FAFAFA")
        bodyPaint.style = Paint.Style.FILL
        
        // 眼睛颜色 - 深色
        eyePaint.color = Color.parseColor("#424242")
        eyePaint.style = Paint.Style.FILL
        
        // 呼吸动画
        bodyAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                breathProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
        
        // 眨眼动画
        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 200
            startDelay = 3000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                blinkProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val baseRadius = width.coerceAtMost(height) / 3f
        
        // 绘制身体（圆形，带呼吸效果）
        val breathOffset = sin(breathProgress * Math.PI).toFloat() * 3f
        val bodyRadius = baseRadius + breathOffset
        
        // 身体阴影
        bodyPaint.setShadowLayer(8f, 0f, 4f, Color.parseColor("#40000000"))
        canvas.drawCircle(centerX, centerY, bodyRadius, bodyPaint)
        
        // 绘制眼睛
        val eyeY = centerY - baseRadius / 4f
        val eyeSpacing = baseRadius / 2f
        val eyeRadius = baseRadius / 6f
        
        // 根据blinkProgress调整眼睛高度（眨眼效果）
        val eyeHeight = eyeRadius * (1f - blinkProgress)
        
        // 左眼
        val leftEyeX = centerX - eyeSpacing
        if (isSpeaking) {
            // 说话时眼睛变成笑眼
            drawSmileEye(canvas, leftEyeX, eyeY, eyeRadius)
        } else {
            drawNormalEye(canvas, leftEyeX, eyeY, eyeRadius, eyeHeight)
        }
        
        // 右眼
        val rightEyeX = centerX + eyeSpacing
        if (isSpeaking) {
            drawSmileEye(canvas, rightEyeX, eyeY, eyeRadius)
        } else {
            drawNormalEye(canvas, rightEyeX, eyeY, eyeRadius, eyeHeight)
        }
        
        // 绘制嘴巴
        if (isSpeaking) {
            drawSpeakingMouth(canvas, centerX, centerY + baseRadius / 3f, baseRadius / 2f)
        } else {
            drawSmileMouth(canvas, centerX, centerY + baseRadius / 3f, baseRadius / 2.5f)
        }
        
        // 绘制天线（可爱的标志）
        drawAntenna(canvas, centerX, centerY - bodyRadius, baseRadius / 4f)
    }
    
    private fun drawNormalEye(canvas: Canvas, x: Float, y: Float, radius: Float, height: Float) {
        if (height > 0.1f) {
            val rect = RectF(x - radius, y - height, x + radius, y + height)
            canvas.drawOval(rect, eyePaint)
        } else {
            // 完全闭眼时画一条线
            canvas.drawLine(x - radius, y, x + radius, y, eyePaint)
        }
    }
    
    private fun drawSmileEye(canvas: Canvas, x: Float, y: Float, radius: Float) {
        // 笑眼是一个向上弯的弧
        val path = Path()
        path.moveTo(x - radius, y)
        path.quadTo(x, y - radius / 2f, x + radius, y)
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = radius / 3f
        eyePaint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(path, eyePaint)
        eyePaint.style = Paint.Style.FILL
    }
    
    private fun drawSmileMouth(canvas: Canvas, x: Float, y: Float, width: Float) {
        val path = Path()
        path.moveTo(x - width, y)
        path.quadTo(x, y + width / 2f, x + width, y)
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = width / 4f
        eyePaint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(path, eyePaint)
        eyePaint.style = Paint.Style.FILL
    }
    
    private fun drawSpeakingMouth(canvas: Canvas, x: Float, y: Float, size: Float) {
        // 说话时嘴巴张开
        val mouthProgress = sin(breathProgress * Math.PI * 4).toFloat()
        val openAmount = size / 2f * (0.5f + mouthProgress * 0.5f)
        canvas.drawCircle(x, y, openAmount, eyePaint)
    }
    
    private fun drawAntenna(canvas: Canvas, x: Float, y: Float, height: Float) {
        // 天线杆
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = 4f
        canvas.drawLine(x, y, x, y - height, eyePaint)
        
        // 天线球
        eyePaint.style = Paint.Style.FILL
        canvas.drawCircle(x, y - height, height / 3f, eyePaint)
    }
    
    /**
     * 设置说话状态
     */
    fun setSpeaking(speaking: Boolean) {
        if (isSpeaking != speaking) {
            isSpeaking = speaking
            invalidate()
        }
    }
    
    /**
     * 设置情绪状态
     */
    fun setEmotion(emotion: String) {
        when (emotion) {
            "happy" -> {
                bodyPaint.color = Color.parseColor("#FFF9C4")
            }
            "caring" -> {
                bodyPaint.color = Color.parseColor("#E1F5FE")
            }
            "encouraging" -> {
                bodyPaint.color = Color.parseColor("#F3E5F5")
            }
            else -> {
                bodyPaint.color = Color.parseColor("#FAFAFA")
            }
        }
        invalidate()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bodyAnimator.cancel()
        blinkAnimator.cancel()
    }
}

