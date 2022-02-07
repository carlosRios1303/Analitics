package com.example.analitics

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.analitics.databinding.ActivityAuthBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SING_IN = 100
    private val callbackManager =CallbackManager.Factory.create()

    private lateinit var authBinding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(authBinding.root)


        // Se lanza un evento a google analytics
        val analytics =FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion a firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //setup
        setup()

        // session
        session()


    }

    override fun onStart() {
        super.onStart()
        authBinding.authLayout.visibility = View.VISIBLE
    }

    private fun session(){

        // accedemos a las preferencias  y nos devuelve los datos guardados
        val prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        // validamos si existe un email y un provider, en caso de no tener null nos lleva a la funcion showHome
        if (email != null && provider != null){
            authBinding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup(){
        title = "Autentificacion"
        authBinding.singButton.setOnClickListener{
            if (authBinding.emailEditText.text.isNotEmpty() && authBinding.passEdiText.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(authBinding.emailEditText.text.toString(),
                    authBinding.passEdiText.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            showHome(it.result?.user?.email ?: "",ProviderType.BASIC)

                        }else{
                            showAlert()

                        }
                }

            }
        }
        authBinding.loginButton.setOnClickListener {
            if (authBinding.emailEditText.text.isNotEmpty() && authBinding.passEdiText.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(authBinding.emailEditText.text.toString(),
                    authBinding.passEdiText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)

                    }else{
                        showAlert()

                    }
                }

            }

        }

        authBinding.googleButton.setOnClickListener {
            // Configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken( getString(R.string.default_web_client_idd))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SING_IN)
        }
        authBinding.facebookButton.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult>{
                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException) {
                        showAlert()
                    }

                    override fun onSuccess(result: LoginResult) {
                       result?.let {
                           val token = it.accessToken

                           val credencial = FacebookAuthProvider.getCredential(token.token)


                           FirebaseAuth.getInstance().signInWithCredential(credencial)
                               .addOnCompleteListener {
                                   if (it.isSuccessful) {
                                       showHome(it.result?.user?.email ?: "", ProviderType.FACEEBOOK)
                                   } else {
                                       showAlert()
                                   }

                               }

                       }
                    }

                })

        }
    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog:AlertDialog = builder.create()
        dialog.show()
    }
    private fun showHome(email: String, provider: ProviderType){
        // buscar cual es la diferencia entre un intent explicito y uno implicito
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SING_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {


                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    val credencial = GoogleAuthProvider.getCredential(account.idToken, null)


                    FirebaseAuth.getInstance().signInWithCredential(credencial)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(it.result?.user?.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert()
                            }

                        }


                }
            } catch (e: ApiException){
                showAlert()
            }


        }
    }

}



