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

class LoginActivity : AppCompatActivity() { //Activity che permette di effettuare il login

    private lateinit var binding: ActivityLoginBinding //Binding per la vista di questa activity

    private lateinit var auth: FirebaseAuth //FirebaseAuth per effettuare il login

    private lateinit var progressDialog: ProgressDialog //Dialog per mostrare lo stato di caricamento dell'applicazione

    override fun onCreate(savedInstanceState: Bundle?) { //Funzione che viene chiamata all'avvio dell'activity
        super.onCreate(savedInstanceState) //Chiamata alla funzione superclasse
        binding = ActivityLoginBinding.inflate(layoutInflater) //Inflazione della vista di questa activity
        setContentView(binding.root) //Impostazione della vista di questa activity


        auth = FirebaseAuth.getInstance() //Inizializzazione di FirebaseAuth

        progressDialog = ProgressDialog(this) //Inizializzazione di un dialog per mostrare lo stato di caricamento dell'applicazione
        progressDialog.setTitle("Registrazione in corso") //Impostazione del titolo del dialog
        progressDialog.setCanceledOnTouchOutside(false)//Impostazione che il dialog non può essere chiuso quando viene toccato l'esterno dello schermo

        binding.signUpTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {

            validateData()

        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() { //Funzione che valida i dati inseriti dall'utente

        email = binding.emailEt.text.toString().trim() //Prende il valore dell'edit text relativo all'email
        password = binding.passwordEt.text.toString().trim() //Prende il valore dell'edit text relativo alla password

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){ //Controlla che l'email sia valida
            Toast.makeText(this,"Format Email non valido",Toast.LENGTH_LONG).show() //Mostra un messaggio di errore


        }else if(password.isEmpty()){ //Controlla che la password non sia vuota
            Toast.makeText(this,"Password non inserita",Toast.LENGTH_LONG).show() //Mostra un messaggio di errore
        } else {
            loginUser() //Chiamata alla funzione che effettua il login
        }

    }

    private fun loginUser() { //Funzione che effettua il login dell'utente

        progressDialog.setMessage("Login in corso...") //Impostazione del messaggio del dialog
        progressDialog.show() //Mostra il dialog

        auth.signInWithEmailAndPassword(email,password) //Effettua il login dell'utente con i dati inseriti
            .addOnSuccessListener {
                checkUser() //Chiamata alla funzione che controlla se l'utente è un docente o uno studente
            }

            .addOnFailureListener{ e->
progressDialog.dismiss() //Chiusura del dialog
                Toast.makeText(this,"Login fallito causato da ${e.message}",Toast.LENGTH_LONG).show() //Mostra un messaggio di errore

            }
    }

    private fun checkUser() { //Funzione che controlla se l'utente è un docente o uno studente
        progressDialog.setMessage("Controllo utente...") //Impostazione del messaggio del dialog
        val firebaseUser= auth.currentUser!! //Prende l'utente corrente
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