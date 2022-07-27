package it.uninsubria.appappunti

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import it.uninsubria.appappunti.databinding.ActivityPdfAddBinding



class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding  // lateinit: variabile non inizializzata, non è possibile usare il binding prima di averlo inizializzato

    private lateinit var firebaseAuth: FirebaseAuth // lateinit: variabile non inizializzata, non è possibile usare il firebaseAuth prima di averlo inizializzato

    private lateinit var progressDialog: ProgressDialog // lateinit: variabile non inizializzata, non è possibile usare il progressDialog prima di averlo inizializzato

    private lateinit var categoryArrayList: ArrayList<ModelCategory> // lateinit: variabile non inizializzata, non è possibile usare la categoryArrayList prima di averlo inizializzato

    private var pdfUri: Uri? = null // var: variabile di tipo nullable, può essere null o non null

    private val TAG = "PDF_ADD_TAG" // val: variabile di tipo costante, non può essere modificata


    override fun onCreate(savedInstanceState: Bundle?) {    // override: sovrascrive un metodo di una superclasse
        super.onCreate(savedInstanceState) // super: chiamo il metodo della superclasse
        binding = ActivityPdfAddBinding.inflate(layoutInflater) // binding: istanzia il binding con il layout dell'activity
        setContentView(binding.root) // setContentView: setta il layout dell'activity



        firebaseAuth = FirebaseAuth.getInstance() // firebaseAuth: istanzia il firebaseAuth con FirebaseAuth.getInstance()
        loadPDFCategories() // loadPDFCategories: carica le categorie di PDF dal database


        progressDialog = ProgressDialog(this) // progressDialog: istanzia il progressDialog con il costruttore della classe ProgressDialog
        progressDialog.setTitle("Caricamento") // progressDialog: setta il titolo del progressDialog con il metodo setTitle
        progressDialog.setCanceledOnTouchOutside(false) // progressDialog: setta il valore del metodo setCanceledOnTouchOutside con false




        binding.categoryTv.setOnClickListener { // binding.categoryTv: setta il listener del binding.categoryTv con una funzione lambda
            categoryPickDialog()
        }

        binding.imageBtnAttachPDF.setOnClickListener { // binding.imageBtnAttachPDF: setta il listener del binding.imageBtnAttachPDF con una funzione lambda
            pdfPickIntent()
        }

        binding.submitBtnAddCategory.setOnClickListener { //    binding.submitBtnAddCategory: setta il listener del binding.submitBtnAddCategory con una funzione lambda
            validateData()
        }

        binding.backBtnAddCategory // binding.backBtnAddCategory: setta il listener del binding.backBtnAddCategory con una funzione lambda
            .setOnClickListener {
            onBackPressed()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() { // funzione validateData: valida i dati inseriti dall'utente

        title = binding.categoryEtAddName.text.toString().trim() // title: setta il valore della variabile title con il metodo trim della variabile categoryEtAddName
        description = binding.categoryEtAddDescription.text.toString().trim() // description: setta il valore della variabile description con il metodo trim della variabile categoryEtAddDescription
        category = binding.categoryTv.text.toString().trim() // category: setta il valore della variabile category con il metodo trim della variabile categoryTv

        if (title.isEmpty()) {
            Toast.makeText(this, "Inserisci il titolo", Toast.LENGTH_SHORT).show() // Toast: mostra un messaggio di toast con il metodo makeText con il metodo show
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Inserisci la Descrizione", Toast.LENGTH_SHORT).show() //  Toast: mostra un messaggio di toast con il metodo makeText con il metodo show
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Seleziona una Categoria", Toast.LENGTH_SHORT).show() //  Toast: mostra un messaggio di toast con il metodo makeText con il metodo show
        } else if (pdfUri == null) {
            Toast.makeText(this, "Inserisci un PDF", Toast.LENGTH_SHORT).show() //  Toast: mostra un messaggio di toast con il metodo makeText con il metodo show
        } else {
            uploadPdfToStorage()
        }

    }  private fun uploadPdfToStorage() {
        progressDialog.setMessage("Caricamento in corso...") // progressDialog: setta il messaggio del progressDialog con il metodo setMessage
        progressDialog.show() // progressDialog: mostra il progressDialog con il metodo show


        val timestamp = System.currentTimeMillis() // timestamp: setta il valore della variabile timestamp con il metodo System.currentTimeMillis()


        val filePathAndName = "Books/$timestamp" // filePathAndName: setta il valore della variabile filePathAndName con il metodo concatenazione di stringhe

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName) // storageReference: istanzia il storageReference con FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"
                uploadedPdfInfoToDb(uploadedPdfUrl, timestamp)
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Caricamento Fallito ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadedPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        progressDialog.setMessage("Caricamento Pdf in corso ..")

        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap() // hashMap: setta il valore della variabile hashMap con il metodo HashMap()
        hashMap["uid"] = "$uid" // hashMap["uid"]: setta il valore della variabile hashMap con il metodo put con il metodo uid
        hashMap["id"] = "$timestamp" // hashMap["id"]: setta il valore della variabile hashMap con il metodo put con il metodo id
        hashMap["title"] = "$title" // hashMap["title"]: setta il valore della variabile hashMap con il metodo put con il metodo title
        hashMap["description"] = "$description" // hashMap["description"]: setta il valore della variabile hashMap con il metodo put con il metodo description
        hashMap["categoryId"] = "$selectedCategoryId" // hashMap["categoryId"]: setta il valore della variabile hashMap con il metodo put con il metodo categoryId
        hashMap["url"] = "$uploadedPdfUrl" // hashMap["url"]: setta il valore della variabile hashMap con il metodo put con il metodo url
        hashMap["timestamp"] = timestamp // hashMap["timestamp"]: setta il valore della variabile hashMap con il metodo put con il metodo timestamp
        hashMap["viewsCount"] = 0 // hashMap["viewsCount"]: setta il valore della variabile hashMap con il metodo put con il metodo viewsCount
        hashMap["dowloadsCount"] = 0 // hashMap["dowloadsCount"]: setta il valore della variabile hashMap con il metodo put con il metodo dowloadsCount

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {

            progressDialog.dismiss()
            Toast.makeText(this, "Caricamento andato a buon fine", Toast.LENGTH_SHORT).show()
            pdfUri = null
        }.addOnFailureListener {

            progressDialog.dismiss()
            Toast.makeText(this, "Caricamento Fallito", Toast.LENGTH_SHORT).show() // Toast: mostra un messaggio di toast con il metodo makeText con il metodo show
        }
    }


    private fun loadPDFCategories() { // funzione loadPDFCategories: carica le categorie dal database
        categoryArrayList = ArrayList() // categoryArrayList: setta il valore della variabile categoryArrayList con il metodo ArrayList()

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories") // ref: istanzia il ref con FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener { // ref: setta il listener del ref con una funzione lambda
            override fun onDataChange(snapshot: DataSnapshot) { //  snapshot: setta il valore della variabile snapshot con il metodo DataSnapshot
                categoryArrayList.clear() // categoryArrayList: setta il valore della variabile categoryArrayList con il metodo clear()
                for (ds in snapshot.children) { // ds: setta il valore della variabile ds con il metodo children
                    val model = ds.getValue(ModelCategory::class.java) // model: setta il valore della variabile model con il metodo getValue con il metodo ModelCategory
                    categoryArrayList.add(model!!) // categoryArrayList: setta il valore della variabile categoryArrayList con il metodo add con il metodo model
                    //Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) { // error: setta il valore della variabile error con il metodo DatabaseError

            }
        })
    }

        private var selectedCategoryId = ""
        private var selectedCategoryTitle = ""


        private fun categoryPickDialog() { // funzione categoryPickDialog: apre una finestra di dialogo per selezionare la categoria
            val categoriesArray = arrayOfNulls<String>(categoryArrayList.size) // categoriesArray: setta il valore della variabile categoriesArray con il metodo arrayOfNulls con il metodo size
            for (i in categoryArrayList.indices) { // i: setta il valore della variabile i con il metodo indices
                categoriesArray[i] = categoryArrayList[i].category // categoriesArray: setta il valore della variabile categoriesArray con il metodo add con il metodo category

            }

            val builder = AlertDialog.Builder(this) // builder: setta il valore della variabile builder con il metodo AlertDialog.Builder con il metodo this
            builder.setTitle("Scegli una Categoria").setItems(categoriesArray) { dialog, which -> // builder: setta il valore della variabile builder con il metodo setTitle con il metodo setItems con il metodo which
                selectedCategoryTitle = categoryArrayList[which].category // selectedCategoryTitle: setta il valore della variabile selectedCategoryTitle con il metodo categoryArrayList con il metodo which

                selectedCategoryId = categoryArrayList[which].id // selectedCategoryId: setta il valore della variabile selectedCategoryId con il metodo categoryArrayList con il metodo which

                binding.categoryTv.text = selectedCategoryTitle // binding: setta il valore della variabile binding con il metodo categoryTv con il metodo text con il metodo selectedCategoryTitle
            }.show()
        }


    private fun pdfPickIntent() { // funzione pdfPickIntent: apre una finestra di dialogo per selezionare il pdf

        val intent = Intent() // intent: setta il valore della variabile intent con il metodo Intent()
        intent.type = "application/pdf" // intent: setta il valore della variabile intent con il metodo type con il metodo application/pdf
        intent.action = Intent.ACTION_GET_CONTENT // intent: setta il valore della variabile intent con il metodo action con il metodo ACTION_GET_CONTENT
        pdfActivityResultLaucher.launch(intent) // pdfActivityResultLaucher: setta il valore della variabile pdfActivityResultLaucher con il metodo launch con il metodo intent
    }
    val pdfActivityResultLaucher = registerForActivityResult( // pdfActivityResultLaucher: setta il valore della variabile pdfActivityResultLaucher con il metodo registerForActivityResult con il metodo launch con il metodo intent
        ActivityResultContracts.StartActivityForResult(), // ActivityResultContracts: setta il valore della variabile ActivityResultContracts con il metodo StartActivityForResult()
        ActivityResultCallback<ActivityResult> { result -> // ActivityResult: setta il valore della variabile ActivityResult con il metodo ActivityResult con il metodo result
            if (result.resultCode == RESULT_OK) { // result: setta il valore della variabile result con il metodo resultCode con il metodo RESULT_OK
                pdfUri = result.data!!.data
            } else {
                Toast.makeText(this, "Cancellato", Toast.LENGTH_SHORT).show()
            }
        }
    )
    }


