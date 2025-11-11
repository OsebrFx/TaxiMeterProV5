package com.taximeter.pro.utils

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView

class SevenSegmentDisplay(
    private val digit1: TextView,
    private val digit2: TextView,
    private val digit3: TextView
) {

    private var currentValue: Double = 0.0

    init {
        applySegmentStyle()
    }

    private fun applySegmentStyle() {
        val digits = listOf(digit1, digit2, digit3)
        digits.forEach { digit ->
            // Police monospace pour look digital
            digit.typeface = Typeface.MONOSPACE
            digit.setTextColor(0xFFFF0000.toInt()) // Rouge vif LED

            // Effet de glow ultra réaliste
            digit.setShadowLayer(
                20f,  // radius augmenté
                0f,   // dx
                0f,   // dy
                0xFFFF0000.toInt()  // rouge brillant
            )

            // Espacement des caractères pour look authentique
            digit.letterSpacing = 0.15f
        }
    }

    fun setNumber(number: Double) {
        // Effet d'odomètre avec animation fluide
        animateOdometer(currentValue, number)
        currentValue = number
    }

    private fun animateOdometer(from: Double, to: Double) {
        val animator = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
        animator.duration = 400 // Animation plus rapide
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            updateDigits(value.toDouble())
        }

        animator.start()
    }

    private fun updateDigits(number: Double) {
        // Format avec 1 décimale
        val formatted = String.format("%.1f", number)
        val parts = formatted.split(".")

        // Extraire parties entière et décimale
        val integerPart = parts[0].padStart(2, '0').takeLast(2)
        val decimalPart = if (parts.size > 1) parts[1].take(1) else "0"

        // Définir le texte pour chaque digit
        digit1.text = integerPart.getOrNull(0)?.toString() ?: "0"
        digit2.text = integerPart.getOrNull(1)?.toString() ?: "0"
        digit3.text = decimalPart

        // Effet de flash lors du changement
        applyFlashEffect()
    }

    private fun applyFlashEffect() {
        listOf(digit1, digit2, digit3).forEach { digit ->
            // Flash temporaire plus brillant
            digit.setShadowLayer(
                28f,
                0f,
                0f,
                0xFFFF3333.toInt()
            )

            // Retour au glow normal après 150ms
            digit.postDelayed({
                digit.setShadowLayer(
                    20f,
                    0f,
                    0f,
                    0xFFFF0000.toInt()
                )
            }, 150)
        }
    }

    fun reset() {
        animateOdometer(currentValue, 2.5)
        currentValue = 2.5
    }
}