package it.uninsubria.appappunti


import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import it.uninsubria.appappunti.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding // lateinit: variabile non inizializzata, non è possibile usarla prima di averla inizializzata

    private lateinit var firebaseAuth: FirebaseAuth // lateinit: variabile non inizializzata, non è possibile usarla prima di averla inizializzata

    private var imageUri: Uri? = null // variabile di tipo Uri? (nullable)

    private lateinit var progressDialog: ProgressDialog // lateinit: variabile non inizializzata, non è possibile usarla prima di averla inizializzata

    override fun onCreate(savedInstanceState: Bundle?) { // override: sovrascrive un metodo di una superclasse
        binding = ActivityProfileEditBinding.inflate(layoutInflater) // inflate: crea una istanza di una classe dato un layout XML
        super.onCreate(savedInstanceState) // super: chiamata a un metodo di una superclasse
        setContentView(binding.root) // setContentView: imposta il layout dell'activity

        progressDialog = ProgressDialog(this) // crea una istanza di una classe ProgressDialog
        progressDialog.setTitle("Attendere prego...") // imposta il titolo della ProgressDialog
        progressDialog.setCanceledOnTouchOutside(false) // imposta che la ProgressDialog non si chiude quando si clicca fuori dalla finestra

        firebaseAuth = FirebaseAuth.getInstance() // crea una istanza di FirebaseAuth

        loadUserInfo() // chiamata a funzione loadUserInfo

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivProfile.setOnClickListener {
            showImageAttachMenu()
        }

        binding.btnUpdate.setOnClickListener {
            validateData()
        }
    }

    private var name = "" // variabile di tipo String
    private fun validateData() { // funzione validateData
        name = binding.edtName.text.toString().trim() // assegna alla variabile name il valore del campo di testo edtName

        if (name.isEmpty()) { // se il campo di testo edtName è vuoto
            Toast.makeText(this, "Inserisci Nome", Toast.LENGTH_SHORT).show() // mostra un messaggio di Toast
        } else { // altrimenti
            if (imageUri == null) { // se l'immagine non è stata selezionata
                updateProfile("") // chiamata a funzione updateProfile
            } else {
                uploadImage() // chiamata a funzione uploadImage
            }
        }

    }

    private fun uploadImage() { // funzione uploadImage
        progressDialog.setMessage("Caricamento in corso...")
        progressDialog.show()

        val filePathAndName = "ProfileImages/" + firebaseAuth.uid // variabile di tipo String

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName) //  crea una istanza di FirebaseStorage e ne crea una referenza per il filePathAndName
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Caricamento della foto non riuscito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadImageUrl: String) { // funzione updateProfile
        progressDialog.setMessage("Aggiornamento informazioni in corso...") // imposta il messaggio della ProgressDialog
        val hashMap: HashMap<String, Any> = HashMap() // crea una istanza di HashMap
        hashMap["name"] = "$name" // aggiunge all'HashMap il valore della variabile name
        if (imageUri != null) { // se l'immagine è stata selezionata
            hashMap["profileImage"] = uploadImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Informazioni personali aggiornate correttamente", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Impossibile aggiornare le informazioni", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserInfo() { // funzione loadUserInfo
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    binding.edtName.setText(name)

                    try {
                        Glide.with(this@ProfileEditActivity) // crea una istanza di Glide e lo passa alla funzione with
                            .load(profileImage) // carica l'immagine dal percorso specificato
                            .placeholder(R.drawable.ic_person_gray) // imposta un placeholder per l'immagine
                            .into(binding.ivProfile) // imposta l'immagine come riferimento per l'ImageView ivProfile
                    } catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu() {

        val popupMenu = PopupMenu(this, binding.ivProfile) // crea una istanza di PopupMenu e lo passa alla funzione con il riferimento all'ImageView ivProfile
        popupMenu.menu.add(Menu.NONE, 0, 0, "Fai una foto") // aggiunge un elemento al menu di PopupMenu
        popupMenu.menu.add(Menu.NONE, 1, 1, "Galleria") // aggiunge un elemento al menu di PopupMenu
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId //
            if (id == 0) { // se l'id dell'elemento è 0
                pickImageCamera() // chiamata a funzione pickImageCamera
            } else if (id == 1) { // altrimenti se l'id dell'elemento è 1
                pickImageGallery() // chiamata a funzione pickImageGallery
            }
            true
        }
    }

    private fun pickImageGallery() { // funzione pickImageGallery
        val intent = Intent(Intent.ACTION_PICK) // crea una istanza di Intent e lo passa alla funzione con l'azione ACTION_PICK
        intent.type = "image/*" // imposta il tipo di file da cercare

        galleryActivityResultLauncher.launch(intent) // chiamata a funzione launch
    }

    private fun pickImageCamera() { // funzione pickImageCamera
        val values = ContentValues() // crea una istanza di ContentValues
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title") // aggiunge all'ContentValues il titolo Temp_Title
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description") // aggiunge all'ContentValues la descrizione Temp_Description

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)//   crea una istanza di Uri e lo passa alla funzione con il riferimento ai valori di ContentValues

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // crea una istanza di Intent e lo passa alla funzione con l'azione ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri) // aggiunge all'Intent l'Uri dell'immagine
        cameraActivityResultLauncher.launch(intent) // chiamata a funzione launch

    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    //imageUri = data!!.data

                    //set imageview
                    binding.ivProfile.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, "Cancellato", Toast.LENGTH_SHORT).show()
                }
            })
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                //set imageview
                binding.ivProfile.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Annulato", Toast.LENGTH_SHORT).show()
            }
        })

}