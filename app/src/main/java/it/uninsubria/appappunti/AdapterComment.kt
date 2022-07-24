package it.uninsubria.appappunti

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.RowCommentBinding

/**
 * Adapter per la lista dei commenti
 */
class AdapterComment :  RecyclerView.Adapter<AdapterComment.HolderComment>{

    val context: Context

    val commentArrayList: ArrayList<ModelComment> //lista di commenti

    private lateinit var binding: RowCommentBinding //binding per la row commento

    private lateinit var firebaseAuth: FirebaseAuth //autenticazione firebase

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) { //costruttore
        this.context = context //
        this.commentArrayList = commentArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderComment(itemView: View) : RecyclerView.ViewHolder(itemView) { //holder per la row commento
        val ivProfile = binding.ivProfile
        val tvName = binding.tvName
        val tvDate = binding.tvDate
        val tvComment = binding.tvComment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment { //crea la row commento
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) { //imposta i dati della row commento
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp.toLong()) //formatta la data

        holder.tvDate.text = date
        holder.tvComment.text = comment

        loadUserDetails(model, holder) //carica i dati dell'utente

        holder.itemView.setOnClickListener { //quando viene cliccata la row commento
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) { //dialog per eliminare il commento
        val builder = AlertDialog.Builder(context) //crea il dialog
        builder.setTitle("Cancella Commento").setMessage("Sei sicuro di voler cancellare il commento ?") //imposta il titolo e il messaggio
            .setPositiveButton("Cancella") { d, e ->

                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue() //elimina il commento dal database
                    .addOnSuccessListener {
                        Toast.makeText(context, "Commento Caricato con Sucesso", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Caricamento Fallito", Toast.LENGTH_SHORT).show()
                    }

            }.setNegativeButton("Annulla") { d, e ->
                d.dismiss()
            }.show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) { //carica i dati dell'utente
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener { //listener per il dato dell'utente
                override fun onDataChange(snapshot: DataSnapshot) { //quando viene caricato il dato dell'utente
                    val name = "${snapshot.child("name").value}" //nome dell'utente
                    val profileImage = "${snapshot.child("profileImage").value}" //immagine dell'utente

                    holder.tvName.text = name
                    try {
                        Glide.with(context).load(profileImage).placeholder(R.drawable.ic_person_gray) //carica l'immagine dell'utente
                            .into(holder.ivProfile) //imposta l'immagine dell'utente
                    } catch (e: Exception) {
                        holder.ivProfile.setImageResource(R.drawable.ic_person_gray) //imposta l'immagine di default
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int = commentArrayList.size //ritorna la lunghezza della lista di commenti

}
