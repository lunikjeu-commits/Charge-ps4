package com.lunikapp.manette_batterie

import android.graphics.*
import android.graphics.drawable.Icon

object IconeBatterie {
    fun creer(niveau: Int): Icon {
        val t = 96
        val bmp = Bitmap.createBitmap(t, t, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val blanc = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val trou = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        // Manette
        c.drawRoundRect(RectF(4f, 2f, 92f, 36f), 16f, 16f, blanc)
        c.drawRect(15f, 17f, 33f, 22f, trou)
        c.drawRect(21f, 11f, 27f, 28f, trou)
        c.drawCircle(68f, 14f, 4f, trou)
        c.drawCircle(78f, 24f, 4f, trou)
        // Pourcentage
        val txt = if (niveau >= 0) niveau.toString() else "-"
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            textSize = 60f
        }
        while (p.measureText(txt) > 92f) p.textSize -= 2f
        c.drawText(txt, 48f, 94f, p)
        return Icon.createWithBitmap(bmp)
    }
}
