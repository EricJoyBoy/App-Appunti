package it.uninsubria.appappunti

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import android.text.format.DateFormat
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() { // classe che gestisce l'applicazione

    override fun onCreate() { // funzione che viene chiamata all'avvio dell'applicazione
        super.onCreate() // chiamata alla funzione della superclasse
    }

    companion object { // classe interna che gestisce l'applicazione
        fun formatTimeStamp(timestamp: Long): String { // funzione che formatta il timestamp in una stringa
            val cal = Calendar.getInstance(Locale.ENGLISH) // oggetto calendario
            cal.timeInMillis = timestamp // impostazione del timestamp
            //format
            return DateFormat.format("dd/MM/yyyy", cal).toString() // restituzione della stringa formattata
        }

        fun loadPdfSize(pdfUrl: String, PdfTitle: String, tvSize: TextView) { // funzione che carica la dimensione del pdf
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl) // oggetto di riferimento al file
            ref.metadata.addOnSuccessListener {
                val bytes = it.sizeBytes.toDouble() // dimensione del file in byte
                val kb = bytes / 1024 // dimensione del file in kb
                val mb = kb / 1024 // dimensione del file in mb
                if (mb > 1) {
                    tvSize.text = "${String.format("%.2f", mb)} + MB" // formattazione della stringa
                } else if (kb >= 1) {
                    tvSize.text = "${String.format("%.2f", kb)} + KB" // formattazione della stringa
                } else {
                    tvSize.text = "${String.format("%.2f", bytes)} + bytes" // formattazione della stringa
                }
            }.addOnFailureListener {

            }
        }

        fun loadPdfFromUrlSinglePage( // funzione che carica il pdf in una pagina singola
            pdfUrl: String, // url del file
            pdfTitle: String, // titolo del file
            pdfView: PDFView, // oggetto di riferimento al pdf
            progressBar: ProgressBar, // oggetto di riferimento al progress bar
            tvPages: TextView? // oggetto di riferimento al text view che mostra il numero di pagine
        ) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl) // oggetto di riferimento al file
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->

                pdfView.fromBytes(bytes) // caricamento del pdf
                    .pages(0) // caricamento della pagina singola
                    .spacing(0) // spaziatura del pdf
                    .swipeHorizontal(false) // abilitazione del swipe orizzontale
                    .enableSwipe(false) //  disabilitazione del swipe
                    .onError {
                        progressBar.visibility = View.INVISIBLE // nascondi il progress bar
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE // nascondi il progress bar
                    }
                    .onLoad { nbPages ->
                        progressBar.visibility = View.INVISIBLE

                        if (tvPages != null) {
                            tvPages.text = "$nbPages"
                        }
                    }.load()
            }.addOnFailureListener {
                //Log.d("ndt", "failed")
            }
        }

        fun loadCategory(categoryId: String, tvCategory: TextView) { // funzione che carica la categoria
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener { // listener per il caricamento della categoria
                override fun onDataChange(snapshot: DataSnapshot) { // funzione che viene chiamata quando cambia il valore della categoria
                    val category: String = "${snapshot.child("category").value}" // recupero del valore della categoria

                    tvCategory.text = category
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) { // funzione che elimina il libro
            val progressDialog = ProgressDialog(context) // oggetto di riferimento al progress dialog
            progressDialog.setTitle("Cancellazioine del File") //   impostazione del titolo del progress dialog
            progressDialog.setMessage("Cancellazione $bookTitle") // impostazione del messaggio del progress dialog
            progressDialog.setCanceledOnTouchOutside(false) // impostazione della cancellazione al tocco esterno
            progressDialog.show() // mostra il progress dialog

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl) // oggetto di riferimento al file
            storageReference.delete() // cancellazione del file
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Cancellazione andata a buon fine", Toast.LENGTH_SHORT)
                                .show()

                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Connessione Fallita al Database", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Fallita la connessione allo Storage", Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementBookViewCount(bookId: String) { // funzione che incrementa il contatore di visualizzazione del libro
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
            ref.child(bookId) // oggetto di riferimento al file
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get luot xem
                        var viewCount = "${snapshot.child("viewsCount").value}" // recupero del valore del contatore di visualizzazione

                        if (viewCount == "" || viewCount == "null") { // se il contatore di visualizzazione è vuoto
                            viewCount = "0" // impostazione del contatore di visualizzazione a 0
                        }
                        val newViewCount = viewCount.toLong() + 1 // incremento del contatore di visualizzazione

                        val hashMap = HashMap<String, Any>() // mappa di riferimento
                        hashMap["viewsCount"] = newViewCount // impostazione del contatore di visualizzazione

                        val dbRef = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
                        dbRef.child(bookId) // oggetto di riferimento al file
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun removeFromFavorite(context: Context, bookId: String) { // funzione che rimuove il libro dai preferiti

            val firebaseAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Rimosso dai preferiti", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Fallimento ", Toast.LENGTH_SHORT).show()
                }
        }
    }
}