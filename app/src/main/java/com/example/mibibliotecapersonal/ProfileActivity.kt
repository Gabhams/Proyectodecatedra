package com.example.mibibliotecapersonal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivFotoPerfil: ImageView

    private val selectorImagen = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        if (uri != null) {
            try {
                // Permitir que la aplicación siga accediendo a la imagen
                // después de cerrar y volver a abrir la pantalla.
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Mostrar la imagen seleccionada
                ivFotoPerfil.setImageURI(uri)

                // Guardar la referencia local de la imagen
                getSharedPreferences(
                    "perfil_preferencias",
                    MODE_PRIVATE
                ).edit()
                    .putString("foto_perfil_uri", uri.toString())
                    .apply()

                Toast.makeText(
                    this,
                    "Foto de perfil actualizada",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "No se pudo cargar la imagen. Inténtalo de nuevo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Navegación inferior
        val menuInferior =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        menuInferior.selectedItemId = R.id.nav_perfil

        menuInferior.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_inicio -> {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )
                    finish()
                    true
                }

                R.id.nav_prestamos -> {
                    startActivity(
                        Intent(this, LoansActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )
                    finish()
                    true
                }

                R.id.nav_perfil -> true

                else -> false
            }
        }

        // Foto de perfil
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil)

        // Recuperar la foto guardada anteriormente
        val preferencias = getSharedPreferences(
            "perfil_preferencias",
            MODE_PRIVATE
        )

        val fotoGuardada = preferencias.getString(
            "foto_perfil_uri",
            null
        )

        if (fotoGuardada != null) {
            try {
                ivFotoPerfil.setImageURI(Uri.parse(fotoGuardada))
            } catch (e: Exception) {
                // Si la imagen ya no está disponible, se conserva
                // el avatar predeterminado.
            }
        }

        // Seleccionar una foto de la galería
        val btnCambiarFoto = findViewById<Button>(R.id.btnCambiarFoto)

        btnCambiarFoto.setOnClickListener {
            selectorImagen.launch(arrayOf("image/*"))
        }

        // Editar perfil
        val btnEditarPerfil = findViewById<Button>(R.id.btnEditarPerfil)

        btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Botones de sesión y navegación
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // Mostrar el usuario autenticado y completar sus datos desde Firestore.
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        var nombre = intent.getStringExtra("nombre") ?: usuarioActual?.displayName
        var correo = intent.getStringExtra("correo") ?: usuarioActual?.email

        val tvNombrePerfil = findViewById<TextView>(R.id.tvNombrePerfil)
        val tvCorreoPerfil = findViewById<TextView>(R.id.tvCorreoPerfil)
        val tvNombreCuentaPerfil =
            findViewById<TextView>(R.id.tvNombreCuentaPerfil)
        val tvCorreoCuentaPerfil =
            findViewById<TextView>(R.id.tvCorreoCuentaPerfil)

        if (!nombre.isNullOrBlank()) {
            tvNombrePerfil.text = nombre
            tvNombreCuentaPerfil.text = nombre
        }

        if (!correo.isNullOrBlank()) {
            tvCorreoPerfil.text = correo
            tvCorreoCuentaPerfil.text = correo
        }

        // Abrir libros leídos
        val cardLibrosLeidos =
            findViewById<LinearLayout>(R.id.cardLibrosLeidos)

        cardLibrosLeidos.setOnClickListener {
            startActivity(Intent(this, ReadBooksActivity::class.java))
        }

        // Abrir reseñas escritas
        val cardResenasEscritas =
            findViewById<LinearLayout>(R.id.cardResenasEscritas)

        cardResenasEscritas.setOnClickListener {
            startActivity(Intent(this, MyReviewsActivity::class.java))
        }


        usuarioActual?.let { user ->
            FirebaseFirestore.getInstance().collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { documento ->
                    val nombreGuardado = documento.getString("nombre") ?: user.displayName
                    val correoGuardado = documento.getString("correo") ?: user.email
                    if (!nombreGuardado.isNullOrBlank()) {
                        tvNombrePerfil.text = nombreGuardado
                        tvNombreCuentaPerfil.text = nombreGuardado
                    }
                    if (!correoGuardado.isNullOrBlank()) {
                        tvCorreoPerfil.text = correoGuardado
                        tvCorreoCuentaPerfil.text = correoGuardado
                    }
                }
        }

        contarResenas()

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        contarResenas()
    }

    // Número real de reseñas del usuario (sustituye al valor de ejemplo)
    private fun contarResenas() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("resenas")
            .whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { r ->
                findViewById<TextView>(R.id.tvResenasEscritas).text = r.size().toString()
            }
    }
}
