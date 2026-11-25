package com.lunikapp.manette_batterie

import android.os.Build
import android.view.InputDevice

object Manettes {
    data class Etat(val nom: String, val niveau: Int, val statut: Int, val supporte: Boolean)

    fun lire(): List<Etat> {
        val liste = mutableListOf<Etat>()
        for (id in InputDevice.getDeviceIds()) {
            val d = InputDevice.getDevice(id) ?: continue
            val s = d.sources
            val pad = (s and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (s and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
            if (!pad || d.isVirtual) continue
            var niveau = -1
            var statut = 0
            var supporte = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val b = d.batteryState
                if (b.isPresent) {
                    supporte = true
                    val c = b.capacity
                    if (!c.isNaN()) niveau = Math.round(c * 100)
                    statut = b.status
                }
            }
            liste.add(Etat(d.name, niveau, statut, supporte))
        }
        return liste
    }

    fun enMap(e: Etat): Map<String, Any?> = mapOf(
        "name" to e.nom, "level" to e.niveau,
        "status" to e.statut, "supported" to e.supporte
    )
}
