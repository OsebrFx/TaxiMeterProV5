package com.taximeter.pro.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import java.util.Locale
import kotlin.math.min

/**
 * Vue personnalisée affichant un nombre avec un style 7-segments ultra réaliste
 * Simule un vrai affichage LED avec segments individuels
 */
class RealisticSevenSegmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Valeur actuelle affichée
    private var currentValue: Double = 2.5
    private var targetValue: Double = 2.5
    private var animator: ValueAnimator? = null

    // Couleurs LED ultra réalistes
    private val activeColor = Color.parseColor("#FF0000")       // Rouge vif
    private val inactiveColor = Color.parseColor("#330000")     // Rouge très sombre
    private val glowColor = Color.parseColor("#FF6666")         // Rouge lumineux pour le glow

    // Paint pour les segments actifs avec effet glow
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
    }

    // Paint pour les segments inactifs
    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inactiveColor
        style = Paint.Style.FILL
    }

    // Paint pour le glow externe
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = glowColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(28f, BlurMaskFilter.Blur.NORMAL)
    }

    // Paint pour l'unité (DH)
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeColor
        textSize = 50f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Nécessaire pour les BlurMaskFilter
    }

    /**
     * Définit la valeur à afficher avec animation
     */
    fun setValue(value: Double, animate: Boolean = true) {
        targetValue = value

        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(currentValue.toFloat(), targetValue.toFloat()).apply {
                duration = 400
                interpolator = DecelerateInterpolator()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Format de la valeur: "XX.X" (utilise Locale.US pour garantir le point décimal)
        val formatted = String.format(Locale.US, "%.1f", currentValue)
        val parts = formatted.split(".")
        val integerPart = parts[0].padStart(2, '0')
        val decimalPart = if (parts.size > 1) parts[1] else "0"

        // Calcul de la taille des digits
        val availableWidth = width - paddingLeft - paddingRight - 180f // Espace pour "DH"
        val digitWidth = availableWidth / 3.5f
        val digitHeight = height - paddingTop - paddingBottom.toFloat()

        var xOffset = paddingLeft.toFloat()
        val yOffset = paddingTop.toFloat()

        // Dessiner les deux premiers digits (partie entière)
        integerPart.forEachIndexed { index, char ->
            // Convertir le caractère en chiffre (ignorer les caractères non-numériques)
            val digit = char.toString().toIntOrNull() ?: 0
            drawDigit(canvas, digit, xOffset, yOffset, digitWidth, digitHeight)
            xOffset += digitWidth + 8f
        }

        // Dessiner le point décimal
        drawDecimalPoint(canvas, xOffset, yOffset, digitHeight)
        xOffset += 20f

        // Dessiner le digit décimal
        val decimalDigit = decimalPart.toIntOrNull() ?: 0
        drawDigit(canvas, decimalDigit, xOffset, yOffset, digitWidth, digitHeight)
        xOffset += digitWidth + 30f

        // Dessiner l'unité "DH"
        canvas.drawText("DH", xOffset, yOffset + digitHeight * 0.6f, unitPaint)
    }

    /**
     * Dessine un digit avec les 7 segments
     */
    private fun drawDigit(canvas: Canvas, digit: Int, x: Float, y: Float, width: Float, height: Float) {
        val segments = getSegmentsForDigit(digit)

        // Dimensions des segments
        val segmentWidth = width * 0.8f
        val segmentHeight = height * 0.06f
        val verticalSegmentWidth = height * 0.06f
        val verticalSegmentHeight = height * 0.42f

        // Positions des segments (a-g)
        val positions = mapOf(
            'a' to SegmentPosition(x + width * 0.1f, y, segmentWidth, segmentHeight, 0f),
            'b' to SegmentPosition(x + width * 0.82f, y + height * 0.05f, verticalSegmentWidth, verticalSegmentHeight, 0f),
            'c' to SegmentPosition(x + width * 0.82f, y + height * 0.53f, verticalSegmentWidth, verticalSegmentHeight, 0f),
            'd' to SegmentPosition(x + width * 0.1f, y + height * 0.94f, segmentWidth, segmentHeight, 0f),
            'e' to SegmentPosition(x + width * 0.02f, y + height * 0.53f, verticalSegmentWidth, verticalSegmentHeight, 0f),
            'f' to SegmentPosition(x + width * 0.02f, y + height * 0.05f, verticalSegmentWidth, verticalSegmentHeight, 0f),
            'g' to SegmentPosition(x + width * 0.1f, y + height * 0.47f, segmentWidth, segmentHeight, 0f)
        )

        // Dessiner chaque segment
        positions.forEach { (segment, pos) ->
            if (segment in segments) {
                // Segment actif avec glow
                drawSegmentWithGlow(canvas, pos, true)
            } else {
                // Segment inactif
                drawSegmentWithGlow(canvas, pos, false)
            }
        }
    }

    /**
     * Dessine un segment avec effet glow
     */
    private fun drawSegmentWithGlow(canvas: Canvas, pos: SegmentPosition, active: Boolean) {
        val path = createSegmentPath(pos)

        if (active) {
            // Dessiner le glow externe
            canvas.drawPath(path, glowPaint)
            // Dessiner le segment actif
            canvas.drawPath(path, activePaint)
        } else {
            // Dessiner le segment inactif
            canvas.drawPath(path, inactivePaint)
        }
    }

    /**
     * Crée le path d'un segment avec forme réaliste (hexagone allongé)
     */
    private fun createSegmentPath(pos: SegmentPosition): Path {
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

    /**
     * Dessine le point décimal
     */
    private fun drawDecimalPoint(canvas: Canvas, x: Float, y: Float, height: Float) {
        val radius = height * 0.06f
        val centerY = y + height * 0.9f

        // Glow
        canvas.drawCircle(x, centerY, radius * 1.8f, glowPaint)
        // Point
        canvas.drawCircle(x, centerY, radius, activePaint)
    }

    /**
     * Retourne les segments à activer pour un digit donné
     */
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

    /**
     * Classe pour représenter la position d'un segment
     */
    private data class SegmentPosition(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 600
        val desiredHeight = 200

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
