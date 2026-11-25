package com.lunikapp.manette_batterie

import android.os.Build
import android.view.InputDevice
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "lc/manette")
            .setMethodCallHandler { call, result ->
                if (call.method == "getControllers") result.success(readControllers())
                else result.notImplemented()
            }
    }

    private fun readControllers(): List<Map<String, Any?>> {
        val list = mutableListOf<Map<String, Any?>>()
        for (id in InputDevice.getDeviceIds()) {
            val d = InputDevice.getDevice(id) ?: continue
            val s = d.sources
            val pad = (s and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (s and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
            if (!pad || d.isVirtual) continue
            var level = -1
            var status = 0
            var supported = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val b = d.batteryState
                if (b.isPresent) {
                    supported = true
                    val c = b.capacity
                    if (!c.isNaN()) level = Math.round(c * 100)
                    status = b.status
                }
            }
            list.add(mapOf(
                "name" to d.name, "level" to level,
                "status" to status, "supported" to supported
            ))
        }
        return list
    }
}
