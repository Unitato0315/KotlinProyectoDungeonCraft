package com.example.dungeoncrafter

import Adaptadores.AdaptadorCartas
import Auxiliar.Conexion
import Modelo.Almacen
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dungeoncrafter.databinding.ActivityColeccionBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale


class Coleccion : AppCompatActivity() {
    lateinit var binding: ActivityColeccionBinding
    lateinit var miRecyclerView : RecyclerView
    val TAG = "JVVM"
    private lateinit var firebaseauth : FirebaseAuth

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var contextoPrincipal: Context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColeccionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbCollection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //BOTON DE RETROCEDER
        binding.tbCollection.setNavigationOnClickListener {
            finish()
        }
        firebaseauth = FirebaseAuth.getInstance()
        // Creamos el recyclerView
        miRecyclerView = binding.recycledCartas
        miRecyclerView.setHasFixedSize(true)
        miRecyclerView.layoutManager = GridLayoutManager(this, 2)

        var miAdapter = AdaptadorCartas(Almacen.Cartas,this)

        miRecyclerView.adapter = miAdapter

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
                Log.d(TAG,languageCode.toString())

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

    override fun onRestart() {
        super.onRestart()
        if (firebaseauth.currentUser.toString() == "null"){
            finish()
        }
        recreate()
    }

}