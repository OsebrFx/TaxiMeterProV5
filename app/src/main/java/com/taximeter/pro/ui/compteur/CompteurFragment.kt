package com.taximeter.pro.ui.compteur

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    private fun setupSevenSegmentDisplay() {
        // CORRECTION : Accès aux TextView via binding.root.findViewById
        val digit1 = binding.root.findViewById<TextView>(R.id.digit_1)
        val digit2 = binding.root.findViewById<TextView>(R.id.digit_2)
        val digit3 = binding.root.findViewById<TextView>(R.id.digit_3)

        sevenSegmentDisplay = SevenSegmentDisplay(digit1, digit2, digit3)

        // Afficher le tarif de base dès le début (2.5 DH)
        sevenSegmentDisplay.setNumber(2.5)
    }

    private fun setupHeaderButtons() {
        // Bouton menu pour ouvrir le drawer
        binding.root.findViewById<View>(R.id.btn_menu)?.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        // Bouton info
        binding.root.findViewById<View>(R.id.btn_info)?.setOnClickListener {
            showInfoDialog()
        }
    }

    private fun showInfoDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("À propos")
            .setMessage("""
                TaxiMeter Pro v1.0
                
                Application de compteur de taxi professionnel.
                
                Tarifs:
                • Prise en charge: 2.5 DH
                • Prix par km: 1.5 DH
                • Prix par minute: 0.5 DH
                
                Développé par Salah Eddine
            """.trimIndent())
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }

    private fun setupObservers() {
        viewModel.fare.observe(viewLifecycleOwner) { fare ->
            sevenSegmentDisplay.setNumber(fare)
        }

        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            binding.tvDistance.text = String.format("%.1f", distance)
        }

        viewModel.timeInSeconds.observe(viewLifecycleOwner) { seconds ->
            val minutes = seconds / 60
            val secs = seconds % 60
            binding.tvTime.text = String.format("%02d:%02d", minutes, secs)
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            updateUIForRunningState(isRunning)
        }
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener {
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

        binding.btnReset.setOnClickListener {
            viewModel.resetTrip()
            stopLocationService()
            sevenSegmentDisplay.setNumber(2.5) // Réinitialiser au tarif de base
            NotificationHelper.showTripEndNotification(
                requireContext(),
                viewModel.fare.value ?: 2.5,
                viewModel.distance.value ?: 0.0,
                viewModel.timeInSeconds.value ?: 0
            )
        }
    }

    private fun updateUIForRunningState(isRunning: Boolean) {
        if (isRunning) {
            binding.btnStart.text = "PAUSE"
            binding.btnStart.setIconResource(R.drawable.ic_pause)
            binding.indicatorActif.setBackgroundResource(R.drawable.indicator_active)
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
        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        }
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Permissions accordées
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // Permissions refusées
    }

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