package it.uninsubria.appappunti

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ActivityDashboardUserBinding
import it.uninsubria.appappunti.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init firebase auth
        auth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Registrazione in corso")
        progressDialog.setCanceledOnTouchOutside(false)

        //handler click, not have account, goto register screen
        binding.signUpTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        //handle click, login user
        binding.loginBtn.setOnClickListener {

            validateData()

        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Format Email non valido",Toast.LENGTH_LONG).show()


        }else if(password.isEmpty()){
            Toast.makeText(this,"Password non inserita",Toast.LENGTH_LONG).show()
        } else {
            loginUser()
        }

    }

    private fun loginUser() {

        progressDialog.setMessage("Login in corso...")
        progressDialog.show()

        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                checkUser()
            }

            .addOnFailureListener{ e->
progressDialog.dismiss()
                Toast.makeText(this,"Login fallito causato da ${e.message}",Toast.LENGTH_LONG).show()

            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Controllo utente...")
        val firebaseUser= auth.currentUser!!
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(error:DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginActivity,"Errore: ${error.message}",Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                        progressDialog.dismiss()

                         val userType = snapshot.child("userType").value
                    if(userType == "user"){

                        startActivity(Intent(this@LoginActivity,DashboardUserActivity::class.java))
                        finish()
                    }else if(userType == "admin"){
                        startActivity(Intent(this@LoginActivity,DashboardAdminActivity::class.java))
                        finish()
                    }
                    }


            })

    }
}