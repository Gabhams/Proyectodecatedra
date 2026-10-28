package com.example.mibibliotecapersonal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_search,
            container,
            false
        )

        val buscador = view.findViewById<EditText>(R.id.etBuscar)

        val libro1 = view.findViewById<TextView>(R.id.resultadoLibro1)
        val libro2 = view.findViewById<TextView>(R.id.resultadoLibro2)
        val libro3 = view.findViewById<TextView>(R.id.resultadoLibro3)

        val mensajeSinResultados =
            view.findViewById<TextView>(R.id.tvSinResultados)

        val consultaInicial = arguments?.getString("consulta") ?: ""

        val resultados = listOf(
            libro1 to "Harry Potter y la piedra filosofal J. K. Rowling Fantasía",
            libro2 to "El principito Antoine de Saint-Exupéry Fábula",
            libro3 to "1984 George Orwell Distopía"
        )

        fun filtrarLibros(texto: String) {
            val consulta = texto.trim()

            resultados.forEach { (libro, informacion) ->
                libro.visibility = if (
                    consulta.isEmpty() ||
                    informacion.contains(consulta, ignoreCase = true)
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

            val hayResultados = resultados.any { (libro, _) ->
                libro.visibility == View.VISIBLE
            }

            mensajeSinResultados.visibility =
                if (consulta.isNotEmpty() && !hayResultados) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        buscador.doAfterTextChanged { texto ->
            filtrarLibros(texto?.toString().orEmpty())
        }

        // Mostrar la consulta enviada desde Inicio.
        if (consultaInicial.isNotEmpty()) {
            buscador.setText(consultaInicial)
            buscador.setSelection(buscador.text.length)
        } else {
            filtrarLibros("")
        }

        libro1.setOnClickListener {
            abrirDetalles(
                "Harry Potter y la piedra filosofal",
                "J. K. Rowling",
                "Fantasía",
                "Harry descubre que es un mago y comienza sus aventuras en Hogwarts."
            )
        }

        libro2.setOnClickListener {
            abrirDetalles(
                "El principito",
                "Antoine de Saint-Exupéry",
                "Fábula",
                "Un relato sobre la amistad, el amor y la importancia de mirar más allá de las apariencias."
            )
        }

        libro3.setOnClickListener {
            abrirDetalles(
                "1984",
                "George Orwell",
                "Distopía",
                "Una novela distópica que explora la vigilancia, el control social y la libertad individual."
            )
        }

        return view
    }

    private fun abrirDetalles(
        titulo: String,
        autor: String,
        genero: String,
        sinopsis: String
    ) {
        val intent = Intent(requireContext(), BookDetailActivity::class.java)

        intent.putExtra("titulo", titulo)
        intent.putExtra("autor", autor)
        intent.putExtra("genero", genero)
        intent.putExtra("sinopsis", sinopsis)

        startActivity(intent)
    }
}