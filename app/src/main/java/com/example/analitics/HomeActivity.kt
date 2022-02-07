package com.example.analitics

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import com.example.analitics.databinding.ActivityHomeBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.oAuthProvider


enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEEBOOK

}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //Setup
        val bundle = intent.extras
        val email =bundle?.getString("email")
        val provider =bundle?.getString("provider")

        setup(email?:"", provider?: "")

        //  GUARDADO DE DATOS

        val prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider", provider)
        prefs.apply()

    }
    private fun setup(email: String, provider: String){
        title = "Inicio"

        binding.emailTextView.text = email
        binding.providerTextView.text = provider

        binding.logOutButton.setOnClickListener{

            // Borrado de datos
            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            if (provider == ProviderType.FACEEBOOK.name){
                LoginManager.getInstance().logOut()
            }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }


    }
}