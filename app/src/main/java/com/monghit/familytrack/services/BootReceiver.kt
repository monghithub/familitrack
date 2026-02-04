package com.monghit.familytrack.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Timber.d("Boot completed, checking if location service should start")

            CoroutineScope(Dispatchers.IO).launch {
                val isLocationEnabled = settingsRepository.isLocationEnabled.first()
                val isRegistered = settingsRepository.isRegistered.first()

                if (isLocationEnabled && isRegistered) {
                    Timber.d("Starting LocationForegroundService after boot")
                    LocationForegroundService.start(context)
                }
            }
        }
    }
}
