package com.lunikapp.manette_batterie

import android.content.Context

object Reglages {
    private fun sp(c: Context) =
        c.applicationContext.getSharedPreferences("lc_reglages", Context.MODE_PRIVATE)

    fun seuil1(c: Context) = sp(c).getInt("seuil1", 20)
    fun seuil2(c: Context) = sp(c).getInt("seuil2", 10)
    fun auto(c: Context) = sp(c).getBoolean("auto", true)

    fun lire(c: Context): Map<String, Any> =
        mapOf("seuil1" to seuil1(c), "seuil2" to seuil2(c), "auto" to auto(c))

    fun ecrire(c: Context, m: Map<*, *>) {
        val e = sp(c).edit()
        (m["seuil1"] as? Number)?.let { e.putInt("seuil1", it.toInt()) }
        (m["seuil2"] as? Number)?.let { e.putInt("seuil2", it.toInt()) }
        (m["auto"] as? Boolean)?.let { e.putBoolean("auto", it) }
        e.apply()
    }
}
