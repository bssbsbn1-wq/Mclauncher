package com.example.mclauncher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

const val MC = "com.mojang.minecraftpe"

fun Activity.launchMinecraft() {
    val i = packageManager.getLaunchIntentForPackage(MC)
    if (i != null) startActivity(i)
    else Toast.makeText(this, "Minecraft не установлен", Toast.LENGTH_SHORT).show()
}

class MainActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        val play = Button(this).apply {
            text = "Запустить Minecraft"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(context, "Выдай разрешение «Поверх других приложений»", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    startService(Intent(this@MainActivity, OverlayService::class.java))
                    launchMinecraft()
                }
            }
        }
        root.addView(play)
        setContentView(root)
    }
}
