package com.example.dungeoncrafter

import Auxiliar.Conexion
import Modelo.Almacen
import Modelo.Carta
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dungeoncrafter.databinding.ActivityVistaCartaBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import java.io.File
import java.util.Locale

class vistaCarta : AppCompatActivity() {
    lateinit var binding: ActivityVistaCartaBinding
    private lateinit var firebaseauth : FirebaseAuth
    var storage = Firebase.storage
    var storageRef = storage.reference
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
    /**
     * Se encarga de dibujar los elementos de la carta seleccionada en el recyclerView
     * */
    fun dibujarCarta(carta: Carta){
        binding.tvNombre.text = carta.nombre

        val descRecur = this.resources.getStringArray(this.resources.getIdentifier("descripcion","array",this.packageName))
        binding.edtmlDescripcion.setText(descRecur[carta.descripcion])
        //val uri1 = "@drawable/"+carta.imagenPersonaje
        val uri2 = "@drawable/"+carta.imagenRelic
        val uri3 = "@drawable/"+carta.imagenTipo
        //val imageResource: Int =
        //    this.resources.getIdentifier(uri1, null, this.packageName)
        //var res: Drawable = this.resources.getDrawable(imageResource)
        var spaceRef = storageRef.child("cartas/${carta.imagenPersonaje}.jpg")

        val localfile  = File.createTempFile("tempImage","jpg")
        spaceRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.imgPersonaje.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(this,"Algo ha fallado en la descarga", Toast.LENGTH_SHORT).show()
        }
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