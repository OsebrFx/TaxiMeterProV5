package com.taximeter.pro.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

        // Paint pour le texte style digital avec police monospace
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            textSize = (height - paddingTop - paddingBottom) * 0.7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f  // Espacement entre caractères
        }

        val text = when (displayType) {
            DisplayType.MONEY -> {
                val formatted = String.format(Locale.US, "%.2f", currentValue)
                "$formatted DH"
            }
            DisplayType.TIME -> {
                val totalSeconds = currentValue.toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
            DisplayType.DISTANCE -> {
                String.format(Locale.US, "%.2f", currentValue)
            }
        }

        // Centrer le texte verticalement et horizontalement
        val x = width / 2f
        val y = height / 2f + (textPaint.textSize / 3f)

        canvas.drawText(text, x, y, textPaint)
    }

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
