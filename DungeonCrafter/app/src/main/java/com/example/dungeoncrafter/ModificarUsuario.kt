package com.example.dungeoncrafter

import Auxiliar.Conexion
import Modelo.Almacen
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dungeoncrafter.databinding.ActivityModificarUsuarioBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ModificarUsuario : AppCompatActivity() {
    lateinit var binding: ActivityModificarUsuarioBinding
    val TAG = "JVVM"
    private lateinit var firebaseauth : FirebaseAuth
    val db = Firebase.firestore
    var gen = 0
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var contextoPrincipal: Context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firebaseauth = FirebaseAuth.getInstance()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        // Comprueba el rol del usuario
        if (Almacen.User.rol == 1){
            binding.cbContrasena.visibility = View.INVISIBLE
        }

        binding.edUserReg.setText(Almacen.User.usuario)
        // Comprueba y selecciona el genero del usuario
        when(Almacen.User.genero){
            1 -> binding.rbMale.isChecked = true
            2 -> binding.rbFemale.isChecked = true
            3 -> binding.rbOther.isChecked = true
        }

        //Para activar o desactivar la modificacion de usuarios
        binding.cbContrasena.setOnClickListener {
            if(binding.cbContrasena.isChecked){
                binding.tfPassReg.visibility = View.VISIBLE
                binding.tfConfReg.visibility = View.VISIBLE
                binding.textView7.visibility = View.VISIBLE
                binding.textView8.visibility = View.VISIBLE
            }else{
                binding.tfPassReg.visibility = View.INVISIBLE
                binding.tfConfReg.visibility = View.INVISIBLE
                binding.textView7.visibility = View.INVISIBLE
                binding.textView8.visibility = View.INVISIBLE
            }
        }

        binding.rgGen.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.rbMale ->{
                    gen = 1
                }
                R.id.rbFemale ->{
                    gen = 2
                }
                R.id.rbOther ->{
                    gen = 3
                }
            }
        }
        // Se encarga de eliminar el usuario actual totalmente
        binding.btnEliminar.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            // Creamos un dialog para asegurarnos que el usuario quiere eliminarlo
            with(builder)
            {
                setTitle(R.string.tituloEliminar)
                setMessage(R.string.mensajeEliminar)
                setPositiveButton(R.string.borrar, DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->
                    // Elimina el usuario de firebaseAuth
                    val user = Firebase.auth.currentUser!!
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User account deleted.")
                            }
                        }

                    eliminarUsuario()
                    firebaseauth.signOut()

                    val signInClient = Identity.getSignInClient(contextoPrincipal)
                    signInClient.signOut()

                    finish()
                }))
                setNeutralButton(R.string.cancelar, null)
                show()
            }


        }
        // Se encarga de comprobar si hay algun valor modificado
        binding.btnModificar.setOnClickListener {

            if (Almacen.User.usuario != binding.edUserReg.text.toString() || Almacen.User.genero != gen){
                guardarModificacion()
                if (binding.cbContrasena.isChecked){
                    if(binding.edPassReg.text.toString() == binding.edConfReg.text.toString() && binding.edPassReg.text.toString().isNotEmpty()){
                        cambiarContraseña()
                        finish()
                    }else{
                        Toast.makeText(this,R.string.errorContrasenas, Toast.LENGTH_SHORT).show()
                    }
                }
            }else if (binding.cbContrasena.isChecked){
                if(binding.edPassReg.text.toString() == binding.edConfReg.text.toString() && binding.edPassReg.text.toString().isNotEmpty()){
                    cambiarContraseña()
                    finish()
                }else{
                    Toast.makeText(this,R.string.errorContrasenas, Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,R.string.noCambios, Toast.LENGTH_SHORT).show()
            }
        }

        contextoPrincipal = this
    }
    /**
     * Se encarga de cambiar la contraseña del usuario en firebaseAuth
     * */
    private fun cambiarContraseña() {
        val user = Firebase.auth.currentUser
        user!!.updatePassword(binding.edPassReg.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User password updated.")
                }
            }
    }
    /**
     * Elimina al usuario de la base de datos y borra todas las cartas asignadas al mismo
     * */
    private fun eliminarUsuario() {
        db.collection("users")
            .whereEqualTo("email", Almacen.User.correo)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("users")
                        .document(document.id)
                        .delete()
                        .toString()
                }
            }
        db.collection("cartas")
            .whereEqualTo("user", Almacen.User.correo)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("cartas")
                        .document(document.id)
                        .delete()
                        .toString()
                }
            }
        Conexion.delConfiguracion(this, Almacen.Configuracion.usuario)
    }
    /**
     * Guarda la modificacion de datos realizada
     * */
    fun guardarModificacion() {
        var user = hashMapOf(
            "usuario" to binding.edUserReg.text.toString(),
            "email" to Almacen.User.correo,
            "genero" to gen,
            "roles" to Almacen.User.rol,
            "Monedas" to 0
        )
        db.collection("users")
            .document(Almacen.User.correo)
            .set(user).addOnSuccessListener {
                Log.d(TAG, "crea")
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu2, menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_1 -> {
                firebaseauth.signOut()
                val signInClient = Identity.getSignInClient(this)
                signInClient.signOut()
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
        recreate()
    }
}