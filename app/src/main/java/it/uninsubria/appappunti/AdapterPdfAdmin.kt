package it.uninsubria.appappunti

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.appappunti.databinding.RowPdfAdminBinding

/**
 * Adapter per la lista dei PDF dell'admin
 */
class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable  {

    private var context: Context

    var pdfArrayList: ArrayList<ModelPdf> //lista di pdf

    private var filterList: ArrayList<ModelPdf> //lista di pdf filtrata

    private var filter: FilterPdfAdmin? = null //filtro per la ricerca

    private lateinit var binding: RowPdfAdminBinding //binding per la row del pdf

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) { //costruttore
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = ArrayList()
        this.filterList.addAll(pdfArrayList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin { //crea la row del pdf
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false) //infla la row del pdf
        return HolderPdfAdmin(binding.root) //ritorna la row del pdf
    }


    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) { //imposta i dati della row del pdf
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp sang dd/mm/yyyy
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.tvTitle.text = title //set titolo
        holder.tvDescription.text = description //set descrizione
        holder.tvDate.text = formattedDate //set data

        MyApplication.loadCategory(categoryId, holder.tvCategory) //carica categoria

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.tvSize)

        //show edit, delete
        holder.btnMore.setOnClickListener {
            moreOptionDialog(model, holder)
        }
        //click item mo man hinh detail book
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPdfActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) { //dialog per le opzioni del pdf
        //get id, url, title cua sach
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //show dialog
        val options = arrayOf("Modifica", "Cancella") //opzioni del dialog


        val builder = AlertDialog.Builder(context) //crea il dialog
        builder.setTitle("Scegli un'opzione")
            .setItems(options) { dialog, position ->

                if (position == 0) { //modifica
                    val intent = Intent(context, EditPdfActivity::class.java) //intent per la modifica
                    intent.putExtra("bookId", bookId) //set id
                    context.startActivity(intent) //    avvia la modifica
                } else if (position == 1) { //cancella


                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }

            }.show()
    }

    override fun getItemCount(): Int = pdfArrayList.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }


    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) { //holder per la row del pdf
        val pdfView = binding.pdfView
        val progressBar = binding.pb
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvCategory = binding.tvCategory
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val btnMore = binding.btnMore
    }

}