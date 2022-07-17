package it.uninsubria.appappunti

import android.widget.Filter


class FilterCategory : Filter { // Filter per la ricerca dei contenuti in base alla categoria

    private var filterList: ArrayList<ModelCategory> //Lista di elementi da filtrare
    private var adapterCategory: AdapterCategory //Adapter della lista da filtrare

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) {
        this.filterList = filterList //Lista da filtrare

        this.adapterCategory = adapterCategory //Adapter della lista da filtrare
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults { //Metodo che esegue il filtro
        var constraint = constraint //Stringa da filtrare
        val results = Filter.FilterResults() //Risultati del filtro

        if (constraint != null && constraint.isNotEmpty()) { //Se la stringa da filtrare non è vuota
            constraint = constraint.toString().toLowerCase() //La stringa da filtrare viene convertita in minuscolo
            val filteredModels: ArrayList<ModelCategory> = ArrayList() //Lista di elementi filtrati
            for (index in 0 until filterList.size) { //Per ogni elemento della lista da filtrare
                if(filterList[index].category.toLowerCase().contains(constraint)) { //Se la stringa da filtrare è contenuta nella categoria
                    filteredModels.add(filterList[index]) //Aggiungo l'elemento alla lista di elementi filtrati
                }
            }
            results.count = filteredModels.size //Il numero di elementi filtrati è uguale al numero di elementi della lista di elementi filtrati
            results.values = filteredModels //Il risultato del filtro è la lista di elementi filtrati

        }
else{
            results.count = filterList.size //Il numero di elementi filtrati è uguale al numero di elementi della lista di elementi filtrati
            results.values = filterList //Il risultato del filtro è la lista di elementi filtrati
        }
        return results //Ritorno i risultati del filtro
    }
    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
       adapterCategory.categoryArrayList =  results.values as ArrayList<ModelCategory> //Il risultato del filtro viene passato all'adapter della lista da filtrare
        adapterCategory.notifyDataSetChanged() //Notifico la modifica della lista


    }
}