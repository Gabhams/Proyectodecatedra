package com.example.mibibliotecapersonal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class LoansActivity : AppCompatActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loans)

        // Navegación inferior
        val menuInferior =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Marcar Préstamos como la pantalla actual
        menuInferior.selectedItemId = R.id.nav_prestamos

        menuInferior.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_inicio -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_prestamos -> {
                    // Ya estamos en Préstamos
                    true
                }

                R.id.nav_perfil -> {
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }

        // Botones de las pestañas
        val btnActivos = findViewById<Button>(R.id.btnTabActivos)
        val btnHistorial = findViewById<Button>(R.id.btnTabHistorial)
        val btnReservas = findViewById<Button>(R.id.btnTabReservas)

        // Contenido de cada pestaña
        val contenidoActivos = findViewById<View>(R.id.contenidoActivos)
        val contenidoHistorial = findViewById<View>(R.id.contenidoHistorial)
        val contenidoReservas = findViewById<View>(R.id.contenidoReservas)

        // Cambiar entre Activos, Historial y Reservas
        fun mostrarSeccion(seccion: String) {
            contenidoActivos.visibility =
                if (seccion == "activos") View.VISIBLE else View.GONE

            contenidoHistorial.visibility =
                if (seccion == "historial") View.VISIBLE else View.GONE

            contenidoReservas.visibility =
                if (seccion == "reservas") View.VISIBLE else View.GONE

            actualizarBoton(btnActivos, seccion == "activos")
            actualizarBoton(btnHistorial, seccion == "historial")
            actualizarBoton(btnReservas, seccion == "reservas")
        }

        btnActivos.setOnClickListener {
            mostrarSeccion("activos")
        }

        btnHistorial.setOnClickListener {
            mostrarSeccion("historial")
        }

        btnReservas.setOnClickListener {
            mostrarSeccion("reservas")
        }

        // Cancelar la solicitud de disponibilidad de Harry Potter
        val btnCancelarSolicitud =
            findViewById<Button>(R.id.btnCancelarSolicitudHarry)

        val tarjetaSolicitud =
            findViewById<LinearLayout>(R.id.cardSolicitudHarry)

        btnCancelarSolicitud.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancelar solicitud")
                .setMessage(
                    "¿Seguro que deseas cancelar el aviso de disponibilidad " +
                            "de Harry Potter y la piedra filosofal?"
                )
                .setNegativeButton("No, volver", null)
                .setPositiveButton("Sí, cancelar") { _, _ ->
                    tarjetaSolicitud.visibility = View.GONE

                    Toast.makeText(
                        this,
                        "Solicitud cancelada.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .show()
        }

        // Mostrar los préstamos activos al abrir la pantalla
        mostrarSeccion("activos")
    }

    override fun onResume() {
        super.onResume()
        cargarPrestamos()
    }

    private fun cargarPrestamos() {
        val user = FirebaseAuth.getInstance().currentUser
        val contActivos = findViewById<LinearLayout>(R.id.contenedorActivos)
        val contHistorial = findViewById<LinearLayout>(R.id.contenedorHistorial)
        val sinActivos = findViewById<TextView>(R.id.tvSinActivos)
        val sinHistorial = findViewById<TextView>(R.id.tvSinHistorial)

        if (user == null) {
            sinActivos.text = "Inicia sesión para ver tus préstamos."
            sinActivos.visibility = View.VISIBLE
            sinHistorial.visibility = View.VISIBLE
            return
        }

        db.collection("prestamos").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { r ->
                contActivos.removeAllViews()
                contHistorial.removeAllViews()

                val ordenados = r.documents.sortedByDescending {
                    it.getTimestamp("fechaPrestamo")?.seconds ?: Long.MAX_VALUE
                }
                val activos = ordenados.filter { it.getString("estado") == "activo" }
                val historial = ordenados.filter { it.getString("estado") == "devuelto" }

                activos.forEach { contActivos.addView(crearTarjeta(contActivos, it, true)) }
                historial.forEach { contHistorial.addView(crearTarjeta(contHistorial, it, false)) }

                sinActivos.text = "No tienes préstamos activos."
                sinActivos.visibility = if (activos.isEmpty()) View.VISIBLE else View.GONE
                sinHistorial.visibility = if (historial.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar tus préstamos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearTarjeta(padre: LinearLayout, doc: DocumentSnapshot, activo: Boolean): View {
        val vista = layoutInflater.inflate(R.layout.item_prestamo, padre, false)
        val titulo = doc.getString("titulo") ?: "Libro"

        vista.findViewById<TextView>(R.id.tvPrestamoTitulo).text = titulo
        vista.findViewById<TextView>(R.id.tvPrestamoAutor).text = doc.getString("autor").orEmpty()
        vista.findViewById<TextView>(R.id.tvPrestamoFecha1).text =
            "Fecha del préstamo: ${Utilidades.fecha(doc.getTimestamp("fechaPrestamo"))}"

        val estado = vista.findViewById<TextView>(R.id.tvPrestamoEstado)
        val fecha2 = vista.findViewById<TextView>(R.id.tvPrestamoFecha2)
        val btnDevolver = vista.findViewById<Button>(R.id.btnDevolver)

        if (activo) {
            val prevista = doc.getTimestamp("fechaDevolucionPrevista")
            fecha2.text = "Devolución prevista: ${Utilidades.fecha(prevista)}"

            val vencido = prevista?.toDate()?.before(Date()) == true
            estado.text = if (vencido) "⚠ Devolución vencida" else "● Préstamo activo"
            if (vencido) estado.setTextColor(getColor(R.color.error))

            btnDevolver.visibility = View.VISIBLE
            btnDevolver.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Devolver libro")
                    .setMessage("¿Confirmas la devolución de \"$titulo\"?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Devolver") { _, _ -> devolver(doc) }
                    .show()
            }
        } else {
            fecha2.text = "Fecha de devolución: ${Utilidades.fecha(doc.getTimestamp("fechaDevolucion"))}"
            estado.text = "✓ Devuelto"
        }
        return vista
    }

    private fun devolver(prestamo: DocumentSnapshot) {
        val libroId = prestamo.getString("libroId").orEmpty()
        val libroRef = db.collection("libros").document(libroId)

        db.runTransaction { tx ->
            val actual = tx.get(prestamo.reference)
            if (actual.getString("estado") != "activo") throw IllegalStateException("YA_DEVUELTO")

            val libro = tx.get(libroRef)
            if (libro.exists()) {
                val total = Utilidades.totalEjemplares(libro)
                val disp = Utilidades.ejemplaresDisponibles(libro)
                tx.update(
                    libroRef,
                    mapOf(
                        "ejemplaresTotales" to total,
                        "ejemplaresDisponibles" to minOf(disp + 1, total)
                    )
                )
            }
            tx.update(
                prestamo.reference,
                mapOf(
                    "estado" to "devuelto",
                    "fechaDevolucion" to FieldValue.serverTimestamp()
                )
            )
            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Libro devuelto. ¡Gracias!", Toast.LENGTH_SHORT).show()
            cargarPrestamos()
        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo registrar la devolución.", Toast.LENGTH_LONG).show()
            cargarPrestamos()
        }
    }

    private fun actualizarBoton(
        boton: Button,
        seleccionado: Boolean
    ) {
        if (seleccionado) {
            boton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    getColor(R.color.purple_primary)
                )
            )
            boton.setTextColor(getColor(R.color.white))
        } else {
            boton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    getColor(R.color.purple_light)
                )
            )
            boton.setTextColor(getColor(R.color.purple_dark))
        }
    }
}