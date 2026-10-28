package com.example.mibibliotecapersonal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (savedInstanceState == null) {
            val consulta = intent.getStringExtra("consulta") ?: ""

            val fragment = SearchFragment().apply {
                arguments = Bundle().apply {
                    putString("consulta", consulta)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.searchContainer, fragment)
                .commit()
        }
    }
}