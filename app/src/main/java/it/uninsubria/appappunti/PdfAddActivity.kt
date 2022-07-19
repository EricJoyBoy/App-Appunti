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

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null

    private val TAG = "PDF_ADD_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)



        firebaseAuth = FirebaseAuth.getInstance()
        loadPDFCategories()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Caricamento")
        progressDialog.setCanceledOnTouchOutside(false)




        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.imageBtnAttachPDF.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtnAddCategory.setOnClickListener {
            validateData()
        }

        binding.backBtnAddCategory
            .setOnClickListener {
            onBackPressed()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {

        title = binding.categoryEtAddName.text.toString().trim()
        description = binding.categoryEtAddDescription.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Inserisci il titolo", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Inserisci la Descrizione", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Seleziona una Categoria", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Inserisci un PDF", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }

    }  private fun uploadPdfToStorage() {
        //Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
        progressDialog.setMessage("Caricamento in corso...")
        progressDialog.show()

        //tmestamp
        val timestamp = System.currentTimeMillis()


        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
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
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["dowloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            // Log.d(TAG, "uploadedPdfInfoToDb: upload to db")
            progressDialog.dismiss()
            Toast.makeText(this, "Caricamento andato a buon fine", Toast.LENGTH_SHORT).show()
            pdfUri = null
        }.addOnFailureListener {
            //Log.d(TAG, "uploadedPdfInfoToDb: thất bại")
            progressDialog.dismiss()
            Toast.makeText(this, "Caricamento Fallito", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadPDFCategories() {
        //Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    //add vào arraylist
                    categoryArrayList.add(model!!)
                    //Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

        private var selectedCategoryId = ""
        private var selectedCategoryTitle = ""


        private fun categoryPickDialog() {
            val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
            for (i in categoryArrayList.indices) {
                categoriesArray[i] = categoryArrayList[i].category

            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Scegli una Categoria").setItems(categoriesArray) { dialog, which ->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle
            }.show()
        }


    private fun pdfPickIntent() {

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLaucher.launch(intent)
    }
    val pdfActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                //Log.d(TAG, "PDF Picked ")
                pdfUri = result.data!!.data
            } else {
                //Log.d(TAG, "PDF Picked Canceled ")
                Toast.makeText(this, "Cancellato", Toast.LENGTH_SHORT).show()
            }
        }
    )
    }


