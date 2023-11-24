package com.example.dungeoncrafter

import Modelo.Almacen
import Modelo.Carta
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
import java.util.Locale

class MenuPrincipal : AppCompatActivity() {
    lateinit var binding: ActivityMenuPrincipalBinding
    val TAG = "JVVM"
    private lateinit var firebaseauth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseauth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar2)

        Almacen.Cartas = ArrayList()
        Almacen.Cartas.add(Carta("Arthas", "arthas","relic_sacer", "boss_icon","Prueba de descripcion "))
        Almacen.Cartas.add(Carta("Khorne", "khorne","relic_sacer", "boss_icon","Prueba de descripcion "))
        Almacen.Cartas.add(Carta("Morko", "morko","relic_sacer", "boss_icon","Prueba de descripcion "))
        Almacen.Cartas.add(Carta("Nurgle", "nurgle","relic_sacer", "boss_icon","Prueba de descripcion "))
        Almacen.Cartas.add(Carta("Sargeras", "sargeras","relic_sacer", "boss_icon","Prueba de descripcion "))

        binding.btnColeccion.setOnClickListener {
            val coleccionIntent = Intent(this, Coleccion::class.java)
            startActivity(coleccionIntent)
        }
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

        // Puedes reiniciar la actividad actual para aplicar los cambios
        recreate()
    }

    override fun onRestart() {
        super.onRestart()
        if (firebaseauth.currentUser.toString() == "null"){
            finish()
        }
    }
}