package it.uninsubria.appappunti

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import it.uninsubria.appappunti.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(" Caricamento...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtnAddCategory.setOnClickListener {
            onBackPressed()
        }
        binding.submitBtnAddCategory.setOnClickListener {
            validateData()
            finish()
        }
    }

    private fun validateData() {
        category = binding.categoryEtAddCategory.text.toString().trim()
        if (category.isEmpty()) {
            Toast.makeText(this, "Inserisci un categoria", Toast.LENGTH_LONG).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = category
        hashMap["timestamp"] = timestamp
        hashMap["category"] = category
        hashMap["uid"] = "${firebaseAuth.uid}"

        FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("categories").child(category).setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Categoria aggiunta", Toast.LENGTH_LONG).show()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Errore", Toast.LENGTH_LONG).show()
                }
            }
    }

}