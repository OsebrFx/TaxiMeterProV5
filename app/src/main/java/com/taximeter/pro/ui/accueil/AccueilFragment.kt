package com.taximeter.pro.ui.accueil

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentAccueilBinding

class AccueilFragment : Fragment() {

    private var _binding: FragmentAccueilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTrip.setOnClickListener {
            animateButtonPress(it) {
                findNavController().navigate(R.id.compteurFragment)
            }
        }

        // Démarrer les animations d'entrée
        startEntryAnimations()
    }

    private fun startEntryAnimations() {
        // Animation du header
        binding.header.apply {
            alpha = 0f
            translationY = -100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Animation de l'icône taxi avec effet bounce
        binding.cardIcon.apply {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            postDelayed({
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setInterpolator(BounceInterpolator())
                    .start()

                // Animation de rotation continue
                startIconRotationAnimation(this)
            }, 200)
        }

        // Animation du titre
        binding.tvTitle.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

                // Effet de pulsation sur le titre
                startTitlePulseAnimation(this)
            }, 800)
        }

        // Animation du sous-titre
        binding.tvSubtitle.apply {
            alpha = 0f
            translationY = 30f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 1000)
        }

        // Animation de la première card (base fare)
        binding.cardBaseFare.apply {
            alpha = 0f
            translationX = -200f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 1200)
        }

        // Animation des cards tarifs
        binding.containerRates.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 1400)
        }

        // Animation du bouton START
        binding.btnStartTrip.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            postDelayed({
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setInterpolator(BounceInterpolator())
                    .start()

                // Animation de pulsation du bouton
                startButtonPulseAnimation(this)
            }, 1600)
        }
    }

    private fun startIconRotationAnimation(view: View) {
        val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, 0f, -5f, 0f)
        rotationAnimator.duration = 4000
        rotationAnimator.repeatCount = ValueAnimator.INFINITE
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()
        rotationAnimator.start()
    }

    private fun startTitlePulseAnimation(view: View) {
        val scaleAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f)
        scaleAnimator.duration = 2000
        scaleAnimator.repeatCount = ValueAnimator.INFINITE
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f)
        scaleYAnimator.duration = 2000
        scaleYAnimator.repeatCount = ValueAnimator.INFINITE
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()

        scaleAnimator.start()
        scaleYAnimator.start()
    }

    private fun startButtonPulseAnimation(view: View) {
        val scaleAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.03f, 1f)
        scaleAnimator.duration = 1500
        scaleAnimator.repeatCount = ValueAnimator.INFINITE
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.03f, 1f)
        scaleYAnimator.duration = 1500
        scaleYAnimator.repeatCount = ValueAnimator.INFINITE
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()

        scaleAnimator.start()
        scaleYAnimator.start()
    }

    private fun animateButtonPress(button: View, action: () -> Unit) {
        button.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction { action() }
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}