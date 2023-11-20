package com.example.dungeoncrafter

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.dungeoncrafter.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
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
        setSupportActionBar(binding.toolbar)
        firebaseauth = FirebaseAuth.getInstance()

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

        binding.btnRegistr.setOnClickListener {
            val registerIntent = Intent(this, Registro::class.java)
            startActivity(registerIntent)
        }

        firebaseauth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        binding.btnGoogle.setOnClickListener {
            loginEnGoogle()
        }


    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun loginEnGoogle(){
        //este método es nuestro.
        val signInClient = googleSignInClient.signInIntent
        launcherVentanaGoogle.launch(signInClient)
        //milauncherVentanaGoogle.launch(signInClient)
    }
    private val launcherVentanaGoogle =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        //si la ventana va bien, se accede a las propiedades que trae la propia ventana q llamamos y recogemos en result.
        if (result.resultCode == Activity.RESULT_OK){
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
        //pido un token, y con ese token, si todo va bien obtenga la info.
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

    private fun irMenuPrincipal(email:String, nombre:String = "Usuario"){
        Log.e(TAG,"Valores: ${email}, ${nombre}")
        val homeIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email",email)
            putExtra("nombre",nombre)
        }
        startActivity(homeIntent)
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
    fun guardarUsuario(email: String, user: String) {
        var existe: Boolean = false
        db.collection("users")
            .document(email)
            .get().addOnSuccessListener { document ->
                existe = document != null
            }
        if (existe){
            var user = hashMapOf(
                "usuario" to user,
                "email" to email,
                "roles" to 1,
                "Monedas" to 0
            )


            // Si no existe el documento lo crea, si existe lo remplaza.
            db.collection("users")
                .document(user["email"].toString()) //Será la clave del documento.
                .set(user).addOnSuccessListener {
                    Toast.makeText(this, "Almacenado",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{
                    Toast.makeText(this, "Ha ocurrido un error",Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(this, "Ya esta registrado",Toast.LENGTH_SHORT).show()
        }
    }
    override fun onRestart() {
        super.onRestart()
        recreate()
    }
}