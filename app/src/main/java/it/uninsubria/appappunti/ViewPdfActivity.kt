package it.uninsubria.appappunti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import it.uninsubria.appappunti.databinding.ActivityViewPdfBinding

class ViewPdfActivity : AppCompatActivity() {  // Activity per la visualizzazione di un PDF
    private lateinit var binding: ActivityViewPdfBinding // Binding dell'activity

    var bookId = ""  // Id del libro
    override fun onCreate(savedInstanceState: Bundle?) { // Metodo onCreate
        binding = ActivityViewPdfBinding.inflate(layoutInflater) // Inflazione della view
        super.onCreate(savedInstanceState) // Chiamata al metodo onCreate della superclasse
        setContentView(binding.root) // Impostazione della view

        bookId = intent.getStringExtra("bookId")!! // Recupero dell'id del libro dall'intent
        loadBookDetails() // Caricamento dei dettagli del libro

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() { // Metodo per il caricamento dei dettagli del libro
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId) // Recupero della referenza del libro
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val urlPDF = snapshot.child("url").value
                    Log.d("ndt", "onDataChange: url pdf: $urlPDF")

                    loadBookFromUrl("$urlPDF")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(urlPDF: String) { // Metodo per il caricamento del libro dall'url
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(urlPDF) // Recupero della referenza del libro
        reference.getBytes(Constants.MAX_BYTES_PDF) // Recupero dei bytes del libro
            .addOnSuccessListener { bytes ->
                binding.viewPdf.fromBytes(bytes)
                    .swipeHorizontal(false) // Impostazione della visualizzazione del libro in modalità swipe
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1 // Calcolo della pagina corrente
                        binding.tvSubTitle.text = "$currentPage/$pageCount" // Impostazione del testo del sottotitolo
                        Log.d("TAG", "$currentPage/$pageCount") // Log della pagina corrente
                    }
                    .onError {
                        Log.d("TAG", "${it.message}") // Log dell'errore
                    }
                    .onPageError { page, t ->
                        Log.d("TAG", "${t.message}") // Log dell'errore
                    }
                    .load() // Caricamento del libro
                binding.progressBar.visibility = View.GONE // Nascondimento del progress bar
            }
            .addOnFailureListener {

            }
    }
}