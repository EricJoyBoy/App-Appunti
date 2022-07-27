package it.uninsubria.appappunti

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.appappunti.databinding.RowPdfUserBinding

class PDFUserAdapter  : RecyclerView.Adapter<PDFUserAdapter.HolderPdfUser>, Filterable { //implements Filterable {

    private var context: Context  //context dell'applicazione

    var pdfArrayList: ArrayList<ModelPdf>  //lista dei pdf

    var filterList: ArrayList<ModelPdf>  //lista dei pdf filtrati

    private var filter: FilterPdfUser? = null  //filtro per la ricerca

    private lateinit var binding: RowPdfUserBinding //binding per la row della lista dei pdf

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {  //costruttore
        this.context = context //context dell'applicazione
        this.pdfArrayList = pdfArrayList // lista dei pdf
        this.filterList = pdfArrayList //lista dei pdf filtrati
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser { //crea la row della lista dei pdf
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false) //infla la row della lista dei pdf
        return HolderPdfUser(binding.root) //ritorna la row della lista dei pdf
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) { //imposta i dati della row della lista dei pdf
        val model = pdfArrayList[position] //prende il pdf dalla lista dei pdf
        val bookId = model.id //prende l'id del pdf
        val categoryId = model.categoryId //prende l'id della categoria del pdf
        val title = model.title //prende il titolo del pdf
        val description = model.description //prende la descrizione del pdf
        val uid = model.uid //prende l'uid del pdf
        val url = model.url //prende l'url del pdf
        val timestamp = model.timestamp //prende il timestamp del pdf

        val date = MyApplication.formatTimeStamp(timestamp) //formatta il timestamp del pdf

        holder.tvTitle.text = title //imposta il titolo del pdf
        holder.tvDescription.text = description //imposta la descrizione del pdf
        holder.tvDate.text = date //imposta la data del pdf

        MyApplication.loadCategory(categoryId, holder.tvCategory) //carica la categoria del pdf

        MyApplication.loadPdfFromUrlSinglePage(
            url,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        MyApplication.loadPdfSize(url, title, holder.tvSize) //carica la dimensione del pdf
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPdfActivity::class.java) //crea l'intent per la activity dei dettagli del pdf
            intent.putExtra("bookId", bookId) //imposta l'id del pdf
            context.startActivity(intent) //avvia l'activity dei dettagli del pdf
        }
    }

    override fun getItemCount(): Int = pdfArrayList.size //ritorna la lunghezza della lista dei pdf

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) { //inner class
        var pdfView = binding.pdfView //pdfView
        var progressBar = binding.progressBar //progressBar
        var tvTitle = binding.tvTitle //tvTitle
        var tvDescription = binding.tvDescription //tvDescription
        var tvCategory = binding.tvCategory //tvCategory
        var tvSize = binding.tvSize //tvSize
        var tvDate = binding.tvDate //tvDate
    }

    override fun getFilter(): Filter { //ritorna il filtro
        if (filter == null) { //    if (filter == null) {
            filter = FilterPdfUser(filterList, this) //crea il filtro
        }
        return filter as FilterPdfUser //ritorna il filtro
    }

}