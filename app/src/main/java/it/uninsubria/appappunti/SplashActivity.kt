package it.uninsubria.appappunti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() { //classe che gestisce l'activity di splash

    private lateinit var firebaseAuth: FirebaseAuth //oggetto che gestisce la connessione con il database

    override fun onCreate(savedInstanceState: Bundle?) { //funzione che viene chiamata quando l'activity viene creata
        super.onCreate(savedInstanceState) //chiamata della funzione super
        setContentView(R.layout.activity_splash) //chiamata della funzione setContentView


        Handler().postDelayed(Runnable {
          startActivity(Intent(this, MainActivity::class.java)) //passo alla MainActivity
            finish() //finisco l'activity
                                       },2000) //tempo di attesa in millisecondi
    }

    private fun checkUser(){
        val firebaseUser = FirebaseAuth.getInstance().currentUser //ottengo l'utente corrente
        if(firebaseUser == null){ //se l'utente non è loggato
            startActivity(Intent(this, MainActivity::class.java)) //passo alla MainActivity
            finish()

        }else{
            val firebaseUser= firebaseAuth.currentUser!! //ottengo l'utente corrente
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {


                        val userType = snapshot.child("userType").value
                        if(userType == "user"){

                            startActivity(Intent(this@SplashActivity,DashboardUserActivity::class.java))
                            finish()
                        }else if(userType == "admin"){
                            startActivity(Intent(this@SplashActivity,DashboardAdminActivity::class.java))
                            finish()
                        }
                    }


                })
        }
    }

}