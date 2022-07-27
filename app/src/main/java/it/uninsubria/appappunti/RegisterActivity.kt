package it.uninsubria.appappunti

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import it.uninsubria.appappunti.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding // lateinit var = non inizializzato

    private lateinit var auth: FirebaseAuth // lateinit var = non inizializzato

    private lateinit var progressDialog: ProgressDialog // lateinit var = non inizializzato

    override fun onCreate(savedInstanceState: Bundle?) { // override fun = sovrascrive un metodo di una classe superiore
        super.onCreate(savedInstanceState) // super = chiamo il metodo della classe superiore
        binding = ActivityRegisterBinding.inflate(layoutInflater) // inflate = crea una vista da un layout xml
        setContentView(binding.root) // setContentView = setta la vista dell'activity

        auth = FirebaseAuth.getInstance() // auth = istanza di FirebaseAuth

        progressDialog = ProgressDialog(this) // progressDialog = istanza di ProgressDialog
        progressDialog.setTitle("Registrazione in corso") // progressDialog.setTitle = setta il titolo del dialogo
        progressDialog.setCanceledOnTouchOutside(false) // progressDialog.setCanceledOnTouchOutside = setta se il dialogo può essere chiuso con un tocco

        binding.backButton.setOnClickListener {
            onBackPressed() // onBackPressed = funzione che fa tornare indietro all'activity precedente
        }

        binding.signupButton.setOnClickListener {
            validateData()
        }


    }

    private var name=""
    private var email=""
    private var password=""

    private fun validateData() {
        //input dei dati
        name = binding.nameEt.text.toString().trim() // name = valore del campo nameEt
        email = binding.emailEt.text.toString().trim() // email = valore del campo emailEt
        password = binding.passwordEt.text.toString().trim() // password = valore del campo passwordEt
        val cPassword = binding.cpasswordEt.text.toString().trim() // cPassword = valore del campo cpasswordEt
        if(name.isEmpty()){
            Toast.makeText(this,"Inserisci un nome", Toast.LENGTH_SHORT).show()

        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Inserisci un indirizzo email valido", Toast.LENGTH_SHORT).show()

        }else if(password.isEmpty()){
            Toast.makeText(this,"Inserisci una password", Toast.LENGTH_SHORT).show()
        }else if(cPassword.isEmpty()){
            Toast.makeText(this,"Conferma la password", Toast.LENGTH_SHORT).show()

        }else if(password != cPassword){
            Toast.makeText(this,"Le password non coincidono", Toast.LENGTH_SHORT).show()

    }else{
        createUserAccount()
        }

}

    private fun createUserAccount() { // funzione che crea un account utente
        progressDialog.setMessage("Creazione account in corso...") // progressDialog.setMessage = setta il messaggio del dialogo
        progressDialog.show() // progressDialog.show = mostra il dialogo

        auth.createUserWithEmailAndPassword(email, password) // auth.createUserWithEmailAndPassword = crea un account con email e password
            .addOnSuccessListener{
                updateUserInfo()

            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this,"Creazione Account fallita a causa di ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateUserInfo() { //

       progressDialog.setMessage("Aggiornamento dati in corso...")
        progressDialog.show()
        val timestamp = System.currentTimeMillis()

        val uid = auth.uid


        val user = auth.currentUser
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")

        ref.child(uid!!).setValue(hashMap)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Registrazione fallita", Toast.LENGTH_SHORT).show()
                }
            }

    }
}