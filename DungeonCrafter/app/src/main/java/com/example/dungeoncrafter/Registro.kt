package com.example.dungeoncrafter

import Auxiliar.Conexion
import Modelo.Almacen
import Modelo.Carta
import Modelo.Configuracion
import Modelo.Users
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dungeoncrafter.databinding.ActivityRegistroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class Registro : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private lateinit var firebaseauth : FirebaseAuth
    val db = Firebase.firestore
    val TAG = "JVVM"
    var gen = 0
    private val canalNombre = "prueba"
    private val canalId = "CanalDePrueva"
    private val notificacionId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbRegistro)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firebaseauth = FirebaseAuth.getInstance()

        binding.tbRegistro.setNavigationOnClickListener {
            finish()
        }
        //Se encarga de validar todas las condiciones para crear un usuario
        binding.btnRegistr.setOnClickListener {
            if (binding.edEmailReg.text?.isNotEmpty() == true && binding.edPassReg.text?.isNotEmpty() == true && gen != 0 ){
                if (binding.edPassReg.text.toString() == binding.edConfReg.text.toString()){
                    if(binding.cbCondiciones.isChecked){
                        firebaseauth.createUserWithEmailAndPassword(binding.edEmailReg.text.toString(),binding.edPassReg.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful){
                                crearCartas()
                                guardarUsuario()
                                buscarConf(binding.edEmailReg.text.toString())
                                cambiarUltima(binding.edEmailReg.text.toString())
                                irMenuPrincipal(it.result?.user?.email?:"")
                                Toast.makeText(this, R.string.creado, Toast.LENGTH_SHORT).show()

                            } else {
                                Toast.makeText(this, R.string.errorCreacion, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this, R.string.errorCondiciones, Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, R.string.errorContrasenas, Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, R.string.datosVacios, Toast.LENGTH_SHORT).show()
            }
        }

        binding.rgGen.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.rbMale ->{
                    gen = 1
                    Log.d(TAG,"1")
                }
                R.id.rbFemale ->{
                    gen = 2
                    Log.d(TAG,"2")
                }
                R.id.rbOther ->{
                    gen = 3
                    Log.d(TAG,"3")
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    /**
     * Guarda el usuario en la base de datos
     * */
    fun guardarUsuario(){
        var user = hashMapOf(
            "usuario" to binding.edUserReg.text.toString(),
            "email" to binding.edEmailReg.text.toString(),
            "genero" to gen,
            "roles" to 0,
            "Monedas" to 0
        )

        // Si no existe el documento lo crea, si existe lo remplaza.
        db.collection("users")
            .document(user.get("email").toString()) //Será la clave del documento.
            .set(user).addOnSuccessListener {
            }
        Users(user["usuario"].toString(),user["roles"].toString().toInt(),user["email"].toString(),user["genero"].toString().toInt(),user["Monedas"].toString().toInt())
        crearCanalNotificacion()
        crearNotificacion(user["email"].toString())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_1 -> {
                var selectec: Int = 0
                val builder = MaterialAlertDialogBuilder(this)
                val inflater = layoutInflater
                builder.setTitle(R.string.menuOpciones)
                val dialogLayout = inflater.inflate(R.layout.dialog_option, null)
                val languageSpinner: Spinner = dialogLayout.findViewById(R.id.spinner2)

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,resources.getStringArray(R.array.idiomas))

                languageSpinner.adapter = adapter

                languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectec = position
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }

                val currentLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    resources.configuration.locale
                }

                val languageCode = currentLocale.language

                when(languageCode.toString()){
                    "en"->{
                        languageSpinner.setSelection(1)
                    }
                    "es"->{
                        languageSpinner.setSelection(0)
                    }
                }
                Log.d(TAG,languageCode.toString())

                builder.setView(dialogLayout)
                builder.setPositiveButton(R.string.guardar) { _, i -> cambiarIdioma(selectec)}
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun cambiarIdioma(pos: Int) {
        var languaje :String = ""
        when(pos){
            0-> languaje = "es"
            1-> languaje = "en"
        }

        val locale = Locale(languaje)
        Locale.setDefault(locale)

        val configuration = android.content.res.Configuration()
        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Puedes reiniciar la actividad actual para aplicar los cambios
        recreate()
    }
    private fun irMenuPrincipal(email:String, nombre:String = "Usuario"){
        Almacen.User = Users(binding.edUserReg.text.toString(),0,binding.edEmailReg.text.toString(),gen,0)
        val homeIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email",email)
            putExtra("nombre",nombre)
        }
        startActivity(homeIntent)
    }

    fun crearCartas(){
        Almacen.Cartas = ArrayList()
        Almacen.Cartas.add(Carta("Arthas", "arthas","relic_sacer", "boss_icon",0))
        Almacen.Cartas.add(Carta("Khorne", "khorne","relic_sacer", "boss_icon",2))
        Almacen.Cartas.add(Carta("Morko", "morko","relic_sacer", "boss_icon",7))
        Almacen.Cartas.add(Carta("Nurgle", "nurgle","relic_sacer", "boss_icon",1))
        Almacen.Cartas.add(Carta("Sargeras", "sargeras","relic_sacer", "boss_icon",5))
        Almacen.Cartas.add(Carta("Alamuerte", "alamuerte","relic_sacer", "boss_icon",3))
        Almacen.Cartas.add(Carta("Gorko", "gorko","relic_sacer", "boss_icon",4))
        Almacen.Cartas.add(Carta("Slaanesh", "slaanesh","relic_sacer", "boss_icon",6))

        Almacen.Cartas.forEach{ card ->
            var carta = hashMapOf(
                "nombre" to card.nombre,
                "imagen" to card.imagenPersonaje,
                "imagenRelic" to card.imagenRelic,
                "imagenTipo" to card.imagenTipo,
                "description" to card.descripcion,
                "user" to binding.edEmailReg.text.toString()
            )
            db.collection("cartas")
                .add(carta)
        }

    }

    fun buscarConf(email:String ){
        val c = Conexion.buscarConfiguracion(this, email)
        if(c == null){
            crearConf(email)
        }
    }

    fun crearConf(email:String){
        val currentLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
        val languageCode = currentLocale.language
        val c = Configuracion(email,languageCode.toString(),"no","si")
        Conexion.addConfiguracion(this, c)
    }

    fun cambiarUltima(email:String){
        var configuraciones = Conexion.obtenerConfiguraciones(this);
        for (c: Configuracion in configuraciones){
            if (c.usuario != email){
                Conexion.modConfiguracionUltimo(this, c.usuario, "no")
            }else{
                Almacen.Configuracion = c
                Conexion.modConfiguracionUltimo(this, c.usuario, "si")
            }
        }

    }
    /*
    * Creacion de notificaciones
    * */
    private fun crearCanalNotificacion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val canalImportancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(canalId, canalNombre, canalImportancia)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun crearNotificacion(email: String){
        val notificacion = NotificationCompat.Builder(this,canalId).also{
            it.setSmallIcon(R.mipmap.ic_launcher_app_icon)
            it.setContentTitle(getString(R.string.Bienvenido))
            it.setContentText(getString(R.string.Mensaje)+ email)
        }.build()

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificacionId,notificacion)
    }

    /**
     * Para que cuando vuelva aqui se cierre de forma automatica
     * */
    override fun onRestart() {
        super.onRestart()
        finish()
    }


}