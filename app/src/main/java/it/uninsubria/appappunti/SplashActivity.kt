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

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler().postDelayed(Runnable {
          startActivity(Intent(this, MainActivity::class.java)) //passo alla MainActivity
            finish() //finisco l'activity
                                       },2000) //tempo di attesa in millisecondi
    }

    private fun checkUser(){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }else{
            val firebaseUser= firebaseAuth.currentUser!!
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {


                        val userType = snapshot.child("userType").value
                        if(userType == "user"){

                            startActivity(Intent(this@SplashActivity,DashboardUserActivity::class.java))
                        }else if(userType == "admin"){
                            startActivity(Intent(this@SplashActivity,DashboardAdminActivity::class.java))
                            finish()
                        }
                    }


                })
        }
    }

}