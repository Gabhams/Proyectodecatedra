package com.example.mibibliotecapersonal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MyReviewsActivity : AppCompatActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reviews)

        findViewById<Button>(R.id.btnVolverMisResenas).setOnClickListener { finish() }
        cargarMisResenas()
    }

    private fun cargarMisResenas() {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorMisResenas)
        val tvSin = findViewById<TextView>(R.id.tvSinMisResenas)
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            tvSin.text = "Inicia sesión para ver tus reseñas."
            tvSin.visibility = View.VISIBLE
            return
        }

        db.collection("resenas").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { r ->
                contenedor.removeAllViews()
                val docs = r.documents.sortedByDescending {
                    it.getTimestamp("fecha")?.seconds ?: Long.MAX_VALUE
                }
                tvSin.visibility = if (docs.isEmpty()) View.VISIBLE else View.GONE
                docs.forEach { agregarTarjeta(contenedor, it) }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar tus reseñas.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarTarjeta(contenedor: LinearLayout, doc: DocumentSnapshot) {
        val vista = layoutInflater.inflate(R.layout.item_resena, contenedor, false)
        val titulo = doc.getString("titulo") ?: "Libro"
        val puntuacion = doc.getLong("puntuacion")?.toInt() ?: 0
        val comentario = doc.getString("comentario").orEmpty()

        vista.findViewById<TextView>(R.id.tvResenaNombre).text = titulo
        vista.findViewById<TextView>(R.id.tvResenaEstrellas).text =
            "${Utilidades.estrellas(puntuacion)}  $puntuacion/5"
        vista.findViewById<TextView>(R.id.tvResenaComentario).text = comentario
        vista.findViewById<TextView>(R.id.tvResenaFecha).text =
            "Publicada el ${Utilidades.fecha(doc.getTimestamp("fecha"))}"
        vista.findViewById<View>(R.id.filaAcciones).visibility = View.VISIBLE

        vista.findViewById<Button>(R.id.btnEditarResena).setOnClickListener {
            Utilidades.mostrarDialogoResena(
                this, "Editar: $titulo", puntuacion, comentario
            ) { nuevaPuntuacion, nuevoComentario ->
                doc.reference.update(
                    mapOf(
                        "puntuacion" to nuevaPuntuacion,
                        "comentario" to nuevoComentario,
                        "fecha" to FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Reseña actualizada.", Toast.LENGTH_SHORT).show()
                    cargarMisResenas()
                }.addOnFailureListener {
                    Toast.makeText(this, "No se pudo actualizar la reseña.", Toast.LENGTH_LONG).show()
                }
            }
        }

        vista.findViewById<Button>(R.id.btnEliminarResena).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar reseña")
                .setMessage("¿Seguro que deseas eliminar la reseña de \"$titulo\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    doc.reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reseña eliminada.", Toast.LENGTH_SHORT).show()
                            cargarMisResenas()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "No se pudo eliminar la reseña.", Toast.LENGTH_LONG).show()
                        }
                }
                .show()
        }

        contenedor.addView(vista)
    }
}
