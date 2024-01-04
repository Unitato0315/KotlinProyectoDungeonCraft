package Modelo

import java.io.Serializable

data class Users(var usuario: String, var rol: Int, var correo: String, var genero: Int,var monedas: Int): Serializable
