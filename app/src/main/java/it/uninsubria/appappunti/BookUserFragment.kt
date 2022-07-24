package it.uninsubria.appappunti


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.FragmentBooksUserBinding

/**
 * Una fragment che mostra i libri che un utente ha in possesso.
 */
class BookUserFragment : Fragment{


    private lateinit var binding: FragmentBooksUserBinding //lateinit perchè non è ancora inizializzato

    companion object { // companion object perchè è una classe statica
        fun newInstance(categoryId: String, category: String, uid: String): BookUserFragment { // funzione statica per creare una nuova istanza di BookUserFragment
            val fragment = BookUserFragment() // creo una nuova istanza di BookUserFragment
            val args = Bundle() // creo un nuovo Bundle
            args.putString("categoryId", categoryId) // aggiungo all'argomento il categoryId
            args.putString("category", category) // aggiungo all'argomento il category
            args.putString("uid", uid) // aggiungo all'argomento il uid
            fragment.arguments = args // imposto l'argomento alla nuova istanza di BookUserFragment
            return fragment // restituisco la nuova istanza di BookUserFragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf> //lateinit perchè non è ancora inizializzato
    private lateinit var adapterPdfUser: PDFUserAdapter // lateinit perchè non è ancora inizializzato

    constructor() // costruttore vuoto

    override fun onCreate(savedInstanceState: Bundle?) { // override perchè è un metodo della superclasse Fragment
        super.onCreate(savedInstanceState) // chiamo il metodo della superclasse Fragment
        val args = arguments // creo una variabile args che è una istanza di Bundle
        if (args != null) { // se args è diverso da null
            categoryId = args.getString("categoryId")!! // prendo il categoryId dall'argomento
            category = args.getString("category")!! // prendo il category dall'argomento
            uid = args.getString("uid")!!// prendo il uid dall'argomento
        }
    }

    override fun onCreateView( // override perchè è un metodo della superclasse Fragment
        inflater: LayoutInflater, // parametro inflater perchè è un metodo della superclasse Fragment
        container: ViewGroup?, // parametro container perchè è un metodo della superclasse Fragment
        savedInstanceState: Bundle? // parametro savedInstanceState perchè è un metodo della superclasse Fragment
    ): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false) // creo una istanza di FragmentBooksUserBinding e la inserisco nel container

        // tải pdf theo danh mục, newInstance load từng loại pdf
        if (category == "Tutti") { // se la categoria è Tutti
            loadAllBook() // carico tutti i libri
        } else if (category == "Piu Visti") { // se la categoria è Piu Visti
            loadMostDownloadBook("viewsCount") // carico i libri più visti
        } else if (category == "Piu Scaricati") { // se la categoria è Piu Scaricati
            loadMostDownloadBook("dowloadsCount") // carico i libri più scaricati
        } else {
            loadCategoryBook() // carico i libri della categoria
        }
        //search
        binding.edtSearch.addTextChangedListener {
            object : TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        adapterPdfUser.filter.filter(s)
                    } catch (e: Exception) {

                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            }
        }

        return binding.root
    }

    private fun loadAllBook() { // funzione per caricare tutti i libri
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener { // aggiungo un listener per il database
            override fun onDataChange(snapshot: DataSnapshot) { // override perchè è un metodo della superclasse ValueEventListener
                pdfArrayList.clear() // svuoto l'arrayList
                for (ds in snapshot.children) { // ciclo sui figli del database
                    val model = ds.getValue(ModelPdf::class.java) // creo una variabile model che è una istanza di ModelPdf e la carico con i dati del figlio del database

                    //add to list
                    pdfArrayList.add(model!!) // aggiungo all'arrayList il model
                }
                adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList) // creo una variabile adapterPdfUser che è una istanza di PDFUserAdapter e la carico con l'arrayList
                binding.rvBooks.adapter = adapterPdfUser// imposto l'adapter alla recyclerView
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadMostDownloadBook(orderBy: String) { // funzione per caricare i libri più scaricati
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) // ordino i libri per viewsCount o dowloadsCount e li limito a 10
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)

                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList)
                    binding.rvBooks.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadCategoryBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)

                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList)
                    binding.rvBooks.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}