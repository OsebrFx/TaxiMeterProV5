package com.taximeter.pro.ui.carte

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import pub.devrel.easypermissions.EasyPermissions
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentCarteBinding
import com.taximeter.pro.viewmodel.TaxiMeterViewModel

class CarteFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCarteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaxiMeterViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderButtons()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Animations d'entrée
        startEntryAnimations()
    }

    private fun setupHeaderButtons() {
        binding.btnMenu?.setOnClickListener {
            animateClick(it)
            (activity as? com.taximeter.pro.MainActivity)?.openDrawer()
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
            .setTitle("Carte GPS")
            .setMessage("Suivi en temps réel de votre position GPS sur Google Maps.")
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }

    private fun startEntryAnimations() {
        // Animation du header
        binding.headerCarte.apply {
            alpha = 0f
            translationY = -100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Animation du card de statut GPS
        view?.findViewById<View>(R.id.map)?.let { mapView ->
            mapView.alpha = 0f
            mapView.postDelayed({
                mapView.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .start()
            }, 300)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Style de carte sombre pour un look professionnel
        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
            if (success == false) {
                // Si le style personnalisé échoue, utiliser le style normal
                googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        } catch (e: Exception) {
            // Utiliser le style par défaut si le fichier n'existe pas
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        if (hasLocationPermissions()) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = false
                isCompassEnabled = true
                isMapToolbarEnabled = false
            }

            viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                        1000,
                        null
                    )
                }
            }
        } else {
            requestLocationPermissions()
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        onMapReady(googleMap ?: return)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // Handle permission denied
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