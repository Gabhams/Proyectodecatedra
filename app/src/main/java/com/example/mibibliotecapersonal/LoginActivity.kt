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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        // Si ya hay una sesión válida, no pedir las credenciales otra vez.
        if (auth.currentUser != null) {
            abrirInicio(auth.currentUser?.displayName.orEmpty(), auth.currentUser?.email.orEmpty())
            return
        }

        val correo = findViewById<EditText>(R.id.etCorreoLogin)
        val contrasena = findViewById<EditText>(R.id.etContrasenaLogin)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val irRegistro = findViewById<TextView>(R.id.tvIrRegistro)

        btnIniciarSesion.setOnClickListener {
            val email = correo.text.toString().trim()
            val password = contrasena.text.toString()
            when {
                email.isEmpty() -> { correo.error = "Ingresa tu correo electrónico"; correo.requestFocus() }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { correo.error = "Ingresa un correo electrónico válido"; correo.requestFocus() }
                password.isEmpty() -> { contrasena.error = "Ingresa tu contraseña"; contrasena.requestFocus() }
                else -> {
                    btnIniciarSesion.isEnabled = false
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            btnIniciarSesion.isEnabled = true
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user == null) {
                                    Toast.makeText(this, "No se pudo recuperar la sesión.", Toast.LENGTH_LONG).show()
                                    return@addOnCompleteListener
                                }
                                db.collection("usuarios").document(user.uid).get()
                                    .addOnSuccessListener { doc ->
                                        val nombre = doc.getString("nombre") ?: user.displayName.orEmpty()
                                        abrirInicio(nombre, user.email.orEmpty())
                                    }
                                    .addOnFailureListener {
                                        // La sesión sigue siendo válida aunque no se pueda leer el perfil.
                                        abrirInicio(user.displayName.orEmpty(), user.email.orEmpty())
                                    }
                            } else {
                                Toast.makeText(this, mensajeAuth(task.exception), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
        irRegistro.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun abrirInicio(nombre: String, correo: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("nombre", nombre)
            putExtra("correo", correo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun mensajeAuth(error: Exception?): String = when {
        error?.message?.contains("password", true) == true -> "Correo o contraseña incorrectos."
        error?.message?.contains("no user", true) == true -> "No existe una cuenta con ese correo."
        error?.message?.contains("network", true) == true -> "No hay conexión. Revisa tu internet e inténtalo de nuevo."
        else -> "No se pudo iniciar sesión. Verifica tus datos y que Email/Contraseña esté habilitado en Firebase."
    }
}
