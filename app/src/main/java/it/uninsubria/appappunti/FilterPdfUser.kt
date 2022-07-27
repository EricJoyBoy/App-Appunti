package it.uninsubria.appappunti

import android.widget.Filter

class FilterPdfUser : Filter{ //classe che implementa il filtro per la ricerca dei pdf dell'utente

    var filterList: ArrayList<ModelPdf> //lista dei pdf dell'utente

    var adapterPdfUser: PDFUserAdapter //adapter della lista dei pdf dell'utente

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: PDFUserAdapter) : super() { //costruttore della classe
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults { //metodo che esegue il filtro
        var constraint: CharSequence? = constraint //variabile che contiene il testo inserito nella searchview
        val results = FilterResults() //variabile che contiene i risultati del filtro

        if (constraint != null && constraint.isNotEmpty()) { //se il testo inserito è diverso da null e non è vuoto
            constraint = constraint.toString().lowercase() //converte il testo inserito in minuscolo
            val filterModel = ArrayList<ModelPdf>() //lista che contiene i risultati del filtro
            for (i in filterList.indices) { //ciclo for che cicla tutti i pdf dell'utente
                if (filterList[i].title.lowercase().contains(constraint)) { //se il titolo del pdf contiene il testo inserito
                    filterModel.add(filterList[i]) //aggiunge il pdf alla lista dei risultati del filtro
                }
            }
            results.count = filterModel.size //numero di risultati del filtro
            results.values = filterModel //lista dei risultati del filtro
        } else {
            results.count = filterList.size //numero di risultati del filtro
            results.values = filterList //lista dei risultati del filtro
        }
        return results //ritorna i risultati del filtro
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) { //metodo che pubblica i risultati del filtro
        //lọc
        adapterPdfUser.pdfArrayList = results!!.values as ArrayList<ModelPdf> //lista dei risultati del filtro

        adapterPdfUser. notifyDataSetChanged() //aggiorna la lista dei pdf dell'utente
    }
}