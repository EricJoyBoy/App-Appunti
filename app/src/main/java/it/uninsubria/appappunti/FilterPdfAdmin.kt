package it.uninsubria.appappunti


import android.widget.Filter


class FilterPdfAdmin : Filter {


    var filterList: ArrayList<ModelPdf> //Lista di tutti i pdf

    var adapterPdfAdmin: AdapterPdfAdmin //Adapter per la lista dei pdf

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) { //Costruttore
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }



    override fun performFiltering(constraint: CharSequence?): FilterResults { // Metodo per il filtro
        var constraint: CharSequence? = constraint //Variabile per la stringa di filtro
        val results = FilterResults() //Variabile per i risultati del filtro


        if (constraint != null && constraint.isNotEmpty()) { //Se la stringa di filtro non è vuota

            constraint = constraint.toString().lowercase() //Converte la stringa in minuscolo
            val filterModel = ArrayList<ModelPdf>() //Lista per i risultati del filtro
            for (i in filterList.indices) { //Ciclo per i pdf
                if (filterList[i].title.lowercase().contains(constraint)) { //Se il titolo del pdf contiene la stringa di filtro

                    filterModel.add(filterList[i]) //Aggiunge il pdf alla lista dei risultati del filtro
                }
            }
            results.count = filterModel.size //Numero di risultati del filtro
            results.values = filterModel //Lista dei risultati del filtro
        } else {
            results.count = filterList.size //Numero di risultati del filtro
            results.values = filterList //Lista dei risultati del filtro
        }
        return results //Ritorna i risultati del filtro
    }


    override fun publishResults(constraint: CharSequence?, results: FilterResults?) { //Metodo per i risultati del filtro
        adapterPdfAdmin.pdfArrayList = results!!.values as ArrayList<ModelPdf> //Lista dei risultati del filtro

        adapterPdfAdmin.notifyDataSetChanged() //Aggiorna la lista dei pdf
    }
}

