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

    // Couleurs LED ultra réalistes - 8K Ultra Sharp
    private val activeColor = Color.parseColor("#FF0000")      // Rouge vif éclatant
    private val inactiveColor = Color.parseColor("#0D0000")    // Presque noir (très sombre)
    private val glowColor = Color.parseColor("#CC0000")        // Rouge moyen pour glow subtil

    // Paints pour les segments - Qualité 8K ULTRA NET (sans flou)
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        style = Paint.Style.FILL
        isAntiAlias = true
        isDither = true
        isFilterBitmap = true
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inactiveColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Paint pour le glow SUBTIL (très léger uniquement)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AA0000")
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        isAntiAlias = true
        alpha = 180
    }

    // Paint pour texte "DH" - ultra net et clair
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        textSize = 50f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        style = Paint.Style.FILL
        strokeWidth = 0f
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
     * Définit la valeur avec animation odomètre - effet de roulement mécanique
     */
    fun setValue(value: Double, animate: Boolean = true) {
        targetValue = value

        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(currentValue.toFloat(), targetValue.toFloat()).apply {
                // Duration plus longue pour effet odométrique visible
                duration = 1200
                // AccelerateDecelerateInterpolator pour effet mécanique réaliste
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    currentValue = (animation.animatedValue as Float).toDouble()
                    invalidate()
                }
                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationStart(animation: android.animation.Animator) {}
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // Assurer que la valeur finale est exacte
                        currentValue = targetValue
                        invalidate()
                    }
                    override fun onAnimationCancel(animation: android.animation.Animator) {}
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })
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
        // Format: XX.XX DH
        val formatted = String.format(Locale.US, "%.2f", currentValue)
        val parts = formatted.split(".")
        val intPart = if (parts[0].length >= 2) parts[0] else parts[0].padStart(2, '0')
        val decPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"

        // Calculer les dimensions avec meilleur espacement
        val totalWidth = width - paddingLeft - paddingRight.toFloat()
        val digitWidth = totalWidth / 8f  // Plus d'espace total pour "DH" en 7-seg
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat() + digitWidth * 0.1f
        val yOffset = paddingTop.toFloat()

        // Dessiner partie entière (2 digits)
        for (i in 0 until 2) {
            val digit = if (i < intPart.length) intPart[i].toString().toIntOrNull() ?: 0 else 0
            drawDigit(canvas, digit, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f  // Plus d'espacement
        }

        // Point décimal - bien séparé
        xOffset += 8f
        drawDecimalPoint(canvas, xOffset, yOffset, digitHeight)
        xOffset += 32f  // Beaucoup plus d'espace après le point

        // Partie décimale (2 digits)
        for (i in 0 until 2) {
            val digit = if (i < decPart.length) decPart[i].toString().toIntOrNull() ?: 0 else 0
            drawDigit(canvas, digit, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f
        }

        // Espace avant DH
        xOffset += 24f

        // Dessiner "D" et "H" en style 7-segments
        drawLetterD(canvas, xOffset, yOffset, digitWidth * 0.6f, digitHeight)
        xOffset += digitWidth * 0.6f + 8f
        drawLetterH(canvas, xOffset, yOffset, digitWidth * 0.6f, digitHeight)
    }

    private fun drawTime(canvas: Canvas) {
        // Format: 00:00
        val totalSeconds = currentValue.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val formatted = String.format(Locale.US, "%02d%02d", minutes, seconds)

        val totalWidth = width - paddingLeft - paddingRight.toFloat()
        val digitWidth = totalWidth / 5.5f
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat() + digitWidth * 0.1f
        val yOffset = paddingTop.toFloat()

        // Minutes (2 digits)
        for (i in 0..1) {
            drawDigit(canvas, formatted[i].toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f
        }

        // Deux points ":" - bien séparé
        xOffset += 8f
        drawColon(canvas, xOffset, yOffset, digitHeight)
        xOffset += 32f

        // Secondes (2 digits)
        for (i in 2..3) {
            drawDigit(canvas, formatted[i].toString().toIntOrNull() ?: 0, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f
        }
    }

    private fun drawDistance(canvas: Canvas) {
        // Format: XX.XX
        val formatted = String.format(Locale.US, "%.2f", currentValue)
        val parts = formatted.split(".")
        val intPart = if (parts[0].length >= 2) parts[0] else parts[0].padStart(2, '0')
        val decPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"

        val totalWidth = width - paddingLeft - paddingRight.toFloat()
        val digitWidth = totalWidth / 5.5f
        val digitHeight = (height - paddingTop - paddingBottom).toFloat()

        var xOffset = paddingLeft.toFloat() + digitWidth * 0.1f
        val yOffset = paddingTop.toFloat()

        // Partie entière (2 digits)
        for (i in 0 until 2) {
            val digit = if (i < intPart.length) intPart[i].toString().toIntOrNull() ?: 0 else 0
            drawDigit(canvas, digit, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f
        }

        // Point décimal - bien séparé
        xOffset += 8f
        drawDecimalPoint(canvas, xOffset, yOffset, digitHeight)
        xOffset += 32f

        // Partie décimale (2 digits)
        for (i in 0 until 2) {
            val digit = if (i < decPart.length) decPart[i].toString().toIntOrNull() ?: 0 else 0
            drawDigit(canvas, digit, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 16f
        }
    }

    private fun drawDigit(canvas: Canvas, digit: Int, x: Float, y: Float, width: Float, height: Float) {
        val segments = getSegmentsForDigit(digit)

        // Segments 8K ultra nets - épaisseur optimale pour clarté maximale
        val segmentWidth = width * 0.82f
        val segmentHeight = height * 0.10f  // Plus épais pour meilleure visibilité
        val verticalWidth = height * 0.10f   // Plus épais
        val verticalHeight = height * 0.38f

        val positions = mapOf(
            'a' to SegmentPos(x + width * 0.09f, y + height * 0.02f, segmentWidth, segmentHeight),
            'b' to SegmentPos(x + width * 0.82f, y + height * 0.10f, verticalWidth, verticalHeight),
            'c' to SegmentPos(x + width * 0.82f, y + height * 0.52f, verticalWidth, verticalHeight),
            'd' to SegmentPos(x + width * 0.09f, y + height * 0.88f, segmentWidth, segmentHeight),
            'e' to SegmentPos(x + width * 0.02f, y + height * 0.52f, verticalWidth, verticalHeight),
            'f' to SegmentPos(x + width * 0.02f, y + height * 0.10f, verticalWidth, verticalHeight),
            'g' to SegmentPos(x + width * 0.09f, y + height * 0.46f, segmentWidth, segmentHeight)
        )

        positions.forEach { (seg, pos) ->
            drawSegment(canvas, pos, seg in segments)
        }
    }

    private fun drawSegment(canvas: Canvas, pos: SegmentPos, active: Boolean) {
        val path = createSegmentPath(pos)

        if (active) {
            // Dessiner d'abord un léger glow derrière (optionnel et très subtil)
            val glowPath = Path(path)
            canvas.drawPath(glowPath, glowPaint)

            // Puis dessiner le segment net et clair au-dessus
            canvas.drawPath(path, activePaint)
        } else {
            // Segments inactifs: nets sans flou
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
        val radius = height * 0.08f
        val centerY = y + height * 0.88f

        // Glow subtil derrière
        canvas.drawCircle(x, centerY, radius * 1.4f, glowPaint)

        // Point principal net et clair
        canvas.drawCircle(x, centerY, radius, activePaint)
    }

    private fun drawColon(canvas: Canvas, x: Float, y: Float, height: Float) {
        val radius = height * 0.08f
        val upperY = y + height * 0.35f
        val lowerY = y + height * 0.65f

        // Point supérieur - glow subtil puis point net
        canvas.drawCircle(x, upperY, radius * 1.4f, glowPaint)
        canvas.drawCircle(x, upperY, radius, activePaint)

        // Point inférieur - glow subtil puis point net
        canvas.drawCircle(x, lowerY, radius * 1.4f, glowPaint)
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

    // Dessiner la lettre "D" en style 7-segments
    private fun drawLetterD(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        // D = segments a, b, c, d, e, f (comme 0 mais en plus étroit)
        val segments = setOf('a', 'b', 'c', 'd', 'e', 'f')

        val segmentWidth = width * 0.75f
        val segmentHeight = height * 0.10f
        val verticalWidth = height * 0.10f
        val verticalHeight = height * 0.38f

        val positions = mapOf(
            'a' to SegmentPos(x + width * 0.12f, y + height * 0.02f, segmentWidth, segmentHeight),
            'b' to SegmentPos(x + width * 0.8f, y + height * 0.10f, verticalWidth, verticalHeight),
            'c' to SegmentPos(x + width * 0.8f, y + height * 0.52f, verticalWidth, verticalHeight),
            'd' to SegmentPos(x + width * 0.12f, y + height * 0.88f, segmentWidth, segmentHeight),
            'e' to SegmentPos(x + width * 0.05f, y + height * 0.52f, verticalWidth, verticalHeight),
            'f' to SegmentPos(x + width * 0.05f, y + height * 0.10f, verticalWidth, verticalHeight)
        )

        positions.forEach { (seg, pos) ->
            drawSegment(canvas, pos, seg in segments)
        }
    }

    // Dessiner la lettre "H" en style 7-segments
    private fun drawLetterH(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        // H = segments b, c, e, f, g
        val segments = setOf('b', 'c', 'e', 'f', 'g')

        val segmentWidth = width * 0.75f
        val segmentHeight = height * 0.10f
        val verticalWidth = height * 0.10f
        val verticalHeight = height * 0.38f

        val positions = mapOf(
            'b' to SegmentPos(x + width * 0.8f, y + height * 0.10f, verticalWidth, verticalHeight),
            'c' to SegmentPos(x + width * 0.8f, y + height * 0.52f, verticalWidth, verticalHeight),
            'e' to SegmentPos(x + width * 0.05f, y + height * 0.52f, verticalWidth, verticalHeight),
            'f' to SegmentPos(x + width * 0.05f, y + height * 0.10f, verticalWidth, verticalHeight),
            'g' to SegmentPos(x + width * 0.12f, y + height * 0.46f, segmentWidth, segmentHeight)
        )

        positions.forEach { (seg, pos) ->
            drawSegment(canvas, pos, seg in segments)
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
