package it.uninsubria.appappunti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ActivityPdfListAdminBinding

class PdfListAdminActivity : AppCompatActivity() { //Activity che mostra la lista dei PDF caricati dall'amministratore


    private lateinit var binding: ActivityPdfListAdminBinding // variabile di tipo ActivityPdfListAdminBinding

    private var categoryId = "" // variabile che contiene l'id della categoria selezionata
    private var category = "" // variabile che contiene il nome della categoria selezionata

    private lateinit var pdfArrayList: ArrayList<ModelPdf> // variabile che contiene l'arraylist di ModelPdf

    private lateinit var pdfAdminAdapter: AdapterPdfAdmin // variabile che contiene l'adapter di AdapterPdfAdmin


    override fun onCreate(savedInstanceState: Bundle?) { // funzione che viene chiamata all'avvio dell'activity
        super.onCreate(savedInstanceState) // chiamata alla funzione super
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater) // chiamata alla funzione inflate di ActivityPdfListAdminBinding
        setContentView(binding.root) // chiamata alla funzione setContentView di ActivityPdfListAdminBinding

        val intent = intent // variabile che contiene l'intent dell'activity
        categoryId = intent.getStringExtra("categoryId")!! // chiamata alla funzione getStringExtra di intent che restituisce l'id della categoria selezionata
        category = intent.getStringExtra("category")!! // chiamata alla funzione getStringExtra di intent che restituisce il nome della categoria selezionata


        binding.tvSubTitle.text = category // chiamata alla funzione text di binding che setta il testo del TextView tvSubTitle con il nome della categoria selezionata

        loadPdfList()// chiamata alla funzione loadPdfList

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //filter data
                try {
                    pdfAdminAdapter.filter!!.filter(s)
                } catch (e: Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }




    private fun loadPdfList() { // funzione che carica la lista dei PDF caricati dall'amministratore
        pdfArrayList = ArrayList() // chiamata alla funzione ArrayList di kotlin
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId) // chiamata alla funzione orderByChild di FirebaseDatabase che ordina i dati in base alla categoria selezionata
            .addValueEventListener(object : ValueEventListener { // chiamata alla funzione addValueEventListener di FirebaseDatabase
                override fun onDataChange(snapshot: DataSnapshot) { // funzione che viene chiamata quando ci sono cambiamenti nel database
                    pdfArrayList.clear() // chiamata alla funzione clear di pdfArrayList
                    for (ds in snapshot.children) { // ciclo for che cicla sui figli di snapshot
                        val model = ds.getValue(ModelPdf::class.java) // chiamata alla funzione getValue di ds che restituisce un ModelPdf
                        if (model != null) { // se model è diverso da null
                            pdfArrayList.add(model) // chiamata alla funzione add di pdfArrayList con model
                        }
                    }
                    pdfAdminAdapter = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList) // chiamata alla funzione AdapterPdfAdmin di AdapterPdfAdmin con this@PdfListAdminActivity e pdfArrayList
                    binding.rvBooks.adapter = pdfAdminAdapter // chiamata alla funzione adapter di binding che setta l'adapter di rvBooks con pdfAdminAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

}