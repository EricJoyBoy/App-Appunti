package it.uninsubria.appappunti

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ActivityEditPdfBinding

class EditPdfActivity : AppCompatActivity() { //Activity che permette di modificare un pdf già presente nel database
    private lateinit var binding: ActivityEditPdfBinding //Binding per la vista

    private lateinit var progressDialog: ProgressDialog //Dialog che mostra lo stato della richiesta di modifica

    private lateinit var categoryTitleArrayList: ArrayList<String> //Lista di stringhe che contiene i titoli delle categorie presenti nel database

    private lateinit var categoryIdArrayList: ArrayList<String> //Lista di stringhe che contiene gli id delle categorie presenti nel database

    //book id get from intent started from PDFAdminAdapter
    private var bookId = "" //id del libro che si sta modificando
    override fun onCreate(savedInstanceState: Bundle?) { //Funzione che viene chiamata all'avvio dell'activity
        binding = ActivityEditPdfBinding.inflate(layoutInflater) //Inflazione della vista
        super.onCreate(savedInstanceState) //Chiamata alla funzione super
        setContentView(binding.root) //Impostazione della vista

        bookId = intent.getStringExtra("bookId")!! //Prende l'id del libro da modificare dall'intent
        progressDialog = ProgressDialog(this) //Creazione del dialog che mostra lo stato della richiesta di modifica
        progressDialog.setTitle("Titolo..") //Impostazione del titolo del dialog
        progressDialog.setCanceledOnTouchOutside(false) //Impostazione che impedisce all'utente di chiudere il dialog con il touch dello schermo

        loadCategories() //Chiamata alla funzione che carica le categorie dal database
        loadBookInfo() //Chiamata alla funzione che carica i dati del libro dal database

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvCategory.setOnClickListener {
            categoryDialog()
        }

        binding.btnSubmit.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() { //Funzione che carica i dati del libro dal database
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId) //Impostazione della referenza del libro
            .addListenerForSingleValueEvent(object : ValueEventListener { //Creazione di un listener per la richiesta di dati
                override fun onDataChange(snapshot: DataSnapshot) { //  Funzione che viene chiamata quando i dati sono disponibili
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString() //Impostazione dell'id della categoria selezionata
                    val description = snapshot.child("description").value.toString() //Impostazione della descrizione del libro
                    val title = snapshot.child("title").value.toString() //Impostazione del titolo del libro

                    binding.edtTitle.setText(title) //Impostazione del titolo del libro nell'edit text
                    binding.edtDescription.setText(description) //Impostazione della descrizione del libro nell'edit text

                    val refBookCategory = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
                    refBookCategory.child(selectedCategoryId) //Impostazione della referenza della categoria selezionata
                        .addListenerForSingleValueEvent(object : ValueEventListener { //Creazione di un listener per la richiesta di dati
                            override fun onDataChange(snapshot: DataSnapshot) { //  Funzione che viene chiamata quando i dati sono disponibili
                                val category = snapshot.child("category").value //Impostazione della categoria selezionata
                                binding.tvCategory.text = category.toString() //Impostazione della categoria selezionata nella text view
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = "" //Stringa che contiene il titolo del libro
    private var description = "" //Stringa che contiene la descrizione del libro

    private fun validateData() { //Funzione che valida i dati inseriti dall'utente
        title = binding.edtTitle.text.toString().trim() //Impostazione del titolo del libro
        description = binding.edtDescription.text.toString().trim() //Impostazione della descrizione del libro
        //validate data
        if (title.isEmpty()) { //Se il titolo del libro è vuoto
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show() //Mostra un toast che indica che il titolo del libro è vuoto
        } else if (description.isEmpty()) { //Se la descrizione del libro è vuota
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show() //Mostra un toast che indica che la descrizione del libro è vuota
        } else if (selectedCategoryId.isEmpty()) { //Se la categoria selezionata è vuota
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show() //Mostra un toast che indica che la categoria non è stata selezionata
        } else {
            updatePdf() //Chiamata alla funzione che aggiorna il libro nel database
        }
    }

    private fun updatePdf() { //Funzione che aggiorna il libro nel database
        progressDialog.setMessage("Updating book info") //Impostazione del messaggio del dialog
        progressDialog.show() //Mostra il dialog


        val hashMap = HashMap<String, Any>() //Creazione di una mappa di stringhe e oggetti che contiene i dati da aggiornare
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Update succesfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = "" //Stringa che contiene l'id della categoria selezionata
    private var selectedCategoryTitle = "" //Stringa che contiene il titolo della categoria selezionata
    private fun categoryDialog() { //Funzione che mostra un dialog che permette di selezionare la categoria del libro
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size) //Creazione di un array di stringhe che contiene i titoli delle categorie
        for (i in categoryTitleArrayList.indices) { //Ciclo che scorre tutte le categorie
            categoriesArray[i] = categoryTitleArrayList[i] //
        }
        val builder = AlertDialog.Builder(this) //Creazione di un alert dialog che permette di selezionare la categoria del libro
        builder.setTitle("Choose Category") //Impostazione del titolo del dialog
            .setItems(categoriesArray) { dialog, position ->

                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.tvCategory.text = selectedCategoryTitle
            }.show()
    }

    private fun loadCategories() { //Funzione che carica le categorie dal database
        categoryTitleArrayList = ArrayList() //Creazione di un array list di stringhe che contiene i titoli delle categorie
        categoryIdArrayList = ArrayList() //Creazione di un array list di stringhe che contiene gli id delle categorie

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener { //Creazione di un listener per la richiesta di dati
            override fun onDataChange(snapshot: DataSnapshot) { //  Funzione che viene chiamata quando i dati sono disponibili
                categoryIdArrayList.clear() //Pulizia dell'array list che contiene gli id delle categorie
                categoryTitleArrayList.clear() //Pulizia dell'array list che contiene i titoli delle categorie

                for (ds in snapshot.children) { //Ciclo che scorre tutte le categorie
                    val id = "${ds.child("id").value}" //  Impostazione dell'id della categoria
                    val category = "${ds.child("categories").value}" //  Impostazione del titolo della categoria

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}