package com.example.dungeoncrafter

import Modelo.Almacen
import Modelo.Carta
import Modelo.Users
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dungeoncrafter.databinding.ActivityMenuPrincipalBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class MenuPrincipal : AppCompatActivity() {
    lateinit var binding: ActivityMenuPrincipalBinding
    val TAG = "JVVM"
    private lateinit var firebaseauth : FirebaseAuth
    val db = Firebase.firestore
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var contextoPrincipal: Context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseauth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.tbMenuPrincipal)

        binding.btnColeccion.setOnClickListener {
            cargarCartas()
        }

        binding.btnModificarPrin.setOnClickListener {
            cargarDatosUsuario()
        }
        contextoPrincipal = this
        //supportActionBar?.setDisplayHomeAsUpEnabled(true) //BOTON DE RETROCEDER
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
                val builder = AlertDialog.Builder(this)
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
        recreate()
    }

    fun cargarCartas(){
        Almacen.Cartas = ArrayList()
        db.collection("cartas")
            .whereEqualTo("user",intent.getStringExtra("email"))
            .get()
            .addOnSuccessListener{

                for (document in it){
                    val aux = document.get("description").toString()
                    Almacen.Cartas.add(Carta(document.get("nombre").toString(),document.get("imagen").toString(),document.get("imagenRelic").toString(),document.get("imagenTipo").toString(),aux.toInt()))
                }
            }.addOnCompleteListener{
                moverAColeccion()
            }
    }

    fun cargarDatosUsuario(){
        var users : ArrayList<Users> = ArrayList()
        db.collection("users")
            .whereEqualTo("email",intent.getStringExtra("email"))
            .get()
            .addOnSuccessListener{
                for (document in it){
                    Almacen.User = Users(document.get("usuario").toString(),document.get("roles").toString().toInt(),document.get("email").toString(),document.get("genero").toString().toInt())
                }
            }.addOnCompleteListener{
                    moverAModificar()
            }
    }

    fun moverAModificar() {
        val modificarIntent: Intent = Intent(this, ModificarUsuario::class.java)
        startActivity(modificarIntent)
    }

    fun moverAColeccion(){
        val coleccionIntent = Intent(this, Coleccion::class.java)
        startActivity(coleccionIntent)
    }

    override fun onRestart() {
        super.onRestart()
        if (firebaseauth.currentUser.toString() == "null"){
            finish()
        }
        recreate()
    }

    fun crearCartas(){
        Almacen.Cartas.forEach{ card ->
            var carta = hashMapOf(
                "nombre" to card.nombre,
                "imagen" to card.imagenPersonaje,
                "imagenRelic" to card.imagenRelic,
                "imagenTipo" to card.imagenTipo,
                "description" to card.descripcion,
                "user" to intent.getStringExtra("email").toString()
            )
            db.collection("cartas")
                .add(carta).addOnSuccessListener {
                    Log.e(TAG, "Carta Creada")
                }
        }
    }
}