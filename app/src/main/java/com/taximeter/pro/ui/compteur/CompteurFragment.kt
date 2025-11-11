package com.taximeter.pro.ui.compteur

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pub.devrel.easypermissions.EasyPermissions
import com.taximeter.pro.MainActivity
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentCompteurBinding
import com.taximeter.pro.service.LocationTrackingService
import com.taximeter.pro.viewmodel.TaxiMeterViewModel
import com.taximeter.pro.utils.NotificationHelper
import com.taximeter.pro.utils.SevenSegmentDisplay

class CompteurFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCompteurBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaxiMeterViewModel by activityViewModels()
    private lateinit var sevenSegmentDisplay: SevenSegmentDisplay

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompteurBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSevenSegmentDisplay()
        setupObservers()
        setupClickListeners()
        setupHeaderButtons()
        checkLocationPermissions()

        startEntryAnimations()
        startContinuousLedAnimations()
    }

    private fun setupSevenSegmentDisplay() {
        val digit1 = binding.digit1
        val digit2 = binding.digit2
        val digit3 = binding.digit3

        // CRITIQUE: Forcer l'affichage initial
        digit1.visibility = View.VISIBLE
        digit2.visibility = View.VISIBLE
        digit3.visibility = View.VISIBLE
        digit1.text = "2"
        digit2.text = "5"
        digit3.text = "0"
        digit1.bringToFront()
        digit2.bringToFront()
        digit3.bringToFront()

        sevenSegmentDisplay = SevenSegmentDisplay(digit1, digit2, digit3)
        sevenSegmentDisplay.setNumber(2.5)
    }

    private fun startEntryAnimations() {
        binding.header.apply {
            alpha = 0f
            translationY = -100f
            animate().alpha(1f).translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        binding.cardFare.apply {
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
            postDelayed({
                animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 200)
        }

        binding.containerTimeDistance.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate().alpha(1f).translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 400)
        }

        binding.cardStatus.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate().alpha(1f).translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 600)
        }

        binding.containerButtons.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate().alpha(1f).translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 800)
        }
    }

    private fun startContinuousLedAnimations() {
        startLedPulseAnimation(binding.indicatorGps)
        startLedPulseAnimation(binding.indicatorSystem)
    }

    private fun startLedPulseAnimation(view: View) {
        val pulseAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.6f, 1f)
        pulseAnimator.duration = 1500
        pulseAnimator.repeatCount = ValueAnimator.INFINITE
        pulseAnimator.interpolator = AccelerateDecelerateInterpolator()
        pulseAnimator.start()
    }

    private fun setupHeaderButtons() {
        binding.btnMenu?.setOnClickListener {
            animateClick(it)
            (activity as? MainActivity)?.openDrawer()
        }

        binding.btnInfo?.setOnClickListener {
            animateClick(it)
            showInfoDialog()
        }
    }

    private fun animateClick(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun showInfoDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("À propos")
            .setMessage(
                """
                TaxiMeter Pro v1.0

                Application de compteur de taxi professionnel.

                Tarifs:
                • Prise en charge: 2.5 DH
                • Prix par km: 1.5 DH
                • Prix par minute: 0.5 DH

                Développé par Salah Eddine
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }

    private fun setupObservers() {
        viewModel.fare.observe(viewLifecycleOwner) { fare ->
            sevenSegmentDisplay.setNumber(fare)
            animateDisplayFlash(binding.cardFare)
        }

        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            animateTextChangeWithOdometer(binding.tvDistance, distance)
        }

        viewModel.timeInSeconds.observe(viewLifecycleOwner) { seconds ->
            val minutes = seconds / 60
            val secs = seconds % 60
            animateTextChange(binding.tvTime, String.format("%02d:%02d", minutes, secs))
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            updateUIForRunningState(isRunning)
        }
    }

    private fun animateDisplayFlash(view: View) {
        view.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    private fun animateTextChangeWithOdometer(textView: TextView, newValue: Double) {
        val currentValue = textView.text.toString().toDoubleOrNull() ?: 0.0

        ValueAnimator.ofFloat(currentValue.toFloat(), newValue.toFloat()).apply {
            duration = 350
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                textView.text = String.format("%.1f", value)
            }
            start()
        }

        textView.animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(70)
            .withEndAction {
                textView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(70)
                    .start()
            }
            .start()
    }

    private fun animateTextChange(textView: TextView, newText: String) {
        textView.animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(70)
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(70)
                    .start()
            }
            .start()
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener {
            animateButtonPress(it) {
                if (viewModel.isRunning.value == true) {
                    viewModel.pauseTrip()
                    stopLocationService()
                } else {
                    if (hasLocationPermissions()) {
                        viewModel.startTrip()
                        startLocationService()
                    } else {
                        requestLocationPermissions()
                    }
                }
            }
        }

        binding.btnReset.setOnClickListener {
            animateButtonPress(it) {
                viewModel.resetTrip()
                stopLocationService()

                binding.cardFare.animate()
                    .rotationY(90f)
                    .setDuration(200)
                    .withEndAction {
                        sevenSegmentDisplay.reset()
                        binding.cardFare.rotationY = -90f
                        binding.cardFare.animate()
                            .rotationY(0f)
                            .setDuration(200)
                            .start()
                    }
                    .start()

                NotificationHelper.showTripEndNotification(
                    requireContext(),
                    viewModel.fare.value ?: 2.5,
                    viewModel.distance.value ?: 0.0,
                    viewModel.timeInSeconds.value ?: 0
                )
            }
        }
    }

    private fun animateButtonPress(button: View, action: () -> Unit) {
        button.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        action()
                    }
                    .start()
            }
            .start()
    }

    private fun updateUIForRunningState(isRunning: Boolean) {
        if (isRunning) {
            binding.btnStart.text = "PAUSE"
            binding.btnStart.setIconResource(R.drawable.ic_pause)
            binding.indicatorActif.setBackgroundResource(R.drawable.led_indicator_active)
            startLedPulseAnimation(binding.indicatorActif)
        } else {
            binding.btnStart.text = "DÉMARRER"
            binding.btnStart.setIconResource(R.drawable.ic_play)
            binding.indicatorActif.setBackgroundResource(R.drawable.indicator_inactive)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun requestLocationPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "Cette application nécessite l'accès à la localisation",
            LOCATION_PERMISSION_REQUEST,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun checkLocationPermissions() {
        if (!hasLocationPermissions()) requestLocationPermissions()
    }

    private fun startLocationService() {
        Intent(requireContext(), LocationTrackingService::class.java).also {
            requireContext().startForegroundService(it)
        }
    }

    private fun stopLocationService() {
        Intent(requireContext(), LocationTrackingService::class.java).also {
            requireContext().stopService(it)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
