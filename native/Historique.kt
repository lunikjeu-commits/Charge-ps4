package com.lunikapp.manette_batterie

import android.content.Context
import android.content.SharedPreferences

object Historique {
    data class Point(val t: Long, val n: Int)

    // DualShock 4 : 1000 mAh, 4 à 8 h avec vibreur. Sans vibreur (CODM) : départ à 8 h,
    // puis l'app apprend la vraie vitesse de décharge.
    private const val AUTONOMIE_DEFAUT_H = 8.0
    private const val TAUX_DEFAUT = 100.0 / AUTONOMIE_DEFAUT_H
    private const val TAUX_MIN = 8.0 // ≈ 12 h
    private const val TAUX_MAX = 40.0 // ≈ 2,5 h (batterie usée)
    private const val CLE_TAUX = "taux2"

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
            if (pts.isNotEmpty()) cloturer()
            return
        }
        val d = pts.lastOrNull()
        if (d != null && niveau != d.n && (niveau > d.n || now - d.t > 3 * 3_600_000L)) cloturer()
        if (pts.isEmpty() || pts.last().n != niveau) {
            pts.add(Point(now, niveau))
            if (pts.size > 300) pts.removeAt(0)
            sauver()
        }
    }

    @Synchronized
    fun points(): List<Point> = pts.toList()

    // Durée restante en minutes
    @Synchronized
    fun resteMinutes(): Int? {
        val d = pts.lastOrNull() ?: return null
        val t = taux()
        var niveau = d.n.toDouble()
        // Le niveau affiché est le milieu d'un palier de 10 % : on le fait
        // descendre en continu entre deux paliers (dès qu'un passage est connu)
        if (pts.size >= 2 && d.n < 100) {
            val ecoule = (System.currentTimeMillis() - d.t) / 3_600_000.0
            niveau = (d.n + 5 - t * ecoule).coerceIn(d.n - 5.0, d.n + 5.0)
        }
        return Math.round(niveau / t * 60).toInt().coerceAtLeast(1)
    }

    fun formater(min: Int) =
        if (min >= 60) "${min / 60} h ${"%02d".format(min % 60)}" else "$min min"

    // Vitesse mesurée (% par heure) et nombre de paliers de 10 % observés
    private fun mesure(): Pair<Double, Double>? {
        if (pts.size < 3) return null
        val ref = pts[1]
        val fin = pts.last()
        val chute = ref.n - fin.n
        val heures = (fin.t - ref.t) / 3_600_000.0
        if (chute <= 0 || heures <= 0) return null
        return Pair(chute / heures, chute / 10.0)
    }

    private fun tauxAppris(): Double {
        val t = sp?.getFloat(CLE_TAUX, 0f) ?: 0f
        return (if (t > 0f) t.toDouble() else TAUX_DEFAUT).coerceIn(TAUX_MIN, TAUX_MAX)
    }

    // Mélange vitesse apprise et vitesse mesurée, selon la quantité observée
    private fun taux(): Double {
        val base = tauxAppris()
        val m = mesure() ?: return base
        val poids = m.second / (m.second + 2.0)
        return (poids * m.first + (1 - poids) * base).coerceIn(TAUX_MIN, TAUX_MAX)
    }

    // Fin de décharge : on retient la vitesse si au moins 30 % ont été observés
    private fun apprendre() {
        val m = mesure() ?: return
        if (m.second < 3.0) return
        val ancien = sp?.getFloat(CLE_TAUX, 0f) ?: 0f
        val nouveau = if (ancien > 0f) 0.6 * ancien + 0.4 * m.first else m.first
        sp?.edit()?.putFloat(CLE_TAUX, nouveau.coerceIn(TAUX_MIN, TAUX_MAX).toFloat())?.apply()
    }

    private fun cloturer() {
        apprendre()
        pts.clear()
        sauver()
    }

    private fun sauver() {
        sp?.edit()?.putString("pts", pts.joinToString(";") { "${it.t}:${it.n}" })?.apply()
    }
}
