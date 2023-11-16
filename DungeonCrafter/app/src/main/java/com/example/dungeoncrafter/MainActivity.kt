package com.example.dungeoncrafter

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dungeoncrafter.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var firebaseauth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
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
                        Toast.makeText(this, "Conexión correcta", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Conexión erronea", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Conexión no establecida", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Vacio", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegistr.setOnClickListener {
            if (binding.edEmail.text?.isNotEmpty() == true && binding.edPass.text?.isNotEmpty() == true ){
                firebaseauth.createUserWithEmailAndPassword(binding.edEmail.text.toString(),binding.edPass.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this, "Conexión correcta", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Conexión erronea", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Conexión no establecida", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Vacio", Toast.LENGTH_SHORT).show()
            }
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
        //pido un token, y con ese token, si todo va bien obtengo la info.
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                //hacer account. y ver otras propiedades interesantes.
                Toast.makeText(this,"Se loggea con google", Toast.LENGTH_SHORT).show()

            }
            else {
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}