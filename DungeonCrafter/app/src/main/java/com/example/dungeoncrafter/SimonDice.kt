package com.example.dungeoncrafter

import Auxiliar.Conexion
import Modelo.Almacen
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dungeoncrafter.databinding.ActivitySimonDiceBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class SimonDice : AppCompatActivity() {
    lateinit var binding: ActivitySimonDiceBinding
    var patron = mutableListOf<Int>()
    var nivel = 0
    var colores = mutableListOf<TextView>()
    var cont = 0
    var cont2 = 0
    var contRespuestas = 0
    val db = Firebase.firestore
    var monedasBase = 10
    private lateinit var firebaseauth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val TAG = "JVVM"
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var contextoPrincipal: Context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimonDiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tbSimonDice)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        colores.addAll(listOf(binding.etRojo,binding.etVerde,binding.etYellow,binding.etBlue))
        binding.tvLevel.visibility= View.INVISIBLE
        binding.btnReiniciar.isEnabled=false
        binding.btnSiguiente.text="Iniciar"
        for (color in colores){
            color.isEnabled = false
        }

        binding.btnSiguiente.setOnClickListener {
            this.nivel++
            patron.add((1..4).random())
            contRespuestas=0
            iniciarPatron()
            binding.tvLevel.visibility= View.VISIBLE
            binding.tvLevel.text= getString(R.string.nivel)+" "+nivel
        }

        binding.btnReiniciar.setOnClickListener {
            nivel = 0
            contRespuestas = 0
            binding.tvLevel.visibility= View.INVISIBLE
            binding.btnSiguiente.text=getString(R.string.Iniciar)
            binding.btnSiguiente.isEnabled=true
            patron.clear()
            for (color in colores){
                color.isEnabled = false
            }
        }

        for (color in colores){
            color.setOnClickListener {
                comprobarEleccion(color)
            }
        }

        Log.d(TAG,Almacen.User.monedas.toString())

        binding.tvCoins.text =" "+Almacen.User.monedas.toString()

        binding.tbSimonDice.subtitle = getString(R.string.SimonDice)
        binding.tbSimonDice.setNavigationOnClickListener {
            finish()
        }
        contextoPrincipal = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_1 -> {
                Log.e(TAG, firebaseauth.currentUser.toString())
                firebaseauth.signOut()

                val signInClient = Identity.getSignInClient(this)
                signInClient.signOut()
                Log.e(TAG,"Cerrada sesión completamente")
                finish()
            }
            R.id.option_2 -> {
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

                builder.setView(dialogLayout)
                builder.setPositiveButton(R.string.guardar) { _, i -> cambiarIdioma(selectec)}
                builder.show()
            }
            R.id.opcion_web -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Unitato0315/KotlinProyectoDungeonCraft.git")))
            R.id.opcion_acercade -> {

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.acercaDeTitulo))
                    .setMessage(resources.getString(R.string.acercaDeContenido))
                    .show()
            }
            R.id.opcion_correo ->{
                val destinatario = "jvsonic9@gmail.com"
                val asunto = resources.getString(R.string.Contacto)


                // Crear un Intent con la acción ACTION_SENDTO para enviar el correo
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // Establecer el esquema como "mailto:"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario)) // Establecer el destinatario del correo
                    putExtra(Intent.EXTRA_SUBJECT, asunto) // Establecer el asunto del correo
                    putExtra(Intent.EXTRA_TEXT, "") // Establecer el cuerpo del correo
                }

                // Verificar si existe alguna aplicación de correo electrónico para manejar el intent

                startActivity(intent)
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

        Almacen.Configuracion.idioma = languaje
        Conexion.modConfiguracion(this, Almacen.Configuracion.usuario, Almacen.Configuracion)

        val locale = Locale(languaje)
        Locale.setDefault(locale)

        val configuration = android.content.res.Configuration()
        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Puedes reiniciar la actividad actual para aplicar los cambios
        recreate()
    }

    fun comprobarEleccion(color: TextView){
        if(patron[contRespuestas].compareTo(color.contentDescription.toString().toInt())==0){
            for (color in colores){
                color.isEnabled = false
            }
            var timer = object: CountDownTimer((2 * 800).toLong(), 800){
                override fun onTick(millisUntilFinished: Long) {
                    if(cont%2 == 0){

                        when(patron[contRespuestas]){
                            1 -> colores[0].setBackgroundColor(getColor(R.color.red))
                            2 -> colores[1].setBackgroundColor(getColor(R.color.green))
                            3 -> colores[2].setBackgroundColor(getColor(R.color.yellow))
                            4 -> colores[3].setBackgroundColor(getColor(R.color.orange))
                        }
                    }else{
                        when(patron[contRespuestas]){
                            1 -> colores[0].setBackgroundColor(getColor(R.color.redPalid))
                            2 -> colores[1].setBackgroundColor(getColor(R.color.greenPalid))
                            3 -> colores[2].setBackgroundColor(getColor(R.color.yellowPalid))
                            4 -> colores[3].setBackgroundColor(getColor(R.color.orangePalid))
                        }
                    }
                    cont++
                }

                override fun onFinish() {
                    cont = 0
                    for (color in colores){
                        color.isEnabled = true
                    }
                    contRespuestas++;
                    if (contRespuestas == patron.size){
                        for (color in colores){
                            color.isEnabled = false
                        }
                        var monedasGanadas = monedasBase * nivel
                        Almacen.User.monedas = Almacen.User.monedas + monedasGanadas
                        Toast.makeText(contextoPrincipal,getString(R.string.acierto)+ monedasGanadas.toString()+" "+getString(R.string.moneda), Toast.LENGTH_SHORT).show()
                        binding.tvCoins.text =" "+Almacen.User.monedas.toString()
                        binding.btnSiguiente.text = getString(R.string.siguiente)
                        binding.btnSiguiente.isEnabled=true
                        var user = hashMapOf(
                            "usuario" to Almacen.User.usuario,
                            "email" to Almacen.User.correo,
                            "genero" to Almacen.User.genero,
                            "roles" to Almacen.User.rol,
                            "Monedas" to Almacen.User.monedas
                        )
                        db.collection("users")
                            .document(Almacen.User.correo)
                            .set(user).addOnSuccessListener {
                                Log.d(TAG, "crea")
                            }
                    }
                }
            }
            timer.start()

        }else{
            contRespuestas=0
            Toast.makeText(contextoPrincipal,getString(R.string.fallo), Toast.LENGTH_SHORT).show()
            iniciarPatron()
        }
    }
    fun iniciarPatron(){
        for (color in colores){
            color.isEnabled = false
        }
        binding.btnReiniciar.isEnabled=false
        binding.btnSiguiente.isEnabled=false

        var timer = object: CountDownTimer((nivel * 2 * 800).toLong(), 800){
            override fun onTick(millisUntilFinished: Long) {
                if(cont%2 == 0){
                    when(patron[cont2]){
                        1 -> colores[0].setBackgroundColor(getColor(R.color.red))
                        2 -> colores[1].setBackgroundColor(getColor(R.color.green))
                        3 -> colores[2].setBackgroundColor(getColor(R.color.yellow))
                        4 -> colores[3].setBackgroundColor(getColor(R.color.orange))
                    }
                }else{
                    when(patron[cont2]){
                        1 -> colores[0].setBackgroundColor(getColor(R.color.redPalid))
                        2 -> colores[1].setBackgroundColor(getColor(R.color.greenPalid))
                        3 -> colores[2].setBackgroundColor(getColor(R.color.yellowPalid))
                        4 -> colores[3].setBackgroundColor(getColor(R.color.orangePalid))
                    }
                    cont2++
                }
                cont++
            }

            override fun onFinish() {
                cont = 0
                cont2 = 0
                for (color in colores){
                    color.isEnabled = true
                }
                binding.btnReiniciar.isEnabled=true
            }
        }
        timer.start()
    }
    fun guardarModificacion() {
        var user = hashMapOf(
            "usuario" to Almacen.User.usuario,
            "email" to Almacen.User.correo,
            "genero" to Almacen.User.genero,
            "roles" to Almacen.User.rol,
            "Monedas" to Almacen.User.monedas
        )
        db.collection("users")
            .document(Almacen.User.correo)
            .set(user).addOnSuccessListener {
                Log.d(TAG, "crea")
            }
    }
}