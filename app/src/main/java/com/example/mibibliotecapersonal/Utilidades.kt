package com.example.mibibliotecapersonal

import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utilidades {

    /** Convierte una puntuación (1..5) en estrellas: 4 -> ★★★★☆ */
    fun estrellas(puntuacion: Int): String {
        val p = puntuacion.coerceIn(0, 5)
        return "★".repeat(p) + "☆".repeat(5 - p)
    }

    /** Identificador estable a partir del título, para libros sin id de Firestore. */
    fun slug(texto: String): String =
        java.text.Normalizer.normalize(texto.lowercase(Locale.ROOT), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    private val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    fun fecha(ts: Timestamp?): String = ts?.toDate()?.let { formato.format(it) } ?: "—"

    /** Fecha de hoy + [dias] días, como Timestamp de Firestore. */
    fun enDias(dias: Int): Timestamp {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dias) }
        return Timestamp(Date(c.timeInMillis))
    }

    const val DIAS_PRESTAMO = 14

    /** Ejemplares que se asumen si el libro en Firestore aún no tiene el campo. */
    const val EJEMPLARES_POR_DEFECTO = 5L

    fun totalEjemplares(doc: DocumentSnapshot): Long =
        doc.getLong("ejemplaresTotales") ?: EJEMPLARES_POR_DEFECTO

    fun ejemplaresDisponibles(doc: DocumentSnapshot): Long =
        doc.getLong("ejemplaresDisponibles") ?: totalEjemplares(doc)

    /**
     * Diálogo para escribir o editar una reseña (puntuación 1-5 + comentario).
     * No se cierra hasta que los datos sean válidos.
     */
    fun mostrarDialogoResena(
        activity: AppCompatActivity,
        titulo: String,
        puntuacionInicial: Int,
        comentarioInicial: String,
        onGuardar: (puntuacion: Int, comentario: String) -> Unit
    ) {
        val vista = LayoutInflater.from(activity).inflate(R.layout.dialog_resena, null)
        val rb = vista.findViewById<RatingBar>(R.id.rbPuntuacion)
        val et = vista.findViewById<EditText>(R.id.etComentario)
        rb.rating = puntuacionInicial.coerceIn(1, 5).toFloat()
        et.setText(comentarioInicial)

        val dialogo = AlertDialog.Builder(activity)
            .setTitle(titulo)
            .setView(vista)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val puntuacion = rb.rating.toInt()
                val comentario = et.text.toString().trim()
                when {
                    puntuacion !in 1..5 ->
                        Toast.makeText(activity, "Elige una puntuación de 1 a 5 estrellas.", Toast.LENGTH_SHORT).show()
                    comentario.length < 3 ->
                        et.error = "Escribe un comentario"
                    else -> {
                        dialogo.dismiss()
                        onGuardar(puntuacion, comentario)
                    }
                }
            }
        }
        dialogo.show()
    }
}
