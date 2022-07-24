package it.uninsubria.appappunti

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.uninsubria.appappunti.databinding.ActivityDashboardUserBinding

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding //lateinit perché non è ancora inizializzato

    private lateinit var firebaseAuth: FirebaseAuth //lateinit perché non è ancora inizializzato

    private lateinit var categoryArrayList: ArrayList<ModelCategory> // lista di categorie

    private lateinit var viewPagerAdapter: ViewPagerAdapter //adapter per il viewpager

    private lateinit var progressDialog: ProgressDialog //dialog per la loading

    override fun onCreate(savedInstanceState: Bundle?) { //quando l'activity viene creata
        super.onCreate(savedInstanceState) //chiamo il metodo della superclasse
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root) //setto il layout dell'activity

        firebaseAuth = FirebaseAuth.getInstance() //inizializzo il firebaseAuth
        checkUser() //controllo se l'utente è loggato

        setupWithViewPagerAdapter(binding.viewPager) //setto l'adapter del viewpager
        binding.tabLayout.setupWithViewPager(binding.viewPager) //setto il tablayout con il viewpager

        binding.loginBtn.setOnClickListener {
            firebaseAuth.signOut() //logout dell'utente
            startActivity(Intent(this, MainActivity::class.java)) //avvio l'activity di login
            finish() //finisco l'activity
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java)) //avvio l'activity di profilo
        }


    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) { //funzione per settare l'adapter del viewpager
        viewPagerAdapter = ViewPagerAdapter( //inizializzo l'adapter
            supportFragmentManager, //il fragment manager dell'activity
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, //il behavior del fragment manager
            this
        )

        categoryArrayList = ArrayList() //inizializzo la lista di categorie

        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener { //listener per la lettura dei dati dal database
            override fun onDataChange(snapshot: DataSnapshot) { //quando i dati sono stati letti
                categoryArrayList.clear() //svuoto la lista di categorie

                val modelAll = ModelCategory("01", "Tutti", 1, "") //creo una categoria per tutti i dati
                val modelMostViewer = ModelCategory("01", "Piu Visti", 1, "") //creo una categoria per i più visti
                val modelMostDownload = ModelCategory("01", "Piu Scaricati", 1, "") //creo una categoria per i più scaricati

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewer)
                categoryArrayList.add(modelMostDownload)

                viewPagerAdapter.addFragment( //aggiungo i fragment all'adapter
                    BookUserFragment.newInstance(
                        "${modelAll.id}", "${modelAll.category}", "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment( //aggiungo i fragment all'adapter
                    BookUserFragment.newInstance(
                        "${modelMostViewer.id}",
                        "${modelMostViewer.category}",
                        "${modelMostViewer.uid}"
                    ), modelMostViewer.category
                )
                viewPagerAdapter.addFragment( //aggiungo i fragment all'adapter
                    BookUserFragment.newInstance(
                        "${modelMostDownload.id}",
                        "${modelMostDownload.category}",
                        "${modelMostDownload.uid}"
                    ), modelMostDownload.category
                )
                viewPagerAdapter.notifyDataSetChanged()

                for (ds in snapshot.children){
                    val model=  ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}", "${model.category}", "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) { //quando ci sono errori
                // Fallito il caricamento delle informazioni

            }
        })

        viewPager.adapter = viewPagerAdapter //setto l'adapter del viewpager
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragmentList: ArrayList<BookUserFragment> = ArrayList() //lista di fragment

        private val fragmentTitleList: ArrayList<String> = ArrayList() //lista di titoli dei fragment
        private val context: Context //context dell'activity

        init { //inizializzo il context dell'activity
            this.context = context
        }

        override fun getCount(): Int = fragmentList.size //restituisce il numero di fragment

        override fun getItem(position: Int): Fragment { //restituisce il fragment in base alla posizione
            return fragmentList[position] //restituisce il fragment in base alla posizione
        }

        override fun getPageTitle(position: Int): CharSequence? { //restituisce il titolo del fragment in base alla posizione
            return fragmentTitleList[position] //restituisce il titolo del fragment in base alla posizione
        }

        fun addFragment(fragment: BookUserFragment, title: String) { //aggiungo un fragment alla lista
            fragmentList.add(fragment) //aggiungo il fragment alla lista
            fragmentTitleList.add(title) //aggiungo il titolo del fragment alla lista
        }
    }

    private fun checkUser() { //controllo se l'utente è loggato
        val firebaseUser = firebaseAuth.currentUser //ottengo l'utente loggato
        if(firebaseUser == null){ //se l'utente non è loggato
            binding.subTitleTv.text="Non sei Loggato" //setto il testo del subtitle

            binding.btnProfile.visibility = View.GONE //nascondo il bottone per il profilo
            binding.loginBtn.visibility = View.GONE //nascondo il bottone per il login
        }else {
            val email = firebaseUser.email //ottengo l'email dell'utente loggato
            binding.subTitleTv.text= email//setto il testo del subtitle

            binding.btnProfile.visibility = View.VISIBLE //mostro il bottone per il profilo
            binding.loginBtn.visibility = View.VISIBLE //mostro il bottone per il login


        }
    }


    }
