package com.example.dungeoncrafter

import Modelo.Carta
import android.graphics.drawable.Drawable
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
import com.example.dungeoncrafter.databinding.ActivityVistaCartaBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class vistaCarta : AppCompatActivity() {
    lateinit var binding: ActivityVistaCartaBinding
    private lateinit var firebaseauth : FirebaseAuth
    val TAG = "JVVM"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaCartaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var carta = intent.getSerializableExtra("obj") as Carta
        setSupportActionBar(binding.tbCarta)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firebaseauth = FirebaseAuth.getInstance()

        binding.tbCarta.setNavigationOnClickListener {
            finish()
        }

        dibujarCarta(carta)

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
    /**
     * Se encarga de dibujar los elementos de la carta seleccionada en el recyclerView
     * */
    fun dibujarCarta(carta: Carta){
        binding.tvNombre.text = carta.nombre

        val descRecur = this.resources.getStringArray(this.resources.getIdentifier("descripcion","array",this.packageName))
        binding.edtmlDescripcion.setText(descRecur[carta.descripcion])
        val uri1 = "@drawable/"+carta.imagenPersonaje
        val uri2 = "@drawable/"+carta.imagenRelic
        val uri3 = "@drawable/"+carta.imagenTipo
        val imageResource: Int =
            this.resources.getIdentifier(uri1, null, this.packageName)
        var res: Drawable = this.resources.getDrawable(imageResource)
        binding.imgPersonaje.setImageDrawable(res)
        val imageResource2: Int =
            this.resources.getIdentifier(uri2, null, this.packageName)
        var res2: Drawable = this.resources.getDrawable(imageResource2)
        binding.imgRelic.setImageDrawable(res2)
        val imageResource3: Int =
            this.resources.getIdentifier(uri3, null, this.packageName)
        var res3: Drawable = this.resources.getDrawable(imageResource3)
        binding.imgTipo.setImageDrawable(res3)
    }

}