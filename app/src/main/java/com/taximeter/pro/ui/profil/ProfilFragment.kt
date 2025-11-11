package com.taximeter.pro.ui.profil

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.taximeter.pro.databinding.FragmentProfilBinding

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderButtons()

        // Générer le QR code
        generateQRCode()

        // Démarrer les animations
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
            .setTitle("Profil du chauffeur")
            .setMessage("Informations personnelles et QR Code du chauffeur de taxi.")
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }

    private fun startEntryAnimations() {
        // Animation du header
        binding.headerProfil.apply {
            alpha = 0f
            translationY = -100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Animation de la photo de profil avec effet bounce
        binding.frameProfile.apply {
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

                // Animation de rotation subtile
                startProfileRotationAnimation(this)
            }, 200)
        }

        // Animation du nom
        binding.tvDriverName.apply {
            alpha = 0f
            translationY = 30f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

                // Effet de pulsation
                startTextPulseAnimation(this)
            }, 800)
        }

        // Animation du rôle
        binding.tvDriverRole.apply {
            alpha = 0f
            translationY = 20f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 1000)
        }

        // Animation de la card âge
        binding.cardAge.apply {
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

        // Animation de la card permis
        binding.cardPermis.apply {
            alpha = 0f
            translationX = 200f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(800)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 1400)
        }

        // Animation du QR code
        binding.cardQr.apply {
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
            }, 1600)
        }
    }

    private fun startProfileRotationAnimation(view: View) {
        val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 3f, 0f, -3f, 0f)
        rotationAnimator.duration = 3000
        rotationAnimator.repeatCount = ValueAnimator.INFINITE
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()
        rotationAnimator.start()
    }

    private fun startTextPulseAnimation(view: View) {
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

    private fun generateQRCode() {
        val driverInfo = """
            Nom: Salah Eddine
            Âge: 20 ans
            Permis: Type B
            TaxiMeter Pro
        """.trimIndent()

        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(driverInfo, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}