package com.example.dacs3_ticket_booking_app.ui.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class DoughnutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(
        val label: String,
        val value: Double,
        val color: Int
    )

    private var slices: List<Slice> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 40f
        style = Paint.Style.FILL
    }
    
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        textSize = 30f
        style = Paint.Style.FILL
    }

    private val rectF = RectF()
    private var strokeThickness = 48f // default thickness in pixels
    private var centerText: String = ""
    private var centerSubText: String = ""

    init {
        // Convert 18dp to pixels for default stroke width
        val density = resources.displayMetrics.density
        strokeThickness = 24 * density
        paint.strokeWidth = strokeThickness
        
        textPaint.textSize = 18 * density
        subTextPaint.textSize = 12 * density
    }

    fun setData(newSlices: List<Slice>, totalText: String = "", totalSubText: String = "") {
        this.slices = newSlices.filter { it.value > 0 }
        this.centerText = totalText
        this.centerSubText = totalSubText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) {
            // Draw a placeholder gray circle if no data
            paint.color = Color.parseColor("#333333")
            drawDoughnutCircle(canvas)
            
            val yOffset = (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText("Không có dữ liệu", width / 2f, height / 2f - yOffset, textPaint)
            return
        }

        val total = slices.sumOf { it.value }
        if (total == 0.0) return

        // Set up coordinates
        val size = min(width, height)
        val padding = strokeThickness / 2f + 16f
        rectF.set(
            width / 2f - size / 2f + padding,
            height / 2f - size / 2f + padding,
            width / 2f + size / 2f - padding,
            height / 2f + size / 2f - padding
        )

        var startAngle = -90f
        
        // If there's only 1 slice, we don't need gaps or cap overlays
        if (slices.size == 1) {
            val slice = slices[0]
            paint.color = slice.color
            canvas.drawArc(rectF, 0f, 360f, false, paint)
        } else {
            // Draw each slice arc
            for (slice in slices) {
                val sweepAngle = ((slice.value / total) * 360f).toFloat()
                paint.color = slice.color
                // Add a small gap between arcs by drawing slightly shorter sweeps
                val gap = 3f
                if (sweepAngle > gap) {
                    canvas.drawArc(rectF, startAngle + gap / 2f, sweepAngle - gap, false, paint)
                } else {
                    canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
                }
                startAngle += sweepAngle
            }
        }

        // Draw Center Info texts
        if (centerText.isNotEmpty()) {
            val textX = width / 2f
            val textY = height / 2f
            
            if (centerSubText.isNotEmpty()) {
                // Draw title
                val titleY = textY - 8f
                canvas.drawText(centerText, textX, titleY, textPaint)
                
                // Draw subtitle
                val subY = textY + subTextPaint.textSize + 8f
                canvas.drawText(centerSubText, textX, subY, subTextPaint)
            } else {
                val yOffset = (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(centerText, textX, textY - yOffset, textPaint)
            }
        }
    }

    private fun drawDoughnutCircle(canvas: Canvas) {
        val size = min(width, height)
        val padding = strokeThickness / 2f + 16f
        rectF.set(
            width / 2f - size / 2f + padding,
            height / 2f - size / 2f + padding,
            width / 2f + size / 2f - padding,
            height / 2f + size / 2f - padding
        )
        canvas.drawArc(rectF, 0f, 360f, false, paint)
    }
}
