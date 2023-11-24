package com.example.dungeoncrafter

import Adaptadores.AdaptadorCartas
import Modelo.Almacen
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dungeoncrafter.databinding.ActivityColeccionBinding


class Coleccion : AppCompatActivity() {
    lateinit var binding: ActivityColeccionBinding
    lateinit var miRecyclerView : RecyclerView

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var contextoPrincipal: Context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColeccionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        miRecyclerView = binding.recycledCartas
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = GridLayoutManager(this, 2)

        var miAdapter = AdaptadorCartas(Almacen.Cartas,this)

        miRecyclerView.adapter = miAdapter

        contextoPrincipal = this
    }
}