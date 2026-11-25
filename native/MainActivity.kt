package com.lunikapp.manette_batterie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "lc/manette")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "getControllers" -> result.success(Manettes.lire().map { Manettes.enMap(it) })
                    "startService" -> {
                        demanderNotifications()
                        startForegroundService(Intent(this, ManetteService::class.java))
                        result.success(true)
                    }
                    "stopService" -> {
                        stopService(Intent(this, ManetteService::class.java))
                        result.success(true)
                    }
                    "serviceActif" -> result.success(ManetteService.actif)
                    else -> result.notImplemented()
                }
            }
    }

    private fun demanderNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }
}
