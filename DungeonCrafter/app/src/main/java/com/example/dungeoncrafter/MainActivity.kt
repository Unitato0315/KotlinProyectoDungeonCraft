package com.example.dungeoncrafter

import Modelo.Almacen
import Modelo.Carta
import android.content.Intent
import android.content.res.Configuration
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dungeoncrafter.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseauth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    var db =Firebase.firestore
    val TAG = "JVVM"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbLogin)
        firebaseauth = FirebaseAuth.getInstance()
        // Inicio de sesion normal
        binding.btnInicio.setOnClickListener {
            if (binding.edEmail.text?.isNotEmpty() == true && binding.edPass.text?.isNotEmpty() == true ){
                firebaseauth.signInWithEmailAndPassword(binding.edEmail.text.toString(),binding.edPass.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        irMenuPrincipal(it.result?.user?.email?:"")
                        Toast.makeText(this, R.string.inicio, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, R.string.errorInicio, Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, R.string.datosVacios, Toast.LENGTH_SHORT).show()
            }
        }

        //Boton encargado de mandarnos a la actividad de registro
        binding.btnRegistr.setOnClickListener {
            val registerIntent = Intent(this, Registro::class.java)
            startActivity(registerIntent)
        }
        // Nos aseguramos que no haya ninguna sesion iniciada
        firebaseauth.signOut()
        // Configuracion para el inicio de sesion con google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        // Inicio de sesion con google
        binding.btnGoogle.setOnClickListener {
            loginEnGoogle()
        }


    }
    /**
     * Se encarga de configurar el menu
     * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    /**
     * Las siguientes cuatro funciones se encargan de realizar el login con google y validar los
     * datos obtenidos
     * */
    private fun loginEnGoogle(){
        val signInClient = googleSignInClient.signInIntent
        launcherVentanaGoogle.launch(signInClient)
    }

    private val launcherVentanaGoogle =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            manejarResultados(task)
        }
    }

    private fun manejarResultados(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                actualizarUI(account)
            }
        }
        else {
            Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun actualizarUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                guardarUsuario(account.email.toString(), account.displayName.toString())
                Toast.makeText(this,R.string.inicio, Toast.LENGTH_SHORT).show()
                irMenuPrincipal(account.email.toString(), account.displayName.toString())
            }
            else {
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Se lanzar el activity del menu principal
     * */
    private fun irMenuPrincipal(email:String, nombre:String = "Usuario"){
        val homeIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email",email)
            putExtra("nombre",nombre)
        }
        startActivity(homeIntent)
    }
    /**
     * Configuracion del menu
     * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_1 -> {
                //Crea un dialog que contiene un spiner para seleccionar el idioma
                var selectec: Int = 0
                val builder = AlertDialog.Builder(this)
                val inflater = layoutInflater
                builder.setTitle(R.string.menuOpciones)
                val dialogLayout = inflater.inflate(R.layout.dialog_option, null)
                //Cargamos la informacion del spiner y le asignamos que hacer
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
                // Compruebo el idioma actual del dispositivo para asignarlo al spinner
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
    /**
     * Se encarga de cambiar la configuracion del idioma de toda la aplicacion
     * */
    fun cambiarIdioma(pos: Int) {
        var languaje :String = ""
        when(pos){
            0-> languaje = "es"
            1-> languaje = "en"
        }

        val locale = Locale(languaje)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        recreate()
    }
    /**
     * Se encarga de comprobar si el usuario de google existe, en caso negativo lo crea en la base
     * de datos
     * */
    fun guardarUsuario(email: String, user: String) {
        var al = ArrayList<String>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("email",email)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    al.add(document.data.toString())
                }

                launch(Dispatchers.Main) {
                    // Procesa los resultados aquí
                    if (al.size == 0){
                        var user = hashMapOf(
                            "usuario" to user,
                            "email" to email,
                            "genero" to 3,
                            "roles" to 1,
                            "Monedas" to 0
                        )


                        // Si no existe el documento lo crea, si existe lo remplaza.
                        db.collection("users")
                            .document(user["email"].toString())
                            .set(user).addOnSuccessListener {
                                Log.d(TAG, "crea")
                            }
                        crearCartas(email)
                    }else{
                        Log.d(TAG, "No lo crea")
                    }
                }
            } catch (e: Exception) {
                // Maneja errores aquí
                e.printStackTrace()
            }
        }
    }
    /**
     * Se encarga de crear la coleccion de cartas inicial de cada usuario
     * */
    fun crearCartas(email: String){
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
                "user" to email
            )
            db.collection("cartas")
                .add(carta).addOnSuccessListener {
                    Log.e(TAG, "Carta Creada")
                }
        }

    }
    override fun onRestart() {
        super.onRestart()
        recreate()
    }
}