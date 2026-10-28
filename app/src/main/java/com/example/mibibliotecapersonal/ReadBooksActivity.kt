package com.example.mibibliotecapersonal

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ReadBooksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_books)

        val btnVolver = findViewById<Button>(R.id.btnVolverLibrosLeidos)

        btnVolver.setOnClickListener {
            finish()
        }
    }
}