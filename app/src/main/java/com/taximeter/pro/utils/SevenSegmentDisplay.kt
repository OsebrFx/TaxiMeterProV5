package com.taximeter.pro.utils

import android.view.View
import android.widget.TextView

class SevenSegmentDisplay(
    private val digit1: TextView,
    private val digit2: TextView,
    private val digit3: TextView
) {

    fun setNumber(number: Double) {
        // Formater le nombre avec 1 décimale
        val formatted = String.format("%.1f", number)
        val parts = formatted.split(".")

        // Extraire les chiffres
        val integerPart = parts[0].padStart(2, '0').takeLast(2)
        val decimalPart = if (parts.size > 1) parts[1].take(1) else "0"

        // Afficher les chiffres
        digit1.text = integerPart[0].toString()
        digit2.text = integerPart[1].toString()
        digit3.text = decimalPart

        // Effet de glow
        applyGlowEffect()
    }

    private fun applyGlowEffect() {
        listOf(digit1, digit2, digit3).forEach { digit ->
            digit.setShadowLayer(
                8f,  // radius
                0f,  // dx
                0f,  // dy
                android.graphics.Color.parseColor("#FF0000")  // color
            )
        }
    }

    fun reset() {
        digit1.text = "0"
        digit2.text = "0"
        digit3.text = "0"
        applyGlowEffect()
    }
}