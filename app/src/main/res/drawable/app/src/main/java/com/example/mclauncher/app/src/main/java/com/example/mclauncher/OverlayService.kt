package com.example.mclauncher

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null

    override fun onBind(i: Intent?): IBinder? = null

    private fun params(x: Int, y: Int) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        if (bubble != null) return START_STICKY
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val lp = params(20, 300)
        val b = TextView(this).apply {
            text = "M"; textSize = 20f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(36, 24, 36, 24)
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#CC3C8527")) }
        }
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        b.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = lp.x; oy = lp.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - sx).toInt(); val dy = (e.rawY - sy).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    lp.x = ox + dx; lp.y = oy + dy; wm.updateViewLayout(b, lp)
                }
                MotionEvent.ACTION_UP -> if (!moved) togglePanel(lp.x + 130, lp.y)
            }
            true
        }
        wm.addView(b, lp); bubble = b
        return START_STICKY
    }

    private fun togglePanel(x: Int, y: Int) {
        if (panel != null) { wm.removeView(panel); panel = null; return }
        val p = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 24)
            background = GradientDrawable().apply { cornerRadius = 24f; setColor(Color.parseColor("#E6202020")) }
        }
        fun item(t: String, a: () -> Unit) = p.addView(Button(this).apply { text = t; setOnClickListener { a() } })
        item("Открыть Minecraft") {
            packageManager.getLaunchIntentForPackage(MC)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
        item("Импорт аддона / пака") {
            startActivity(Intent(this, PickerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        item("Закрыть меню") { togglePanel(0, 0) }
        item("Выключить оверлей") { stopSelf() }
        wm.addView(p, params(x, y)); panel = p
    }

    override fun onDestroy() {
        bubble?.let { wm.removeView(it) }; panel?.let { wm.removeView(it) }
        super.onDestroy()
    }
}
