package com.example.mibibliotecapersonal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class BookDetailActivity : AppCompatActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var titulo: String
    private lateinit var autor: String
    private var libroId: String = ""

    // true cuando el libro existe en la colección "libros" de Firestore
    private var enCatalogo = false
    private var disponibles = 0L

    private lateinit var tvDisponibilidad: TextView
    private lateinit var tvTotalEjemplares: TextView
    private lateinit var tvEjemplaresDisponibles: TextView
    private lateinit var tvEjemplaresPrestados: TextView
    private lateinit var tvFechaDisponibilidad: TextView
    private lateinit var btnSolicitarPrestamo: Button
    private lateinit var btnAvisarmeDisponibilidad: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // Datos del libro seleccionado
        titulo = intent.getStringExtra("titulo") ?: "Libro sin título"
        autor = intent.getStringExtra("autor") ?: "Autor desconocido"
        val genero = intent.getStringExtra("genero") ?: "Sin género"
        val anio = intent.getStringExtra("anio") ?: "No especificado"
        val sinopsis = intent.getStringExtra("sinopsis")
            ?: "No hay una descripción disponible."

        findViewById<TextView>(R.id.tvTitulo).text = titulo
        findViewById<TextView>(R.id.tvAutor).text = "Autor: $autor"
        findViewById<TextView>(R.id.tvGenero).text = "Género: $genero"
        findViewById<TextView>(R.id.tvAnio).text = "Año de publicación: $anio"
        findViewById<TextView>(R.id.tvSinopsis).text = sinopsis

        tvDisponibilidad = findViewById(R.id.tvDisponibilidad)
        tvTotalEjemplares = findViewById(R.id.tvTotalEjemplares)
        tvEjemplaresDisponibles = findViewById(R.id.tvEjemplaresDisponibles)
        tvEjemplaresPrestados = findViewById(R.id.tvEjemplaresPrestados)
        tvFechaDisponibilidad = findViewById(R.id.tvFechaDisponibilidad)
        btnSolicitarPrestamo = findViewById(R.id.btnSolicitarPrestamo)
        btnAvisarmeDisponibilidad = findViewById(R.id.btnAvisarmeDisponibilidad)

        // Estado inicial mientras se consulta Firestore
        btnSolicitarPrestamo.isEnabled = false
        btnAvisarmeDisponibilidad.visibility = View.GONE

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        btnSolicitarPrestamo.setOnClickListener { confirmarPrestamo() }

        btnAvisarmeDisponibilidad.setOnClickListener {
            Toast.makeText(
                this,
                "La función de avisos estará disponible próximamente.",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.btnVerResenas).setOnClickListener {
            startActivity(Intent(this, ReviewsActivity::class.java).apply {
                putExtra("titulo", titulo)
                putExtra("libroId", libroId)
            })
        }

        findViewById<Button>(R.id.btnEscribirResena).setOnClickListener { escribirResena() }

        // Si no llega el id (p. ej. desde el buscador), se busca por título.
        val idRecibido = intent.getStringExtra("libroId")
        if (!idRecibido.isNullOrBlank()) {
            libroId = idRecibido
            cargarDatos()
        } else {
            db.collection("libros").whereEqualTo("titulo", titulo).limit(1).get()
                .addOnSuccessListener { r ->
                    libroId = r.documents.firstOrNull()?.id ?: Utilidades.slug(titulo)
                    cargarDatos()
                }
                .addOnFailureListener {
                    libroId = Utilidades.slug(titulo)
                    cargarDatos()
                }
        }
    }

    private var primeraCarga = true

    override fun onResume() {
        super.onResume()
        // Al volver de otra pantalla se refrescan opiniones y disponibilidad
        // (la primera carga ya la hace onCreate).
        if (primeraCarga) { primeraCarga = false; return }
        if (libroId.isNotEmpty()) cargarDatos()
    }

    private fun cargarDatos() {
        cargarDisponibilidad()
        cargarResumenResenas()
    }

    // ───────────────────────── DISPONIBILIDAD ─────────────────────────

    private fun cargarDisponibilidad() {
        db.collection("libros").document(libroId).get()
            .addOnSuccessListener { doc ->
                enCatalogo = doc.exists()
                if (!enCatalogo) {
                    mostrarSinInventario()
                    return@addOnSuccessListener
                }
                val total = Utilidades.totalEjemplares(doc)
                disponibles = Utilidades.ejemplaresDisponibles(doc)
                mostrarDisponibilidad(total, disponibles)
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo consultar la disponibilidad.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDisponibilidad(total: Long, disp: Long) {
        tvTotalEjemplares.visibility = View.VISIBLE
        tvEjemplaresDisponibles.visibility = View.VISIBLE
        tvEjemplaresPrestados.visibility = View.VISIBLE

        tvTotalEjemplares.text = "Ejemplares en total: $total"
        tvEjemplaresDisponibles.text = "Disponibles: $disp"
        tvEjemplaresPrestados.text = "Prestados: ${(total - disp).coerceAtLeast(0)}"

        if (disp > 0) {
            tvDisponibilidad.text = "● Disponible para préstamo"
            tvDisponibilidad.setTextColor(getColor(R.color.success))
            tvFechaDisponibilidad.visibility = View.GONE
            btnAvisarmeDisponibilidad.visibility = View.GONE
            btnSolicitarPrestamo.visibility = View.VISIBLE
            btnSolicitarPrestamo.isEnabled = true
        } else {
            tvDisponibilidad.text = "● No disponible"
            tvDisponibilidad.setTextColor(getColor(R.color.error))
            tvFechaDisponibilidad.text = "Disponibilidad estimada: pendiente de confirmar"
            tvFechaDisponibilidad.visibility = View.VISIBLE
            btnAvisarmeDisponibilidad.visibility = View.VISIBLE
            btnSolicitarPrestamo.visibility = View.GONE
        }
    }

    private fun mostrarSinInventario() {
        tvDisponibilidad.text = "● Sin información de ejemplares"
        tvDisponibilidad.setTextColor(getColor(R.color.text_secondary))
        tvTotalEjemplares.visibility = View.GONE
        tvEjemplaresDisponibles.visibility = View.GONE
        tvEjemplaresPrestados.visibility = View.GONE
        tvFechaDisponibilidad.visibility = View.GONE
        btnAvisarmeDisponibilidad.visibility = View.GONE
        btnSolicitarPrestamo.visibility = View.VISIBLE
        btnSolicitarPrestamo.isEnabled = false
    }

    // ───────────────────────── PRÉSTAMO ─────────────────────────

    private fun confirmarPrestamo() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Inicia sesión para solicitar un préstamo.", Toast.LENGTH_LONG).show()
            return
        }
        if (!enCatalogo) {
            Toast.makeText(this, "Este libro aún no está en el catálogo de la biblioteca.", Toast.LENGTH_LONG).show()
            return
        }

        btnSolicitarPrestamo.isEnabled = false

        // No permitir pedir dos veces el mismo libro mientras siga activo
        db.collection("prestamos").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { r ->
                val yaLoTiene = r.documents.any {
                    it.getString("libroId") == libroId && it.getString("estado") == "activo"
                }
                if (yaLoTiene) {
                    btnSolicitarPrestamo.isEnabled = true
                    Toast.makeText(this, "Ya tienes este libro en préstamo.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                AlertDialog.Builder(this)
                    .setTitle("Solicitar préstamo")
                    .setMessage(
                        "Se te prestará \"$titulo\" por ${Utilidades.DIAS_PRESTAMO} días.\n" +
                                "Devolución prevista: ${Utilidades.fecha(Utilidades.enDias(Utilidades.DIAS_PRESTAMO))}"
                    )
                    .setNegativeButton("Cancelar") { _, _ -> btnSolicitarPrestamo.isEnabled = true }
                    .setOnCancelListener { btnSolicitarPrestamo.isEnabled = true }
                    .setPositiveButton("Confirmar") { _, _ -> registrarPrestamo(user.uid) }
                    .show()
            }
            .addOnFailureListener {
                btnSolicitarPrestamo.isEnabled = true
                Toast.makeText(this, "No se pudo verificar tus préstamos.", Toast.LENGTH_LONG).show()
            }
    }

    private fun registrarPrestamo(uid: String) {
        val libroRef = db.collection("libros").document(libroId)
        val prestamoRef = db.collection("prestamos").document()

        // Transacción: descuenta un ejemplar y crea el préstamo a la vez,
        // para que dos usuarios no se lleven el último ejemplar.
        db.runTransaction { tx ->
            val libro = tx.get(libroRef)
            if (!libro.exists()) throw IllegalStateException("SIN_LIBRO")

            val total = Utilidades.totalEjemplares(libro)
            val disp = Utilidades.ejemplaresDisponibles(libro)
            if (disp <= 0) throw IllegalStateException("SIN_STOCK")

            tx.update(
                libroRef,
                mapOf(
                    "ejemplaresTotales" to total,
                    "ejemplaresDisponibles" to disp - 1
                )
            )
            tx.set(
                prestamoRef,
                hashMapOf(
                    "uid" to uid,
                    "libroId" to libroId,
                    "titulo" to titulo,
                    "autor" to autor,
                    "estado" to "activo",
                    "fechaPrestamo" to FieldValue.serverTimestamp(),
                    "fechaDevolucionPrevista" to Utilidades.enDias(Utilidades.DIAS_PRESTAMO),
                    "fechaDevolucion" to null
                )
            )
            null
        }.addOnSuccessListener {
            Toast.makeText(this, "¡Préstamo registrado! Lo verás en la pestaña Préstamos.", Toast.LENGTH_LONG).show()
            cargarDisponibilidad()
        }.addOnFailureListener { e ->
            btnSolicitarPrestamo.isEnabled = true
            val mensaje = when {
                e.message?.contains("SIN_STOCK") == true -> "Ya no quedan ejemplares disponibles."
                e.message?.contains("PERMISSION_DENIED") == true ->
                    "Firestore rechazó la operación. Revisa las reglas de seguridad de 'prestamos' y 'libros'."
                else -> "No se pudo registrar el préstamo. Inténtalo de nuevo."
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            cargarDisponibilidad()
        }
    }

    // ───────────────────────── RESEÑAS ─────────────────────────

    private fun cargarResumenResenas() {
        val tvResumen = findViewById<TextView>(R.id.tvResumenResenas)
        val tvEstrellas = findViewById<TextView>(R.id.tvEstrellasLibro)

        db.collection("resenas").whereEqualTo("libroId", libroId).get()
            .addOnSuccessListener { r ->
                val notas = r.documents.mapNotNull { it.getLong("puntuacion")?.toInt() }
                if (notas.isEmpty()) {
                    tvEstrellas.text = Utilidades.estrellas(0)
                    tvResumen.text = "Sin opiniones todavía"
                } else {
                    val promedio = notas.average()
                    tvEstrellas.text = Utilidades.estrellas(Math.round(promedio).toInt())
                    tvResumen.text = String.format(
                        Locale("es", "ES"), "%.1f de 5 · %d %s",
                        promedio, notas.size, if (notas.size == 1) "opinión" else "opiniones"
                    )
                }
            }
            .addOnFailureListener { tvResumen.text = "No se pudieron cargar las opiniones" }
    }

    private fun escribirResena() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Inicia sesión para escribir una reseña.", Toast.LENGTH_LONG).show()
            return
        }
        val resenaRef = db.collection("resenas").document("${user.uid}_$libroId")

        // Si ya reseñaste este libro, se abre para editarla (una reseña por usuario y libro)
        resenaRef.get().addOnSuccessListener { previa ->
            val puntInicial = previa.getLong("puntuacion")?.toInt() ?: 5
            val comInicial = previa.getString("comentario") ?: ""

            Utilidades.mostrarDialogoResena(
                this,
                if (previa.exists()) "Editar tu reseña" else "Reseña de \"$titulo\"",
                puntInicial,
                comInicial
            ) { puntuacion, comentario ->
                val nombre = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Lector"

                resenaRef.set(
                    hashMapOf(
                        "uid" to user.uid,
                        "libroId" to libroId,
                        "titulo" to titulo,
                        "usuario" to nombre,
                        "puntuacion" to puntuacion,
                        "comentario" to comentario,
                        "fecha" to FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "¡Gracias por tu reseña!", Toast.LENGTH_SHORT).show()
                    cargarResumenResenas()
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "No se pudo guardar la reseña. Revisa las reglas de Firestore para 'resenas'.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo consultar tu reseña.", Toast.LENGTH_SHORT).show()
        }
    }
}
