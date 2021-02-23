package com.example.wall_e

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.net.Socket
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

enum class ProviderType
{
    BASIC,
    GOOGLE
}

class MainActivity : AppCompatActivity() {
    private val RQ_SPEECH_REC = 102
    lateinit var texto : TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.desplegable)


        val bundle = intent.extras
        val email =bundle?.getString("email")
        val provider= bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardado de datos
        val pref = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit()
        pref.putString("email", email)
        pref.putString("provider", provider)
        pref.apply()

        toolbar.setNavigationOnClickListener {
            val mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
            mDrawerLayout.openDrawer(Gravity.LEFT)
        }


        // Con estas lineas programamos los botones del coso desplazable
        val drawerlay : DrawerLayout = findViewById(R.id.drawer_layout)
        val nav : NavigationView = drawerlay.findViewById(R.id.nav_view)
        var pantallaFondo : ViewStub = findViewById(R.id.layout_stub)
        pantallaFondo.layoutResource = R.layout.layout_vacio
        var ultimo = pantallaFondo.inflate()
        ultimo = cargarMain(ultimo)
        nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    ultimo = cargarMain(ultimo)
                    true
                }
                R.id.nav_gallery -> {
                    ultimo = cargarMasInfo(ultimo)
                    true
                }
                else -> false
            }
        }

        //codigo del boton acelerar
        btnAcelerar.setOnTouchListener {
            v, event ->
            if (event.action == MotionEvent.ACTION_DOWN)
            {
                enviar_mensaje("acelerar");
                btnAcelerar.setBackgroundResource(R.drawable.icon_pause);
            }
            else if (event.action == MotionEvent.ACTION_UP)
            {
                enviar_mensaje("frenar");
                btnAcelerar.setBackgroundResource(R.drawable.icon_move2);
            }
            false
        }


    }
    

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK){
            val result =data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val resultado = result?.get(0).toString()
            texto.text = resultado
            enviar_mensaje(resultado)
        }
    }

    private fun askSpeechInput(){
        if (!SpeechRecognizer.isRecognitionAvailable(this)){
            Toast.makeText(this, "No fue posible", Toast.LENGTH_SHORT).show()
        }
        else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "di algo!")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }

    //una función para enviar mensajes al servidor
    fun enviar_mensaje(mensaje: String){
        try {
            val s = Socket("192.168.1.8", 25000)
            s.outputStream.write("Movil".toByteArray())
            s.outputStream.write(mensaje.toByteArray())
            s.close()
        }
        catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun setup(email: String, provider: String)
    {
        val navigationView : NavigationView = findViewById(R.id.nav_view)
        val headerView : View = navigationView.getHeaderView(0)
        val navUsername : TextView = headerView.findViewById(R.id.desplazable_mail)
        val btnCerrarSesion : Button = navigationView.findViewById(R.id.cerrarSesionButton)
        navUsername.text = email

        btnCerrarSesion.setOnClickListener {

            //borrado de datos
            val pref = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit()
            pref.clear()
            pref.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }

    fun cargarMain(ultimo: View): View? {
        ultimo.visibility = View.GONE
        val vs = ViewStub(this)
        vs.layoutResource = R.layout.activity_main
        val cl = findViewById<CoordinatorLayout>(R.id.contenedor)
        cl.addView(vs)
        val inflado = vs.inflate()

        // Definimos los botones y textos
        val botonAudio : Button = inflado.findViewById(R.id.btn_button)
        texto = inflado.findViewById(R.id.tv_text)

        botonAudio.setOnClickListener{
            askSpeechInput()
        }
        var like = false

        val botonAnimacion : ImageView = inflado.findViewById(R.id.likeImageView)
        botonAnimacion.setOnClickListener {
            like = likeAnimation(likeImageView, R.raw.eva_robot, like)
        }
        return inflado

    }

    fun cargarMasInfo(ultimo: View): View? {
        ultimo.visibility = View.GONE
        val vs = ViewStub(this)
        vs.layoutResource = R.layout.more_info
        val cl = findViewById<CoordinatorLayout>(R.id.contenedor)
        cl.addView(vs)
        val inflado = vs.inflate()
        btn_button.setOnClickListener{
            askSpeechInput()
        }
        var like = false

        likeImageView.setOnClickListener {
            like = likeAnimation(likeImageView, R.raw.eva_robot, like)
        }
        return inflado
    }

    private fun likeAnimation(imageView: LottieAnimationView, animation: Int, like: Boolean) : Boolean {
        if (!like){
            imageView.setAnimation(animation)
            imageView.repeatCount = 2
            imageView.playAnimation()
        }
        else{
            imageView.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(object: AnimatorListenerAdapter(){
                        override fun onAnimationEnd(animator: Animator){
                            imageView.setImageResource(R.drawable.icon_walle32)
                            imageView.alpha = 1f
                        }
                    })

        }

        return !like
    }
}