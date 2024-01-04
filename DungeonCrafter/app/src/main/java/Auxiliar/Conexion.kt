package Auxiliar

import Conexion.AdminSQLiteConexion
import Modelo.Configuracion
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity

object Conexion {
    // Administracion de las consultas de mysql
    private  var DATABASE_NAME = "configuracion.db3"
    private  var DATABASE_VERSION = 1


    fun cambiarBD(nombreBD:String){
        this.DATABASE_NAME = nombreBD
    }
    /*
    * Añade una nueva configuracion
    * */
    fun addConfiguracion(contexto: AppCompatActivity, c: Configuracion):Long{
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.writableDatabase
        val registro = ContentValues()
        registro.put("usuario", c.usuario)
        registro.put("idioma",c.idioma)
        registro.put("oscuro", c.oscuro)
        registro.put("ultimo", c.ultimo)
        val codigo = bd.insert("configuracion", null, registro)
        bd.close()
        return codigo
    }
    /*
    * Elimina una configuracion
    * */
    fun delConfiguracion(contexto: AppCompatActivity, usuario: String):Int{
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.writableDatabase
        val cant = bd.delete("configuracion", "usuario=?", arrayOf(usuario.toString()))
        bd.close()
        return cant
    }
    /*
    * Modifica una configuracion
    * */
    fun modConfiguracion(contexto: AppCompatActivity, usuario:String, c:Configuracion):Int {
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.writableDatabase
        val registro = ContentValues()
        registro.put("idioma", c.idioma)
        registro.put("oscuro", c.oscuro)
        val cant = bd.update("configuracion", registro, "usuario=?", arrayOf(usuario.toString()))
        bd.close()
        return cant
    }
    /*
    * Modifica solo la casilla ultima de una configuracion
    * */
    fun modConfiguracionUltimo(contexto: AppCompatActivity, usuario:String, ultima:String):Int {
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.writableDatabase
        val registro = ContentValues()
        registro.put("ultimo", ultima)
        val cant = bd.update("configuracion", registro, "usuario=?", arrayOf(usuario.toString()))
        bd.close()
        return cant
    }

    /*
    * Busca una configuracion de un usuario
    * */
    fun buscarConfiguracion(contexto: AppCompatActivity, usuario:String):Configuracion? {
        var c:Configuracion? = null
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.readableDatabase
        val fila =bd.rawQuery(
            "SELECT * FROM configuracion WHERE usuario=?",
            arrayOf(usuario.toString())
        )
        if (fila.moveToFirst()) {
            c = Configuracion(usuario, fila.getString(0), fila.getString(1), fila.getString(2))
        }
        bd.close()
        return c
    }

    /*
    * Busca todas las configuraciones
    * */
    fun obtenerConfiguraciones(contexto: AppCompatActivity):ArrayList<Configuracion>{
        var personas:ArrayList<Configuracion> = ArrayList(1)
        val admin = AdminSQLiteConexion(contexto, this.DATABASE_NAME, null, DATABASE_VERSION)
        val bd = admin.readableDatabase
        val fila = bd.rawQuery("select * from configuracion", null)
        while (fila.moveToNext()) {
            var c:Configuracion = Configuracion(fila.getString(0),fila.getString(1), fila.getString(2), fila.getString(3))
            personas.add(c)
        }
        bd.close()
        return personas //este arrayList lo puedo poner en un adapter de un RecyclerView por ejemplo.
    }



}