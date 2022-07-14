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

    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var auth: FirebaseAuth

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        auth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Registrazione in corso")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click, goto  previous screen
        binding.backButton.setOnClickListener {
            onBackPressed() // go back to previous screen
        }

        //handle click, begin register
        binding.signupButton.setOnClickListener {
            validateData()
        }


    }

    private var name=""
    private var email=""
    private var password=""

    private fun validateData() {
        name = binding.nameEt.text.toString()
        email = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()
        val cPassword = binding.cpasswordEt.text.toString()
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

    private fun createUserAccount() {
        progressDialog.setMessage("Creazione account in corso...")
        progressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                      updateUserInfo()
                    progressDialog.dismiss()
                    Toast.makeText(baseContext, "Account creato con successo", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    progressDialog.dismiss()
                    Toast.makeText(baseContext, "Account non creato", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun updateUserInfo() {
       progressDialog.setMessage("Aggiornamento dati in corso...")
        val timestamp = System.currentTimeMillis()
        val uid = auth.uid
        progressDialog.show()
        val user = auth.currentUser
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("users")

        ref.child(uid!!).setValue(hashMap)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(baseContext, "Account creato con successo", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity,DashboardUserActivity::class.java))
                    finish()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(baseContext, "Dati non salvati", Toast.LENGTH_SHORT).show()
                }
            }

    }
}