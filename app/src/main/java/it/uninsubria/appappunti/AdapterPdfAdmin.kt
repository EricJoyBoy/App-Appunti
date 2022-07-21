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

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable  {

    private var context: Context

    var pdfArrayList: ArrayList<ModelPdf>

    private var filterList: ArrayList<ModelPdf>

    private var filter: FilterPdfAdmin? = null

    private lateinit var binding: RowPdfAdminBinding

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = ArrayList()
        this.filterList.addAll(pdfArrayList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }


    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
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
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = formattedDate

        //load category
        MyApplication.loadCategory(categoryId, holder.tvCategory)

        //không cần số trang ở đây, đặt null cho số trang
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

    private fun moreOptionDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        //get id, url, title cua sach
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //show dialog
        val options = arrayOf("Modifica", "Cancella")

        //thong bao
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Scegli un'opzione")
            .setItems(options) { dialog, position ->

                if (position == 0) {
                    val intent = Intent(context, EditPdfActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                } else if (position == 1) {


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


    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
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