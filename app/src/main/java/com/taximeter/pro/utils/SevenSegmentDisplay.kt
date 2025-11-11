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

    // Map pour simuler les caractères 7-segment
    private val segmentMap = mapOf(
        '0' to "0",
        '1' to "1",
        '2' to "2",
        '3' to "3",
        '4' to "4",
        '5' to "5",
        '6' to "6",
        '7' to "7",
        '8' to "8",
        '9' to "9"
    )

    init {
        applySegmentStyle()
    }

    private fun applySegmentStyle() {
        val digits = listOf(digit1, digit2, digit3)
        digits.forEach { digit ->
            // Police monospace pour look digital authentique
            digit.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

            // Couleur rouge vif LED
            digit.setTextColor(0xFFFF0000.toInt())

            // Effet de glow multi-couches pour réalisme maximal
            digit.setShadowLayer(
                24f,  // radius externe
                0f,   // dx
                0f,   // dy
                0xFFFF0000.toInt()  // couleur glow rouge
            )

            // Espacement optimal pour affichage 7-segment
            digit.letterSpacing = 0f

            // Rendre les digits visibles
            digit.alpha = 1f
        }
    }

    fun setNumber(number: Double) {
        // Animation odomètre fluide
        animateOdometer(currentValue, number)
        currentValue = number
    }

    private fun animateOdometer(from: Double, to: Double) {
        val animator = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
        animator.duration = 350
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

        // S'assurer que les digits sont visibles
        digit1.visibility = android.view.View.VISIBLE
        digit2.visibility = android.view.View.VISIBLE
        digit3.visibility = android.view.View.VISIBLE

        // Effet de flash intense lors du changement
        applyFlashEffect()
    }

    private fun applyFlashEffect() {
        listOf(digit1, digit2, digit3).forEach { digit ->
            // Flash ultra brillant
            digit.setShadowLayer(
                32f,
                0f,
                0f,
                0xFFFF3333.toInt()
            )

            // Retour au glow normal
            digit.postDelayed({
                digit.setShadowLayer(
                    24f,
                    0f,
                    0f,
                    0xFFFF0000.toInt()
                )
            }, 120)
        }
    }

    fun reset() {
        animateOdometer(currentValue, 2.5)
        currentValue = 2.5
    }
}