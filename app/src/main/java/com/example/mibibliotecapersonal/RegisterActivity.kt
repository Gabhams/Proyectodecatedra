package com.example.mibibliotecapersonal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        val nombre = findViewById<EditText>(R.id.etNombreRegistro)
        val correo = findViewById<EditText>(R.id.etCorreoRegistro)
        val contrasena = findViewById<EditText>(R.id.etContrasenaRegistro)
        val confirmarContrasena = findViewById<EditText>(R.id.etConfirmarContrasenaRegistro)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val irLogin = findViewById<TextView>(R.id.tvIrLogin)

        btnCrearCuenta.setOnClickListener {
            val nombreTexto = nombre.text.toString().trim()
            val correoTexto = correo.text.toString().trim()
            val password = contrasena.text.toString()
            val confirmarPassword = confirmarContrasena.text.toString()
            when {
                nombreTexto.isEmpty() -> { nombre.error = "Escribe tu nombre"; nombre.requestFocus() }
                correoTexto.isEmpty() -> { correo.error = "Escribe tu correo electrónico"; correo.requestFocus() }
                !Patterns.EMAIL_ADDRESS.matcher(correoTexto).matches() -> { correo.error = "Escribe un correo válido"; correo.requestFocus() }
                password.length < 6 -> { contrasena.error = "Usa al menos 6 caracteres"; contrasena.requestFocus() }
                password != confirmarPassword -> { confirmarContrasena.error = "Las contraseñas no coinciden"; confirmarContrasena.requestFocus() }
                else -> {
                    btnCrearCuenta.isEnabled = false
                    auth.createUserWithEmailAndPassword(correoTexto, password)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                btnCrearCuenta.isEnabled = true
                                Toast.makeText(this, "No se pudo crear la cuenta. Comprueba si Email/Contraseña está habilitado en Firebase y si el correo ya está registrado.", Toast.LENGTH_LONG).show()
                                return@addOnCompleteListener
                            }
                            val user = auth.currentUser
                            if (user == null) {
                                btnCrearCuenta.isEnabled = true
                                Toast.makeText(this, "La cuenta se creó, pero no se pudo recuperar la sesión.", Toast.LENGTH_LONG).show()
                                return@addOnCompleteListener
                            }
                            val perfil = hashMapOf(
                                "uid" to user.uid,
                                "nombre" to nombreTexto,
                                "correo" to correoTexto,
                                "fechaCreacion" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            db.collection("usuarios").document(user.uid).set(perfil)
                                .addOnSuccessListener {
                                    user.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombreTexto).build())
                                        .addOnCompleteListener {
                                            btnCrearCuenta.isEnabled = true
                                            Toast.makeText(this, "¡Cuenta creada correctamente!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java).apply {
                                                putExtra("nombre", nombreTexto)
                                                putExtra("correo", correoTexto)
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            })
                                            finish()
                                        }
                                }
                                .addOnFailureListener {
                                    btnCrearCuenta.isEnabled = true
                                    Toast.makeText(this, "La cuenta se creó en Authentication, pero no se pudo guardar el perfil en Firestore. Revisa las reglas de seguridad de la colección usuarios.", Toast.LENGTH_LONG).show()
                                }
                        }
                }
            }
        }
        irLogin.setOnClickListener { finish() }
    }
}
