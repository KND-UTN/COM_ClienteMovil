package com.example.wall_e

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {

        //SplashScreen
        Thread.sleep(3000)
        setTheme(R.style.Theme_Walle)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //setup
        setup()

        session()
    }

    override fun onStart() {
        super.onStart()

        authLayout.visibility = View.VISIBLE
    }

    private fun session()
    {
        val pref = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE)
        val email = pref.getString("email", null)
        val provider = pref.getString("provider", null)

        if (email != null && provider != null)
        {
            authLayout.visibility = View.INVISIBLE
            pasarPantallaPrincipal(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup(){

        singInButton.setOnClickListener {
            if (editTextTextEmailAddress.text.isNotEmpty() && editTextTextPassword.text.isNotEmpty())
            {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextTextEmailAddress.text.toString(),
                        editTextTextPassword.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        pasarPantallaPrincipal(it.result?.user?.email ?: "" , ProviderType.BASIC)
                    }
                    else
                    {
                        showAlert()
                    }
                }
            }
        }

        ingresarButton.setOnClickListener {
            if (editTextTextEmailAddress.text.isNotEmpty() && editTextTextPassword.text.isNotEmpty())
            {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextTextEmailAddress.text.toString(),
                        editTextTextPassword.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        pasarPantallaPrincipal(it.result?.user?.email ?: "" , ProviderType.BASIC)
                    }
                    else
                    {
                        showAlert()
                    }
                }
            }
        }

        googleButton.setOnClickListener {
            val googleConfiguracion = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

            val googleCliente = GoogleSignIn.getClient(this, googleConfiguracion)
            googleCliente.signOut()

            startActivityForResult(googleCliente.signInIntent, GOOGLE_SIGN_IN)
        }

    }

    private fun showAlert()
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Esta función me permite pasar a la pantalla de inicio desde el login
    private fun pasarPantallaPrincipal(email: String, provider: ProviderType )
    {
        val homeIntent = Intent(this, MainActivity::class.java).apply{
            putExtra("email", email )
            putExtra("provider", provider.name )
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN)
        {
            val respuesta = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val cuenta = respuesta.getResult(ApiException::class.java)

                if (cuenta != null)
                {
                    val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credencial).addOnCompleteListener {
                        if (it.isSuccessful) {
                            pasarPantallaPrincipal(cuenta.email ?: "", ProviderType.GOOGLE)
                        } else {
                            showAlert()
                        }
                    }
                }
            }
            catch (e: ApiException)
            {
                showAlert()
            }

        }
    }


}