package com.example.dungeoncrafter

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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
import com.example.dungeoncrafter.databinding.ActivityMainBinding
import com.example.dungeoncrafter.databinding.ActivityRegistroBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class Registro : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private lateinit var firebaseauth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val db = Firebase.firestore
    val TAG = "JVVM"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firebaseauth = FirebaseAuth.getInstance()
        binding.toolbar3.setNavigationOnClickListener {
            finish()
        }
        binding.btnRegistr.setOnClickListener {
            if (binding.edEmailReg.text?.isNotEmpty() == true && binding.edPassReg.text?.isNotEmpty() == true ){
                Log.d(TAG,binding.edPassReg.text.toString())
                Log.d(TAG,binding.edConfReg.text.toString())
                if (binding.edPassReg.text.toString() == binding.edConfReg.text.toString()){
                    firebaseauth.createUserWithEmailAndPassword(binding.edEmailReg.text.toString(),binding.edPassReg.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            guardarUsuario()
                            irMenuPrincipal(it.result?.user?.email?:"")
                            Toast.makeText(this, R.string.creado, Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this, R.string.errorCreacion, Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, R.string.errorContrasenas, Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, R.string.datosVacios, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    fun guardarUsuario(){
        var user = hashMapOf(
            "usuario" to binding.edUserReg.text.toString(),
            "email" to binding.edEmailReg.text.toString(),
            "roles" to 0,
            "Monedas" to 0
        )

        // Si no existe el documento lo crea, si existe lo remplaza.
        db.collection("users")
            .document(user.get("email").toString()) //Será la clave del documento.
            .set(user).addOnSuccessListener {
                Toast.makeText(this, "Almacenado",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this, "Ha ocurrido un error",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_1 -> {
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
    private fun irMenuPrincipal(email:String, nombre:String = "Usuario"){
        Log.e(TAG,"Valores: ${email}, ${nombre}")
        val homeIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email",email)
            putExtra("nombre",nombre)
        }
        startActivity(homeIntent)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }


}