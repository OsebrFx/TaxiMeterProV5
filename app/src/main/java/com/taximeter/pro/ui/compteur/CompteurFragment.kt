package com.taximeter.pro.ui.compteur

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.BounceInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import pub.devrel.easypermissions.EasyPermissions
import com.taximeter.pro.MainActivity
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentCompteurBinding
import com.taximeter.pro.service.LocationTrackingService
import com.taximeter.pro.viewmodel.TaxiMeterViewModel
import com.taximeter.pro.utils.NotificationHelper
import com.taximeter.pro.ui.views.SevenSegmentDisplay

class CompteurFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCompteurBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaxiMeterViewModel by activityViewModels()
    private lateinit var sevenSegmentMoney: SevenSegmentDisplay
    private lateinit var sevenSegmentTime: SevenSegmentDisplay
    private lateinit var sevenSegmentDistance: SevenSegmentDisplay

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getParcelableExtra<Location>(LocationTrackingService.EXTRA_LOCATION)?.let { location ->
                viewModel.updateLocation(location)
            }
        }
    }

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
        // Initialiser les trois affichages 7-segments
        sevenSegmentMoney = binding.sevenSegmentMoney
        sevenSegmentTime = binding.sevenSegmentTime
        sevenSegmentDistance = binding.sevenSegmentDistance

        // Ensure views are visible and properly initialized
        sevenSegmentMoney.visibility = View.VISIBLE
        sevenSegmentTime.visibility = View.VISIBLE
        sevenSegmentDistance.visibility = View.VISIBLE

        sevenSegmentMoney.post {
            sevenSegmentMoney.setValue(2.5, animate = false)
        }
        sevenSegmentTime.post {
            sevenSegmentTime.setTimeValue(0, animate = false)
        }
        sevenSegmentDistance.post {
            sevenSegmentDistance.setValue(0.0, animate = false)
        }
    }

    private fun startEntryAnimations() {
        // Ensure all elements are visible before animating
        binding.header.visibility = View.VISIBLE
        binding.cardFare.visibility = View.VISIBLE
        binding.containerTimeDistance.visibility = View.VISIBLE
        binding.cardStatus.visibility = View.VISIBLE
        binding.containerButtons.visibility = View.VISIBLE

        // Animation header avec bounce
        binding.header.apply {
            alpha = 0f
            translationY = -120f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }

        // Animation display principal avec effet zoom + rotation
        binding.cardFare.apply {
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            rotationY = 90f
            postDelayed({
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotationY(0f)
                    .setDuration(900)
                    .setInterpolator(OvershootInterpolator(0.8f))
                    .start()
            }, 250)
        }

        // Animation temps/distance avec slide et bounce
        binding.containerTimeDistance.apply {
            alpha = 0f
            translationY = 80f
            scaleX = 0.9f
            scaleY = 0.9f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(BounceInterpolator())
                    .start()
            }, 450)
        }

        // Animation panel LED avec glow
        binding.cardStatus.apply {
            alpha = 0f
            translationY = 60f
            scaleX = 0.95f
            scaleY = 0.95f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(750)
                    .setInterpolator(OvershootInterpolator(0.6f))
                    .start()
            }, 650)
        }

        // Animation boutons avec effet wave
        binding.containerButtons.apply {
            alpha = 0f
            translationY = 70f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(OvershootInterpolator(0.5f))
                    .start()
            }, 850)
        }
    }

    private fun startContinuousLedAnimations() {
        startLedPulseAnimation(binding.indicatorGps)
        startLedPulseAnimation(binding.indicatorSystem)
    }

    private fun startLedPulseAnimation(view: View) {
        // Animation de pulsation réaliste avec scale
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f)
        alphaAnimator.duration = 1800
        alphaAnimator.repeatCount = ValueAnimator.INFINITE
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.start()

        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        scaleXAnimator.duration = 1800
        scaleXAnimator.repeatCount = ValueAnimator.INFINITE
        scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleXAnimator.start()

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
        scaleYAnimator.duration = 1800
        scaleYAnimator.repeatCount = ValueAnimator.INFINITE
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleYAnimator.start()
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
            sevenSegmentMoney.setValue(fare, animate = true)
            animateDisplayFlash(binding.cardFare)
            updateNotificationData()
        }

        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            sevenSegmentDistance.setValue(distance, animate = true)
            updateNotificationData()
        }

        viewModel.timeInSeconds.observe(viewLifecycleOwner) { seconds ->
            sevenSegmentTime.setTimeValue(seconds, animate = true)
            updateNotificationData()
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            updateUIForRunningState(isRunning)
        }
    }

    private fun updateNotificationData() {
        // Envoyer les données au service pour mettre à jour la notification
        if (viewModel.isRunning.value == true) {
            val intent = Intent(LocationTrackingService.ACTION_UPDATE_NOTIFICATION).apply {
                putExtra(LocationTrackingService.EXTRA_FARE, viewModel.fare.value ?: 2.5)
                putExtra(LocationTrackingService.EXTRA_DISTANCE, viewModel.distance.value ?: 0.0)
                putExtra(LocationTrackingService.EXTRA_TIME, viewModel.timeInSeconds.value ?: 0)
            }
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun animateDisplayFlash(view: View) {
        // Animation flash améliorée avec glow
        view.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .alpha(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener {
            animateButtonPress(it) {
                if (viewModel.isRunning.value == true) {
                    // PAUSE - Arrêter le compteur
                    viewModel.pauseTrip()
                    stopLocationService()

                    // Afficher notification PAUSE avec tarif et infos
                    NotificationHelper.showPauseNotification(
                        requireContext(),
                        viewModel.fare.value ?: 2.5,
                        viewModel.distance.value ?: 0.0,
                        viewModel.timeInSeconds.value ?: 0
                    )
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

                // Animation reset améliorée avec flip 3D
                binding.cardFare.animate()
                    .rotationY(90f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(250)
                    .withEndAction {
                        sevenSegmentMoney.setValue(2.5, animate = false)
                        binding.cardFare.rotationY = -90f
                        binding.cardFare.animate()
                            .rotationY(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(250)
                            .setInterpolator(OvershootInterpolator(0.5f))
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
        // Animation bouton réaliste avec effet mécanique
        button.animate()
            .scaleX(0.93f)
            .scaleY(0.93f)
            .alpha(0.85f)
            .setDuration(120)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(120)
                    .setInterpolator(OvershootInterpolator(0.8f))
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
            binding.indicatorActif.setBackgroundResource(R.drawable.led_ultra_realistic_red)
            startLedPulseAnimation(binding.indicatorActif)
        } else {
            binding.btnStart.text = "DÉMARRER"
            binding.btnStart.setIconResource(R.drawable.ic_play)
            binding.indicatorActif.setBackgroundResource(R.drawable.led_ultra_realistic_inactive)
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

    override fun onResume() {
        super.onResume()
        // Register location updates receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationReceiver,
            IntentFilter(LocationTrackingService.ACTION_LOCATION_UPDATE)
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister location updates receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver)
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
