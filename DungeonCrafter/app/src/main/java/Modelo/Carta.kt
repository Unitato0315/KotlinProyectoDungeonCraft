package Modelo

import java.io.Serializable

data class Carta(var nombre: String,
                 var imagenPersonaje: String,
                 var imagenRelic: String,
                 var imagenTipo: String,
                 var descripcion: Int): Serializable
