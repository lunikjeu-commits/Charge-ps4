package com.lunikapp.manette_batterie

import android.content.Context
import android.content.SharedPreferences

object Historique {
    data class Point(val t: Long, val n: Int)

    private var sp: SharedPreferences? = null
    private val pts = mutableListOf<Point>()

    @Synchronized
    fun init(c: Context) {
        if (sp != null) return
        val prefs = c.applicationContext.getSharedPreferences("lc_histo", Context.MODE_PRIVATE)
        sp = prefs
        prefs.getString("pts", "")!!.split(";").forEach {
            val p = it.split(":")
            val t = p.getOrNull(0)?.toLongOrNull()
            val n = p.getOrNull(1)?.toIntOrNull()
            if (t != null && n != null) pts.add(Point(t, n))
        }
    }

    @Synchronized
    fun ajouter(niveau: Int, enCharge: Boolean) {
        if (niveau < 0) return
        val now = System.currentTimeMillis()
        if (enCharge) {
            if (pts.isNotEmpty()) { pts.clear(); sauver() }
            return
        }
        val d = pts.lastOrNull()
        if (d != null && niveau != d.n && (niveau > d.n || now - d.t > 3 * 3_600_000L)) pts.clear()
        if (pts.isEmpty() || pts.last().n != niveau) {
            pts.add(Point(now, niveau))
            if (pts.size > 300) pts.removeAt(0)
            sauver()
        }
    }

    @Synchronized
    fun points(): List<Point> = pts.toList()

    // Vitesse mesurée entre le 1er palier franchi et le dernier
    @Synchronized
    fun resteMinutes(): Int? {
        if (pts.size < 3) return null
        val ref = pts[1]
        val fin = pts.last()
        val chute = ref.n - fin.n
        val heures = (fin.t - ref.t) / 3_600_000.0
        if (chute <= 0 || heures <= 0) return null
        return Math.round(fin.n / (chute / heures) * 60).toInt()
    }

    fun formater(min: Int) =
        if (min >= 60) "${min / 60} h ${"%02d".format(min % 60)}" else "$min min"

    private fun sauver() {
        sp?.edit()?.putString("pts", pts.joinToString(";") { "${it.t}:${it.n}" })?.apply()
    }
}
