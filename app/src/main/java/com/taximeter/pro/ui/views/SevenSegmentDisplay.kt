package com.taximeter.pro.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.taximeter.pro.R
import java.util.Locale
import kotlin.math.min

/**
 * Vue 7-segments 8K ultra réaliste avec effet odomètre
 * Supporte différents types d'affichage: montant, temps, distance
 */
class SevenSegmentDisplay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class DisplayType {
        MONEY,      // Format: 00.00 DH
        TIME,       // Format: 00:00
        DISTANCE    // Format: 00.00
    }

    private var displayType: DisplayType = DisplayType.MONEY
    private var currentValue: Double = 0.0
    private var targetValue: Double = 0.0
    private var animator: ValueAnimator? = null

    // Couleurs LED ultra réalistes
    private val activeColor = Color.parseColor("#FF0000")
    private val inactiveColor = Color.parseColor("#220000")
    private val glowColor = Color.parseColor("#FF4444")

    // Paints pour les segments
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inactiveColor
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = glowColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        textSize = 40f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // Lire les attributs XML
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SevenSegmentDisplay)
            val typeIndex = typedArray.getInt(R.styleable.SevenSegmentDisplay_displayType, 0)
            displayType = DisplayType.values()[typeIndex]

            val initialValueStr = typedArray.getString(R.styleable.SevenSegmentDisplay_initialValue)
            if (initialValueStr != null) {
                currentValue = parseValue(initialValueStr)
                targetValue = currentValue
            }

            typedArray.recycle()
        }
    }

    private fun parseValue(valueStr: String): Double {
        return when (displayType) {
            DisplayType.TIME -> {
                // Format "00:00" -> convertir en secondes
                val parts = valueStr.split(":")
                if (parts.size == 2) {
                    (parts[0].toIntOrNull() ?: 0) * 60.0 + (parts[1].toIntOrNull() ?: 0)
                } else 0.0
            }
            else -> valueStr.toDoubleOrNull() ?: 0.0
        }
    }

    /**
     * Définit la valeur avec animation odomètre
     */
    fun setValue(value: Double, animate: Boolean = true) {
        targetValue = value

        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(currentValue.toFloat(), targetValue.toFloat()).apply {
                duration = 600
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    currentValue = (animation.animatedValue as Float).toDouble()
                    invalidate()
                }
                start()
            }
        } else {
            currentValue = targetValue
            invalidate()
        }
    }

    /**
     * Pour le temps (en secondes)
     */
    fun setTimeValue(seconds: Int, animate: Boolean = true) {
        if (displayType == DisplayType.TIME) {
            setValue(seconds.toDouble(), animate)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (displayType) {
            DisplayType.MONEY -> drawMoney(canvas)
            DisplayType.TIME -> drawTime(canvas)
            DisplayType.DISTANCE -> drawDistance(canvas)
        }
    }

    private fun drawMoney(canvas: Canvas) {
        // Format: 00.00 DH
        val formatted = String.format(Locale.US, "%05.2f", currentValue)
        val parts = formatted.split(".")
        val intPart = parts[0].padStart(2, '0')
        val decPart = if (parts.size > 1) parts[1] else "00"

        val availableWidth = width - paddingLeft - paddingRight - 120f
        val digitWidth = availableWidth / 5f
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat()
        val yOffset = paddingTop.toFloat()

        // Dessiner partie entière
        intPart.forEach { char ->
            drawDigit(canvas, char.toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }

        // Point décimal
        drawDecimalPoint(canvas, xOffset, yOffset, digitHeight)
        xOffset += 16f

        // Partie décimale
        decPart.forEach { char ->
            drawDigit(canvas, char.toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }

        // Unité DH
        xOffset += 20f
        canvas.drawText("DH", xOffset, yOffset + digitHeight * 0.6f, unitPaint)
    }

    private fun drawTime(canvas: Canvas) {
        // Format: 00:00
        val totalSeconds = currentValue.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val formatted = String.format(Locale.US, "%02d%02d", minutes, seconds)

        val availableWidth = width - paddingLeft - paddingRight.toFloat()
        val digitWidth = availableWidth / 4.5f
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat()
        val yOffset = paddingTop.toFloat()

        // Minutes
        for (i in 0..1) {
            drawDigit(canvas, formatted[i].toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }

        // Deux points
        drawColon(canvas, xOffset, yOffset, digitHeight)
        xOffset += 16f

        // Secondes
        for (i in 2..3) {
            drawDigit(canvas, formatted[i].toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }
    }

    private fun drawDistance(canvas: Canvas) {
        // Format: 00.00
        val formatted = String.format(Locale.US, "%05.2f", currentValue)
        val parts = formatted.split(".")
        val intPart = parts[0].padStart(2, '0')
        val decPart = if (parts.size > 1) parts[1] else "00"

        val availableWidth = width - paddingLeft - paddingRight.toFloat()
        val digitWidth = availableWidth / 5f
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat()
        val yOffset = paddingTop.toFloat()

        // Partie entière
        intPart.forEach { char ->
            drawDigit(canvas, char.toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }

        // Point décimal
        drawDecimalPoint(canvas, xOffset, yOffset, digitHeight)
        xOffset += 16f

        // Partie décimale
        decPart.forEach { char ->
            drawDigit(canvas, char.toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 6f
        }
    }

    private fun drawDigit(canvas: Canvas, digit: Int, x: Float, y: Float, width: Float, height: Float) {
        val segments = getSegmentsForDigit(digit)

        val segmentWidth = width * 0.75f
        val segmentHeight = height * 0.05f
        val verticalWidth = height * 0.05f
        val verticalHeight = height * 0.42f

        val positions = mapOf(
            'a' to SegmentPos(x + width * 0.12f, y, segmentWidth, segmentHeight),
            'b' to SegmentPos(x + width * 0.8f, y + height * 0.05f, verticalWidth, verticalHeight),
            'c' to SegmentPos(x + width * 0.8f, y + height * 0.53f, verticalWidth, verticalHeight),
            'd' to SegmentPos(x + width * 0.12f, y + height * 0.95f, segmentWidth, segmentHeight),
            'e' to SegmentPos(x + width * 0.05f, y + height * 0.53f, verticalWidth, verticalHeight),
            'f' to SegmentPos(x + width * 0.05f, y + height * 0.05f, verticalWidth, verticalHeight),
            'g' to SegmentPos(x + width * 0.12f, y + height * 0.475f, segmentWidth, segmentHeight)
        )

        positions.forEach { (seg, pos) ->
            drawSegment(canvas, pos, seg in segments)
        }
    }

    private fun drawSegment(canvas: Canvas, pos: SegmentPos, active: Boolean) {
        val path = createSegmentPath(pos)

        if (active) {
            canvas.drawPath(path, glowPaint)
            canvas.drawPath(path, activePaint)
        } else {
            canvas.drawPath(path, inactivePaint)
        }
    }

    private fun createSegmentPath(pos: SegmentPos): Path {
        val path = Path()

        if (pos.width > pos.height) {
            // Segment horizontal
            val indent = pos.height * 0.3f
            path.moveTo(pos.x + indent, pos.y)
            path.lineTo(pos.x + pos.width - indent, pos.y)
            path.lineTo(pos.x + pos.width, pos.y + pos.height / 2)
            path.lineTo(pos.x + pos.width - indent, pos.y + pos.height)
            path.lineTo(pos.x + indent, pos.y + pos.height)
            path.lineTo(pos.x, pos.y + pos.height / 2)
            path.close()
        } else {
            // Segment vertical
            val indent = pos.width * 0.3f
            path.moveTo(pos.x + pos.width / 2, pos.y)
            path.lineTo(pos.x + pos.width, pos.y + indent)
            path.lineTo(pos.x + pos.width, pos.y + pos.height - indent)
            path.lineTo(pos.x + pos.width / 2, pos.y + pos.height)
            path.lineTo(pos.x, pos.y + pos.height - indent)
            path.lineTo(pos.x, pos.y + indent)
            path.close()
        }

        return path
    }

    private fun drawDecimalPoint(canvas: Canvas, x: Float, y: Float, height: Float) {
        val radius = height * 0.05f
        val centerY = y + height * 0.9f
        canvas.drawCircle(x, centerY, radius * 1.5f, glowPaint)
        canvas.drawCircle(x, centerY, radius, activePaint)
    }

    private fun drawColon(canvas: Canvas, x: Float, y: Float, height: Float) {
        val radius = height * 0.05f
        val upperY = y + height * 0.35f
        val lowerY = y + height * 0.65f

        canvas.drawCircle(x, upperY, radius * 1.5f, glowPaint)
        canvas.drawCircle(x, upperY, radius, activePaint)
        canvas.drawCircle(x, lowerY, radius * 1.5f, glowPaint)
        canvas.drawCircle(x, lowerY, radius, activePaint)
    }

    private fun getSegmentsForDigit(digit: Int): Set<Char> {
        return when (digit) {
            0 -> setOf('a', 'b', 'c', 'd', 'e', 'f')
            1 -> setOf('b', 'c')
            2 -> setOf('a', 'b', 'd', 'e', 'g')
            3 -> setOf('a', 'b', 'c', 'd', 'g')
            4 -> setOf('b', 'c', 'f', 'g')
            5 -> setOf('a', 'c', 'd', 'f', 'g')
            6 -> setOf('a', 'c', 'd', 'e', 'f', 'g')
            7 -> setOf('a', 'b', 'c')
            8 -> setOf('a', 'b', 'c', 'd', 'e', 'f', 'g')
            9 -> setOf('a', 'b', 'c', 'd', 'f', 'g')
            else -> emptySet()
        }
    }

    private data class SegmentPos(val x: Float, val y: Float, val width: Float, val height: Float)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 600
        val desiredHeight = 180

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}
