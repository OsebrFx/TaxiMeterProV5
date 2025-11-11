package com.taximeter.pro

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.taximeter.pro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        setupNavigation()
        setupDrawerMenu()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupDrawerMenu() {
        // Navigation vers les différentes sections
        binding.drawerLayout.findViewById<View>(R.id.menu_accueil)?.setOnClickListener {
            navigateToFragment(R.id.accueilFragment)
        }

        binding.drawerLayout.findViewById<View>(R.id.menu_compteur)?.setOnClickListener {
            navigateToFragment(R.id.compteurFragment)
        }

        binding.drawerLayout.findViewById<View>(R.id.menu_carte)?.setOnClickListener {
            navigateToFragment(R.id.carteFragment)
        }

        binding.drawerLayout.findViewById<View>(R.id.menu_profil)?.setOnClickListener {
            navigateToFragment(R.id.profilFragment)
        }

        binding.drawerLayout.findViewById<View>(R.id.menu_parametres)?.setOnClickListener {
            closeDrawer()
            // Afficher un dialog pour les paramètres
            showSettingsDialog()
        }
    }

    private fun navigateToFragment(fragmentId: Int) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Naviguer vers le fragment
        navController.navigate(fragmentId)

        // Fermer le drawer
        closeDrawer()

        // Mettre à jour la bottom navigation
        binding.bottomNavigation.selectedItemId = fragmentId
    }

    private fun showSettingsDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Paramètres")
            .setMessage("Configuration des tarifs et paramètres de l'application.")
            .setPositiveButton("OK", null)
            .show()
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}