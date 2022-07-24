package it.uninsubria.appappunti

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import it.uninsubria.appappunti.databinding.DialogCommentAddBinding
import it.uninsubria.appappunti.databinding.ActivityDetailPdfBinding

import java.io.FileOutputStream

class DetailPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPdfBinding //lateinit perchè non è ancora inizializzato

    private var bookTitle = "" //titolo del libro
    private var bookUrl = "" //url del libro

    private var isInMyFavorite = false //se il libro è in mio preferito

    private var bookId = "" //id del libro

    private lateinit var progressDialog: ProgressDialog //dialog per la caricamento del libro
    private lateinit var firebaseAuth: FirebaseAuth //autenticazione firebase

    private lateinit var commentArrayList: ArrayList<ModelComment> //lista dei commenti

    private lateinit var adapterComment: AdapterComment //adapter dei commenti

    override fun onCreate(savedInstanceState: Bundle?) { //quando l'activity viene creata
        binding = ActivityDetailPdfBinding.inflate(layoutInflater) //infla la view
        super.onCreate(savedInstanceState)//richiamo il metodo della superclasse
        setContentView(binding.root)//setto la view come contenuto dell'activity
        bookId = intent.getStringExtra("bookId")!! //prendo l'id del libro dall'intent

        //init progressBar
        progressDialog = ProgressDialog(this) //creo il dialog
        progressDialog.setTitle("Attendere prego") //setto il titolo del dialog
        progressDialog.setCanceledOnTouchOutside(false) //non si può chiudere con il touch sull'area sopra al dialog

        firebaseAuth = FirebaseAuth.getInstance() //prendo l'autenticazione firebase
        if (firebaseAuth.currentUser != null) { //se l'utente è loggato
            checkIsFavorite() //controllo se il libro è in mio preferito
        }

        MyApplication.incrementBookViewCount(bookId) //incremento il contatore di visualizzazioni del libro

        loadBookDetail() //carico i dettagli del libro
        showComments() //mostro i commenti

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnReadBook.setOnClickListener {
            val intent = Intent(this, ViewPdfActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.btnDowloadBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE //controllo se ho il permesso di scrivere sull'SD
                ) == PackageManager.PERMISSION_GRANTED //se ho il permesso
            ) {
                Log.d("ndt", "permission granted") //log per controllo
                dowloadBook() //scarico il libro
            } else {
                Log.d("ndt", "permission denied") //log per controllo
                requestStorePermissonLaucher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) //richiedo il permesso
            }
        }

        binding.btnFavorite.setOnClickListener {

            if (firebaseAuth.currentUser == null) { //se non sono loggato
                Toast.makeText(this, "Non sei loggato", Toast.LENGTH_SHORT).show()  //mostro un toast
            } else {
                if (isInMyFavorite) { //se il libro è in mio preferito
                    MyApplication.removeFromFavorite(this, bookId) //rimuovo il libro dai miei preferiti
                } else { //se il libro non è in mio preferito
                    addToFavorite() //aggiungo il libro ai miei preferiti
                }
            }
        }
        binding.btnAddComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) { //se non sono loggato
                Toast.makeText(
                    this,
                    "Non sei loggato effettua il login per commentare",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                addCommentDialog()
            }
        }
    }


    private fun showComments() { //mostro i commenti
        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear() //svuoto la lista dei commenti
                    for (ds in snapshot.children) { //per ogni commento
                        val model = ds.getValue(ModelComment::class.java) //prendo i dati del commento
                        commentArrayList.add(model!!) //aggiungo il commento alla lista
                    }
                    adapterComment = AdapterComment(this@DetailPdfActivity, commentArrayList) //creo l'adapter dei commenti

                    binding.rvComment.adapter = adapterComment //setto l'adapter dei commenti
                }
                override fun onCancelled(error: DatabaseError) { //

                }
            })
    }

    private var comment = ""
    private fun addCommentDialog() { //dialog per aggiungere un commento
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this)) //inflo la view del dialog

        val builder = AlertDialog.Builder(this, R.style.CustomDialog) //creo il dialog
        builder.setView(commentAddBinding.root) //setto la view del dialog

        val alertDialog = builder.create() //creo il dialog
        alertDialog.show() //mostro il dialog

        commentAddBinding.btnBack.setOnClickListener {
            alertDialog.dismiss() //chiudo il dialog
        }
        commentAddBinding.btnSubmit.setOnClickListener {
            comment = commentAddBinding.edtComment.text.toString().trim() //prendo il commento
            if (comment.isEmpty()) { //se il commento è vuoto
                Toast.makeText(
                    this,
                    "Commenti non può essere vuoto",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Non ha aggiunto un commento")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //books > bookId > Comments > commentId > commentData
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp) //per ogni commento
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Hai aggiunto un commento", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Commento non aggiunto",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val requestStorePermissonLaucher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("ndt", "permission granted")
                dowloadBook()
            } else {
                Log.d("ndt", "permission denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun dowloadBook() { //scarico il libro
        progressDialog.setTitle("Scarica i tuoi appunti") //setto il titolo del dialog
        progressDialog.show() //mostro il dialog

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl) //prendo la reference del libro
        storageReference.getBytes(Constants.MAX_BYTES_PDF) //prendo i bytes del libro
            .addOnSuccessListener { bytes ->
                saveToDowloadsFolder(bytes)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Download non andato a buon fine ", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToDowloadsFolder(bytes: ByteArray?) { //salvo il libro nella cartella dei dowloads
        val nameWithExtension = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) //prendo la cartella dei dowloads
            downloadsFolder.mkdirs() //creo la cartella dei dowloads

            val filePath = downloadsFolder.path + "/" + nameWithExtension //prendo il percorso del file
            val out = FileOutputStream(filePath) //prendo il file output stream
            out.write(bytes) //scrivo i bytes nel file
            out.close() //chiudo il file output stream

            Toast.makeText(this, "Download" +
                    "", Toast.LENGTH_SHORT)
                .show()
            progressDialog.dismiss()
            incrementDownloadCount()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Errore generato da ${e.message
            }", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun incrementDownloadCount() { //incremento il contatore di download
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("dowloadsCount").value}"

                    if (downloadsCount == "" || downloadsCount == "null") { //se il contatore è vuoto
                        downloadsCount = "0" //lo setto a 0
                    }
                    val newDownloadsCount: Long = downloadsCount.toLong() + 1 //incremento il contatore

                    val hashMap: HashMap<String, Any> = HashMap() //creo il hashmap
                    hashMap["dowloadsCount"] = newDownloadsCount //setto il contatore

                    val dbRef = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
                    dbRef.child(bookId) //per ogni libro
                        .updateChildren(hashMap)//aggiorno il contatore
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener {

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDetail() { //carico i dettagli del libro
        //Books > bookId > Detail
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val dowloadsCount = "${snapshot.child("dowloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.tvCategory) //carico la categoria

                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.tvPages
                    )//carico il libro

                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.tvSize)

                    //set data
                    binding.tvTitle.text = bookTitle
                    binding.tvDescription.text = description
                    binding.tvView.text = viewsCount
                    binding.tvDowload.text = dowloadsCount
                    binding.tvDate.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite() { //controllo se il libro è già stato aggiunto ai preferiti
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener { //listener per i dati del libro
                override fun onDataChange(snapshot: DataSnapshot) { //se il libro è già stato aggiunto ai preferiti
                    isInMyFavorite = snapshot.exists() //setto la variabile a true
                    if (isInMyFavorite) { //se il libro è già stato aggiunto ai preferiti
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, //left
                            R.drawable.ic_favorite, //right
                            0, //top
                            0 //bottom
                        )
                        binding.btnFavorite.text = "Rimuovi dai preferiti"
                    } else {
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border,
                            0,
                            0
                        )
                        binding.btnFavorite.text = "Aggiungi ai Preferiti"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() { //aggiungo il libro ai preferiti
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>() //creo il hashmap
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Appunto aggiunto ai preferiti", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Appunto rimosso dai preferiti", Toast.LENGTH_SHORT).show()
            }
    }
}