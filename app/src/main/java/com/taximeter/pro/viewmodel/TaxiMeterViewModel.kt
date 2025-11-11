package com.taximeter.pro.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaxiMeterViewModel : ViewModel() {

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _distance = MutableLiveData(0.0)
    val distance: LiveData<Double> = _distance

    private val _timeInSeconds = MutableLiveData(0)
    val timeInSeconds: LiveData<Int> = _timeInSeconds

    private val _fare = MutableLiveData(BASE_FARE) // Initialiser avec le tarif de base
    val fare: LiveData<Double> = _fare

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> = _currentLocation

    private var lastLocation: Location? = null
    private var timerJob: Job? = null

    companion object {
        const val BASE_FARE = 2.5
        const val PRICE_PER_KM = 1.5
        const val PRICE_PER_MINUTE = 0.5
    }

    fun startTrip() {
        _isRunning.value = true
        lastLocation = null // Reset pour préparer nouveau trajet
        startTimer()
    }

    fun pauseTrip() {
        _isRunning.value = false
        stopTimer()
    }

    fun resetTrip() {
        _isRunning.value = false
        stopTimer()
        _distance.value = 0.0
        _timeInSeconds.value = 0
        _fare.value = BASE_FARE // Réinitialiser au tarif de base
        lastLocation = null
    }

    fun updateLocation(location: Location) {
        _currentLocation.value = location

        // Seulement calculer la distance si le compteur est en marche
        if (_isRunning.value == true) {
            // Filtrer les localisations inexactes (précision > 50m)
            if (location.hasAccuracy() && location.accuracy > 50f) {
                return
            }

            lastLocation?.let { last ->
                val distanceInMeters = last.distanceTo(location)

                // Filtrer les sauts irréalistes (> 500m en 5 secondes = 360 km/h)
                if (distanceInMeters > 500f) {
                    lastLocation = location
                    return
                }

                // Ignorer les très petits mouvements (< 1m) pour éviter le bruit GPS
                if (distanceInMeters >= 1f) {
                    val distanceInKm = distanceInMeters / 1000.0
                    _distance.value = (_distance.value ?: 0.0) + distanceInKm
                    calculateFare()
                }
            }

            // Toujours mettre à jour lastLocation pour le prochain calcul
            lastLocation = location
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_isRunning.value == true) {
                delay(1000)
                _timeInSeconds.value = (_timeInSeconds.value ?: 0) + 1
                calculateFare()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun calculateFare() {
        val dist = _distance.value ?: 0.0
        val timeInMinutes = (_timeInSeconds.value ?: 0) / 60.0

        val calculatedFare = BASE_FARE +
                (dist * PRICE_PER_KM) +
                (timeInMinutes * PRICE_PER_MINUTE)

        _fare.value = calculatedFare
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}