package com.taximeter.pro.ui.compteur

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pub.devrel.easypermissions.EasyPermissions
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentCompteurBinding
import com.taximeter.pro.service.LocationTrackingService
import com.taximeter.pro.viewmodel.TaxiMeterViewModel
import com.taximeter.pro.utils.NotificationHelper

class CompteurFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCompteurBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaxiMeterViewModel by activityViewModels()

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

        setupObservers()
        setupClickListeners()
        checkLocationPermissions()
    }

    private fun setupObservers() {
        viewModel.fare.observe(viewLifecycleOwner) { fare ->
            binding.tvFare.text = String.format("%.1f", fare)
        }

        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            binding.tvDistance.text = String.format("%.1f", distance)
        }

        viewModel.timeInSeconds.observe(viewLifecycleOwner) { seconds ->
            val minutes = seconds / 60
            val secs = seconds % 60
            binding.tvTime.text = String.format("%02d%02d", minutes, secs)
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
            NotificationHelper.showTripEndNotification(
                requireContext(),
                viewModel.fare.value ?: 0.0,
                viewModel.distance.value ?: 0.0,
                viewModel.timeInSeconds.value ?: 0
            )
        }
    }

    private fun updateUIForRunningState(isRunning: Boolean) {
        binding.btnStart.text = if (isRunning) "PAUSE" else "DÉMARRER"
        binding.indicatorActif.setBackgroundResource(
            if (isRunning) R.drawable.indicator_active else R.drawable.indicator_inactive
        )
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
