package com.lunikapp.manette_batterie

import android.app.*
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class ManetteService : Service() {
    companion object {
        @Volatile var actif = false
        const val CANAL = "suivi"
        const val CANAL_ALERTE = "alerte"
        const val ID = 1
        const val ID_ALERTE = 2
    }

    private val h = Handler(Looper.getMainLooper())
    private lateinit var nm: NotificationManager
    private var envoye20 = false
    private var envoye10 = false

    private val boucle = object : Runnable {
        override fun run() {
            maj()
            h.postDelayed(this, 5000)
        }
    }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CANAL, "Suivi manette", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(
            NotificationChannel(CANAL_ALERTE, "Alerte batterie faible",
                NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            })
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        actif = true
        startForeground(ID, construire(Manettes.lire().firstOrNull()))
        h.removeCallbacks(boucle)
        h.post(boucle)
        return START_STICKY
    }

    override fun onDestroy() {
        actif = false
        h.removeCallbacks(boucle)
        nm.cancel(ID_ALERTE)
        super.onDestroy()
    }

    private fun maj() {
        val l = Manettes.lire()
        val e = l.firstOrNull { it.supporte && it.niveau >= 0 } ?: l.firstOrNull()
        nm.notify(ID, construire(e))
        if (e != null) alertes(e)
    }

    private fun construire(e: Manettes.Etat?): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val niveau = e?.niveau ?: -1
        val titre = when {
            e == null -> "Aucune manette connectée"
            niveau >= 0 -> "${e.nom} : $niveau %"
            else -> "${e.nom} : niveau indisponible"
        }
        val texte = when {
            e == null -> "Allume la manette (touche PS)"
            e.statut == 1 -> "En charge"
            e.statut == 4 -> "Batterie pleine"
            else -> "Sur batterie"
        }
        return Notification.Builder(this, CANAL)
            .setSmallIcon(IconeBatterie.creer(niveau))
            .setContentTitle(titre)
            .setContentText(texte)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pi)
            .build()
    }

    private fun alertes(e: Manettes.Etat) {
        if (e.niveau < 0) return
        val charge = e.statut == 1 || e.statut == 4
        if (charge || e.niveau > 25) {
            envoye20 = false
            envoye10 = false
            nm.cancel(ID_ALERTE)
            return
        }
        if (e.niveau <= 10 && !envoye10) {
            envoye10 = true
            envoye20 = true
            alerter(e, "Batterie critique")
        } else if (e.niveau <= 20 && !envoye20) {
            envoye20 = true
            alerter(e, "Batterie faible")
        }
    }

    private fun alerter(e: Manettes.Etat, titre: String) {
        val n = Notification.Builder(this, CANAL_ALERTE)
            .setSmallIcon(IconeBatterie.creer(e.niveau))
            .setContentTitle("$titre : ${e.niveau} %")
            .setContentText("Pense à recharger ${e.nom}")
            .setAutoCancel(true)
            .build()
        nm.notify(ID_ALERTE, n)
    }
}
