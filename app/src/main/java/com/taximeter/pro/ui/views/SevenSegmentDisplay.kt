package com.taximeter.pro.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.res.ResourcesCompat
import com.taximeter.pro.R
import java.util.Locale
import kotlin.math.min
import kotlin.math.sin

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

    // Animations 8K ultra réalistes
    private var glowAnimator: ValueAnimator? = null
    private var glowIntensity: Float = 1.0f
    private var bootSequenceProgress: Float = 1.0f  // 0.0 = boot en cours, 1.0 = terminé
    private var scanLinePosition: Float = 0f
    private var scanLineAnimator: ValueAnimator? = null
    private var isBooting = false

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

        // Démarrer les animations 8K ultra réalistes automatiquement
        startGlowAnimation()
        startScanLineAnimation()
    }

    /**
     * Animation GLOW PULSANT 8K - Effet LED ultra réaliste
     */
    private fun startGlowAnimation() {
        glowAnimator?.cancel()
        glowAnimator = ValueAnimator.ofFloat(0.7f, 1.0f).apply {
            duration = 1500  // Pulsation lente et hypnotique
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                glowIntensity = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Animation SCAN LINE - Lignes de balayage LCD vintage 8K
     */
    private fun startScanLineAnimation() {
        scanLineAnimator?.cancel()
        scanLineAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000  // Balayage lent et subtil
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                scanLinePosition = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * BOOT SEQUENCE - Animation de démarrage comme vrais displays LCD
     */
    fun startBootSequence() {
        isBooting = true
        bootSequenceProgress = 0f

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000  // 2 secondes de boot
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                bootSequenceProgress = animation.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isBooting = false
                    bootSequenceProgress = 1f
                    invalidate()
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    isBooting = false
                    bootSequenceProgress = 1f
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            start()
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
     * Définit la valeur avec animation odomètre AMÉLIORÉE - effet mécanique ultra réaliste
     */
    fun setValue(value: Double, animate: Boolean = true) {
        targetValue = value

        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(currentValue.toFloat(), targetValue.toFloat()).apply {
                // Duration plus longue pour effet odométrique bien visible
                duration = 1800
                // OvershootInterpolator pour effet mécanique réaliste avec léger rebond
                interpolator = OvershootInterpolator(0.5f)
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

        // ========== EFFET BOOT SEQUENCE ==========
        if (isBooting && bootSequenceProgress < 1f) {
            drawBootSequence(canvas)
            return
        }

        // Charger la police 7-segment personnalisée
        val customTypeface = try {
            ResourcesCompat.getFont(context, R.font.font_dig)
        } catch (e: Exception) {
            // Fallback vers MONOSPACE si la police n'est pas trouvée
            Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
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

        // Calculer la taille de texte optimale pour qu'il reste dans les limites
        val availableWidth = width - paddingLeft - paddingRight - 40f
        val availableHeight = height - paddingTop - paddingBottom - 20f

        var testSize = availableHeight * 0.6f

        // ========== PAINT PRINCIPAL avec GLOW PULSANT 8K ==========
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Couleur dynamique avec pulsation
            val glowedColor = Color.rgb(
                (Color.red(activeColor) * glowIntensity).toInt(),
                (Color.green(activeColor) * glowIntensity).toInt(),
                (Color.blue(activeColor) * glowIntensity).toInt()
            )
            color = glowedColor
            typeface = customTypeface
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
            textSize = testSize
        }

        // Ajuster la taille pour rester dans les limites
        var textWidth = textPaint.measureText(text)
        while (textWidth > availableWidth && testSize > 10f) {
            testSize -= 2f
            textPaint.textSize = testSize
            textWidth = textPaint.measureText(text)
        }

        val x = width / 2f
        val y = height / 2f + (textPaint.textSize / 3f)

        // ========== EFFET OMBRE 3D pour profondeur 8K ==========
        val shadowPaint = Paint(textPaint).apply {
            color = Color.parseColor("#330000")  // Ombre rouge très sombre
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawText(text, x + 4f, y + 4f, shadowPaint)

        // ========== EFFET GLOW EXTERNE ultra réaliste ==========
        val outerGlowPaint = Paint(textPaint).apply {
            color = Color.parseColor("#FF0000")
            alpha = (80 * glowIntensity).toInt()  // Alpha pulsant
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawText(text, x, y, outerGlowPaint)

        // ========== TEXTE PRINCIPAL ==========
        canvas.drawText(text, x, y, textPaint)

        // ========== SCAN LINES effet LCD vintage 8K ==========
        drawScanLines(canvas)
    }

    /**
     * Dessine l'animation de BOOT SEQUENCE comme vrais displays LCD
     */
    private fun drawBootSequence(canvas: Canvas) {
        // Fond noir
        canvas.drawColor(Color.parseColor("#000000"))

        // Tous les segments s'allument progressivement
        val bootPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            alpha = (255 * bootSequenceProgress).toInt()
            textSize = (height * 0.4f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Afficher "8.8.8.8" style test de display
        val bootText = when (displayType) {
            DisplayType.MONEY -> "88.88 DH"
            DisplayType.TIME -> "88:88"
            DisplayType.DISTANCE -> "88.88"
        }

        canvas.drawText(bootText, width / 2f, height / 2f, bootPaint)
    }

    /**
     * Dessine les SCAN LINES pour effet LCD vintage ultra réaliste
     */
    private fun drawScanLines(canvas: Canvas) {
        val scanPaint = Paint().apply {
            color = Color.parseColor("#FF0000")
            alpha = 15  // Très subtil
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        // Ligne de balayage qui se déplace
        val lineY = height * scanLinePosition
        canvas.drawLine(0f, lineY, width.toFloat(), lineY, scanPaint)

        // Lignes horizontales fixes pour effet CRT
        for (i in 0 until height step 4) {
            scanPaint.alpha = 8
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), scanPaint)
        }
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Nettoyer toutes les animations pour éviter les fuites mémoire
        animator?.cancel()
        glowAnimator?.cancel()
        scanLineAnimator?.cancel()
    }
}
