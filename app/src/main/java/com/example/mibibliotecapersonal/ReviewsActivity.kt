package com.example.mibibliotecapersonal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ReviewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        val titulo = intent.getStringExtra("titulo")
        val libroId = intent.getStringExtra("libroId").orEmpty()

        if (!titulo.isNullOrBlank()) {
            findViewById<TextView>(R.id.tvTituloLibroResenas).text = "Reseñas de $titulo"
        }

        findViewById<Button>(R.id.btnVolverResenas).setOnClickListener { finish() }

        if (libroId.isNotBlank()) cargarResenas(libroId)
    }

    private fun cargarResenas(libroId: String) {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorResenas)
        val tvSin = findViewById<TextView>(R.id.tvSinResenas)
        val tvPromedio = findViewById<TextView>(R.id.tvPromedio)
        val tvEstrellas = findViewById<TextView>(R.id.tvEstrellasPromedio)
        val tvTotal = findViewById<TextView>(R.id.tvTotalOpiniones)

        FirebaseFirestore.getInstance().collection("resenas")
            .whereEqualTo("libroId", libroId)
            .get()
            .addOnSuccessListener { r ->
                contenedor.removeAllViews()

                // Más recientes primero (se ordena aquí para no requerir índices en Firestore)
                val docs = r.documents.sortedByDescending { it.getTimestamp("fecha")?.seconds ?: Long.MAX_VALUE }

                if (docs.isEmpty()) {
                    tvPromedio.text = "—"
                    tvEstrellas.text = Utilidades.estrellas(0)
                    tvTotal.text = "0 opiniones"
                    tvSin.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                tvSin.visibility = View.GONE

                val notas = docs.mapNotNull { it.getLong("puntuacion")?.toInt() }
                val promedio = notas.average()
                tvPromedio.text = String.format(Locale("es", "ES"), "%.1f de 5", promedio)
                tvEstrellas.text = Utilidades.estrellas(Math.round(promedio).toInt())
                tvTotal.text = "${docs.size} ${if (docs.size == 1) "opinión" else "opiniones"}"

                for (doc in docs) {
                    val vista = layoutInflater.inflate(R.layout.item_resena, contenedor, false)
                    vista.findViewById<TextView>(R.id.tvResenaNombre).text =
                        doc.getString("usuario") ?: "Lector"
                    vista.findViewById<TextView>(R.id.tvResenaEstrellas).text =
                        Utilidades.estrellas(doc.getLong("puntuacion")?.toInt() ?: 0)
                    vista.findViewById<TextView>(R.id.tvResenaComentario).text =
                        doc.getString("comentario").orEmpty()
                    vista.findViewById<TextView>(R.id.tvResenaFecha).text =
                        Utilidades.fecha(doc.getTimestamp("fecha"))
                    contenedor.addView(vista)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar las reseñas.", Toast.LENGTH_SHORT).show()
            }
    }
}
