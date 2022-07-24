package it.uninsubria.appappunti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ActivityDashboardAdminBinding

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding // lateinit: variabile non inizializzata

    private lateinit var firebaseAuth: FirebaseAuth // firebaseAuth per accedere alle funzioni di autenticazione

    private lateinit var categoryArrayList: ArrayList<ModelCategory> // arraylist per le categorie

    private lateinit var adapterCategory: AdapterCategory // adapter per le categorie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // chiamata alla funzione super
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater) // inflate della view
        setContentView(binding.root) // set della view

        firebaseAuth = FirebaseAuth.getInstance() // inizializzazione firebaseAuth
        checkUser() // chiamata a funzione checkUser
        loadCategories() // chiamata a funzione loadCategories

        binding.searchEtDashboardAdmin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { // funzione per la ricerca
                adapterCategory.filter.filter(s) // filtro delle categorie
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s) // filtro delle categorie
                } catch (e: Exception) {
                    e.printStackTrace() // stampa dell'errore
                }
            }
        })



        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut() // logout
            checkUser() // chiamata a funzione checkUser
        }


        binding.addCategoryBtnDashAdmin.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        binding.addPdfFabDashAdmin.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    private fun loadCategories() {
        categoryArrayList = ArrayList() // inizializzazione arraylist

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear() // svuota l'arraylist
                 for (ds in snapshot.children){ // ciclo per ogni figlio della categoria
                     val model = ds.getValue(ModelCategory::class.java) // oggetto model categoria
                     categoryArrayList.add(model!!) // aggiungi all'arraylist

                 }
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList) // inizializzazione adapter
                binding.categoriesRvDashboardAdmin.adapter = adapterCategory // set dell'adapter
            }

        })
    }


    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser // oggetto firebaseUser
        if(firebaseUser == null){ // se non c'è un utente loggato
            startActivity(Intent(this,MainActivity::class.java)) // avvia la pagina principale
            finish() // chiudi l'activity
        }else { // se c'è un utente loggato
            val email = firebaseUser.email // email dell'utente
            binding.subTitleTv.text= email // set dell'email

        }


    }
}