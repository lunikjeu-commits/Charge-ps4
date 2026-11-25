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
        const val CANAL_VEILLE = "veille"
        const val CANAL_ALERTE = "alerte"
        const val ID = 1
        const val ID_ALERTE = 2
    }

    private val h = Handler(Looper.getMainLooper())
    private lateinit var nm: NotificationManager
    private var envoye1 = false
    private var envoye2 = false
    private var delai = 5000L

    private val boucle = object : Runnable {
        override fun run() {
            maj()
            h.postDelayed(this, delai)
        }
    }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Historique.init(applicationContext)
        nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CANAL, "Suivi manette", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(
            NotificationChannel(CANAL_VEILLE, "Veille (manette non connectée)",
                NotificationManager.IMPORTANCE_MIN))
        nm.createNotificationChannel(
            NotificationChannel(CANAL_ALERTE, "Alerte batterie faible",
                NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            })
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        actif = true
        startForeground(ID, construire(null, null))
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
        if (e != null) Historique.ajouter(e.niveau, e.statut == 1 || e.statut == 4)
        delai = if (e == null) 10000L else 5000L
        nm.notify(ID, construire(e, Historique.resteMinutes()))
        if (e != null) alertes(e) else nm.cancel(ID_ALERTE)
    }

    private fun construire(e: Manettes.Etat?, reste: Int?): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val niveau = e?.niveau ?: -1
        val titre = when {
            e == null -> "Surveillance de la manette"
            niveau >= 0 -> "${e.nom} : $niveau %"
            else -> "${e.nom} : niveau indisponible"
        }
        val texte = when {
            e == null -> "En attente de la manette"
            e.statut == 1 -> "En charge"
            e.statut == 4 -> "Batterie pleine"
            reste != null -> "Sur batterie · ≈ ${Historique.formater(reste)} restantes"
            else -> "Sur batterie"
        }
        return Notification.Builder(this, if (e == null) CANAL_VEILLE else CANAL)
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
        val s1 = Reglages.seuil1(this)
        val s2 = Reglages.seuil2(this)
        val charge = e.statut == 1 || e.statut == 4
        if (charge || e.niveau > s1 + 5) {
            envoye1 = false
            envoye2 = false
            nm.cancel(ID_ALERTE)
            return
        }
        if (e.niveau <= s2 && !envoye2) {
            envoye2 = true
            envoye1 = true
            alerter(e, "Batterie critique")
        } else if (e.niveau <= s1 && !envoye1) {
            envoye1 = true
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
