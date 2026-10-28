package com.example.mibibliotecapersonal

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var menuInferior: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navegación inferior
        menuInferior = findViewById(R.id.bottomNavigation)

        menuInferior.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_inicio -> {
                    // Ya estamos en Inicio
                    true
                }

                R.id.nav_prestamos -> {
                    startActivity(Intent(this, LoansActivity::class.java))
                    true
                }

                R.id.nav_perfil -> {
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("nombre", getIntent().getStringExtra("nombre"))
                        putExtra("correo", getIntent().getStringExtra("correo"))
                    }
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // Campana de notificaciones
        findViewById<ImageButton>(R.id.btnNotificaciones)
            .setOnClickListener {
                startActivity(Intent(this, NotificationsActivity::class.java))
            }

        // Buscador principal
        val buscador = findViewById<EditText>(R.id.etBuscarLibro)

        buscador.setOnEditorActionListener { _, actionId, _ ->
            if (
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("consulta", buscador.text.toString().trim())
                startActivity(intent)
                true
            } else {
                false
            }
        }

        // Libros destacados desde Firestore
        cargarLibros()
    }

    // Actualizar la selección cada vez que Inicio vuelve a mostrarse
    override fun onResume() {
        super.onResume()
        menuInferior.selectedItemId = R.id.nav_inicio
    }

    private fun cargarLibros() {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorLibros)

        FirebaseFirestore.getInstance()
            .collection("libros")
            .get()
            .addOnSuccessListener { resultado ->
                contenedor.removeAllViews()

                for (doc in resultado) {
                    val titulo = doc.getString("titulo")?.trim() ?: ""
                    val autor = doc.getString("autor")?.trim() ?: ""
                    val genero = doc.getString("genero")?.trim() ?: ""
                    val sinopsis = doc.getString("sinopsis")?.trim() ?: ""

                    val vista = layoutInflater.inflate(
                        R.layout.item_libro,
                        contenedor,
                        false
                    )

                    vista.findViewById<TextView>(R.id.tvTitulo).text = titulo
                    vista.findViewById<TextView>(R.id.tvAutor).text = autor
                    vista.findViewById<TextView>(R.id.tvGenero).text = genero
                    vista.findViewById<TextView>(R.id.tvSinopsis).text = sinopsis

                    vista.findViewById<Button>(R.id.btnDetalles)
                        .setOnClickListener {
                            abrirDetalles(doc.id, titulo, autor, genero, sinopsis)
                        }

                    contenedor.addView(vista)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "No se pudieron cargar los libros",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun abrirDetalles(
        libroId: String,
        titulo: String,
        autor: String,
        genero: String,
        sinopsis: String
    ) {
        val intent = Intent(this, BookDetailActivity::class.java)

        intent.putExtra("libroId", libroId)
        intent.putExtra("titulo", titulo)
        intent.putExtra("autor", autor)
        intent.putExtra("genero", genero)
        intent.putExtra("sinopsis", sinopsis)

        startActivity(intent)
    }
}