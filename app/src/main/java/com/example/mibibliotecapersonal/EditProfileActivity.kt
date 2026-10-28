package com.example.mibibliotecapersonal

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etNombre = findViewById<EditText>(R.id.etNombreEditarPerfil)
        val etCorreo = findViewById<EditText>(R.id.etCorreoEditarPerfil)

        val nombreInicial = intent.getStringExtra("nombre").orEmpty()
        val correoInicial = intent.getStringExtra("correo").orEmpty()

        etNombre.setText(nombreInicial)
        etCorreo.setText(correoInicial)

        findViewById<Button>(R.id.btnGuardarPerfil).setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()

            if (nombre.isBlank()) {
                etNombre.error = "Escribe tu nombre"
                etNombre.requestFocus()
                return@setOnClickListener
            }

            if (correo.isBlank() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
            ) {
                etCorreo.error = "Escribe un correo válido"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            val resultado = intent.apply {
                putExtra("nombre", nombre)
                putExtra("correo", correo)
            }

            setResult(Activity.RESULT_OK, resultado)

            Toast.makeText(
                this,
                "Cambios preparados correctamente",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }

        findViewById<Button>(R.id.btnCancelarEditarPerfil).setOnClickListener {
            finish()
        }
    }
}