package it.uninsubria.appappunti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.appappunti.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding //lateinit var permette di inizializzare la variabile dopo che è stato creato l'oggetto

    private lateinit var firebaseAuth: FirebaseAuth //lateinit var permette di inizializzare la variabile dopo che è stato creato l'oggetto

    private lateinit var booksArrayList: ArrayList<ModelPdf> // lateinit var permette di inizializzare la variabile dopo che è stato creato l'oggetto

    private lateinit var adapterPdfFavorite: AdapterPdfFavorite //lateinit var permette di inizializzare la variabile dopo che è stato creato l'oggetto

    override fun onCreate(savedInstanceState: Bundle?) { //override permette di sovrascrivere il metodo onCreate
        binding = ActivityProfileBinding.inflate(layoutInflater) //inflate permette di infilare il layout in una view
        super.onCreate(savedInstanceState) //super permette di chiamare il metodo onCreate della superclasse
        setContentView(binding.root) //setContentView permette di impostare la view in una activity

        firebaseAuth = FirebaseAuth.getInstance() // inizializzo l'oggetto firebaseAuth
        loadUserInfo() // chiamo il metodo loadUserInfo
        loadFavoriteBooks() // chiamo il metodo loadFavoriteBooks

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }

    private fun loadUserInfo() { // metodo per caricare le informazioni dell'utente
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.tvName.text = name
                    binding.tvEmail.text = email
                    binding.tvMemberDate.text = formattedDate
                    binding.tvTypeAccount.text = userType

                    //set image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.ivProfile)
                    } catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadFavoriteBooks() { // metodo per caricare i libri preferiti dell'utente

        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for (ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"

                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //add model to list
                        booksArrayList.add(modelPdf)
                    }

                    binding.tvFavoriteBookCount.text = "${booksArrayList.size}"
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)
                    binding.rvFavorite.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}