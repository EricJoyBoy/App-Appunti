package it.uninsubria.appappunti

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import it.uninsubria.appappunti.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding // lateinit perché non è ancora stato inizializzato
    private lateinit var firebaseAuth: FirebaseAuth // lateinit perché non è ancora stato inizializzato
    private lateinit var progressDialog: ProgressDialog // lateinit perché non è ancora stato inizializzato
    private var category = "" // variabile di appoggio per salvare il nome della categoria

    override fun onCreate(savedInstanceState: Bundle?) { // funzione che viene chiamata quando l'activity viene creata
        super.onCreate(savedInstanceState) // chiamata della funzione super perché è una classe di Android
        binding = ActivityCategoryAddBinding.inflate(layoutInflater) // inflate della view
        setContentView(binding.root) // set della view

        firebaseAuth = FirebaseAuth.getInstance() // inizializzazione della variabile firebaseAuth
        progressDialog = ProgressDialog(this) // inizializzazione della variabile progressDialog
        progressDialog.setTitle(" Caricamento...") // settaggio del titolo del dialogo
        progressDialog.setCanceledOnTouchOutside(false) // settaggio del dialogo per non essere cancellato quando si clicca fuori dal dialogo

        binding.backBtnAddCategory.setOnClickListener {
            onBackPressed()
        }
        binding.submitBtnAddCategory.setOnClickListener {
            validateData()
            finish()
        }
    }

    private fun validateData() { // funzione per validare i dati
        category = binding.categoryEtAddCategory.text.toString().trim() // settaggio della variabile category con il testo inserito nel campo di testo
        if (category.isEmpty()) {// se la variabile category è vuota
            Toast.makeText(this, "Inserisci un categoria", Toast.LENGTH_LONG).show() // mostra un toast con un messaggio
        } else {
            addCategoryFirebase() // chiamata della funzione addCategoryFirebase
        }
    }

    private fun addCategoryFirebase() { // funzione per aggiungere una categoria nel database
        progressDialog.show() // mostra il dialogo

        val timestamp = System.currentTimeMillis() // inizializzazione della variabile timestamp con il timestamp in millisecondi

        val hashMap = HashMap<String, Any>()// inizializzazione della variabile hashMap
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