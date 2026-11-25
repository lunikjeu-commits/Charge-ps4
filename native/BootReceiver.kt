package com.lunikapp.manette_batterie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED && Reglages.auto(c)) {
            try {
                c.startForegroundService(Intent(c, ManetteService::class.java))
            } catch (_: Exception) {
            }
        }
    }
}
