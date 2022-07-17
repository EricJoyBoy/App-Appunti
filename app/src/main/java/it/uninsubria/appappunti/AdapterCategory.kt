package it.uninsubria.appappunti

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import it.uninsubria.appappunti.databinding.RowCategoryBinding

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>,Filterable {

    private val context: Context //permette di accedere ai vari metodi di android
    public var categoryArrayList: ArrayList<ModelCategory>

    private var filterList: ArrayList<ModelCategory>//lista di tutte le categorie
    private var filter: FilterCategory? = null //filtro per la ricerca

    private lateinit var binding: RowCategoryBinding //permette di accedere ai vari metodi di databinding


    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context //context permette di accedere ai vari metodi di android
        this.categoryArrayList = categoryArrayList //lista di tutte le categorie
        this.filterList = categoryArrayList //lista di tutte le categorie
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root) //crea la view della categoria
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = categoryArrayList[position] //prende la categoria dalla lista
        val id = model.id //id della categoria
        val category = model.category //prende la categoria dall'oggetto ModelCategory
        val uid = model.uid //id dell'utente che ha creato la categoria
        val timestamp = model.timestamp //timestamp della categoria


        holder.categoryTv.text = category //inserisce la categoria nella view

        holder.deleteBtn.setOnClickListener{ //listener per il bottone di eliminazione
            val builder = AlertDialog.Builder(context) //crea un alert dialog per la conferma dell'eliminazione
            builder.setTitle("Cancella") //titolo dell'alert dialog
                .setMessage("Sei sicuro di voler cancellare questa categoria?") //messaggio dell'alert dialog
                .setPositiveButton("Conferma"){dialog, which -> //listener per il bottone di conferma


                    Toast.makeText(context, "Cancellazione dal database...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model,holder) //cancella la categoria dal database

                }
                .setNegativeButton("Annulla"){dialog, which ->
                    dialog.dismiss() //chiude l'alert dialog
                }
                .show() //mostra l'alert dialog
        }


    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
       val id = model.id //id della categoria

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories").child(id)
        ref.child(id) //prende la categoria dal database
            .removeValue() //cancella la categoria dal database
            .addOnSuccessListener {
                Toast.makeText(context, "Categoria cancellata con successo", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e ->
                Toast.makeText(context, "Errore: $e", Toast.LENGTH_SHORT).show()
            }

    }


    override fun getItemCount(): Int {
        return categoryArrayList.size //ritorna la lunghezza della lista
    }


 //
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryTv: TextView = binding.categoryTvRowCategory //prende il testo della categoria
        var deleteBtn: ImageButton = binding.deleteBtnRowCategory //prende il bottone di eliminazione



    }

    override fun getFilter(): Filter {
        if (filter == null) { //se non è stato ancora creato un filtro
            filter = FilterCategory(filterList, this) //crea un filtro
        }
        return filter as Filter //ritorna il filtro
    }


}