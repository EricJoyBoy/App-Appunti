package it.uninsubria.appappunti


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ItemPdfFavoriteBinding

/**
 * Adapter per la lista dei PDF preferiti
 */
class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{
    private val context: Context //context dell'applicazione

    private var bookArrayList: ArrayList<ModelPdf> //lista dei PDF preferiti

    private lateinit var binding: ItemPdfFavoriteBinding //lateinit perchè non è inizializzata ancora

    constructor(context: Context, bookArrayList: ArrayList<ModelPdf>) { // costruttore
        this.context = context
        this.bookArrayList = bookArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = ItemPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) { // metodo per settare i dati dei PDF preferiti
        val model = bookArrayList[position] // oggetto ModelPdf
        loadBookDetail(model, holder) // metodo per caricare i dettagli del PDF preferito

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPdfActivity::class.java)
            intent.putExtra("bookId", model.id) //pass book id not category id
            context.startActivity(intent)
        }

        holder.btnRemoveFavorite.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadBookDetail(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("dowloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewCount = viewsCount.toLong()
                    model.dowloadsCount = downloadsCount.toLong()

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.tvCategory)

                    MyApplication.loadPdfFromUrlSinglePage(
                        "$url",
                        "$title",
                        binding.pdfView,
                        binding.progressBar,
                        null
                    )

                    //load pdf size
                    MyApplication.loadPdfSize("$url", "$title", binding.tvSize) //Carica la dimensione del pdf

                    //set data
                    binding.tvTitle.text = title
                    binding.tvDescription.text = description
                    binding.tvDate.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int = bookArrayList.size //ritorna la lunghezza della lista

    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) { //holder per i PDF preferiti
        var viewPdf = binding.pdfView  //view del pdf
        var processBar = binding.progressBar // progress bar
        val tvTitle = binding.tvTitle //titolo del pdf
        val tvDescription = binding.tvDescription //descrizione del pdf
        val tvCategory = binding.tvCategory //categoria del pdf
        val tvSize = binding.tvSize //dimensione del pdf
        val tvDate = binding.tvDate //data di pubblicazione del pdf
        val btnRemoveFavorite = binding.btnRemoveFavorite //bottone di rimozione dai preferiti
    }

}