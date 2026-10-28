package com.example.mibibliotecapersonal

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val btnVolver = findViewById<Button>(R.id.btnVolverNotificaciones)

        btnVolver.setOnClickListener {
            finish()
        }
    }
}