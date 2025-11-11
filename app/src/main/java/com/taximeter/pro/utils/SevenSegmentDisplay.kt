package com.taximeter.pro.utils

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.taximeter.pro.R

class SevenSegmentDisplay(
    private val digit1: TextView,
    private val digit2: TextView,
    private val digit3: TextView
) {

    init {
        // Apply 7-segment display style to all digits
        applySegmentStyle()
    }

    private fun applySegmentStyle() {
        val digits = listOf(digit1, digit2, digit3)
        digits.forEach { digit ->
            // Use monospace font for digital look
            digit.typeface = Typeface.MONOSPACE
            digit.setTextColor(0xFFFF0000.toInt()) // Bright red

            // Apply glow effect
            digit.setShadowLayer(
                12f,  // radius
                0f,   // dx
                0f,   // dy
                0xFF330000.toInt()  // dark red glow
            )

            // Set letter spacing for authentic display look
            digit.letterSpacing = 0.1f
        }
    }

    fun setNumber(number: Double) {
        // Format with 1 decimal place
        val formatted = String.format("%.1f", number)
        val parts = formatted.split(".")

        // Extract integer and decimal parts
        val integerPart = parts[0].padStart(2, '0').takeLast(2)
        val decimalPart = if (parts.size > 1) parts[1].take(1) else "0"

        // Set text for each digit
        digit1.text = integerPart.getOrNull(0)?.toString() ?: "0"
        digit2.text = integerPart.getOrNull(1)?.toString() ?: "0"
        digit3.text = decimalPart

        // Enhance glow on update
        applyEnhancedGlow()
    }

    private fun applyEnhancedGlow() {
        listOf(digit1, digit2, digit3).forEach { digit ->
            digit.setShadowLayer(
                14f,   // increased radius
                0f,
                0f,
                0xFFFF3333.toInt()  // brighter glow
            )
        }
    }

    fun reset() {
        digit1.text = "0"
        digit2.text = "0"
        digit3.text = "0"
        applySegmentStyle()
    }
}