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

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.loginBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        //load category từ db
        val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "Tutti", 1, "")
                val modelMostViewer = ModelCategory("01", "Piu Visti", 1, "")
                val modelMostDownload = ModelCategory("01", "Piu Scaricati", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewer)
                categoryArrayList.add(modelMostDownload)

                //add vào viewpager
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelAll.id}", "${modelAll.category}", "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostViewer.id}",
                        "${modelMostViewer.category}",
                        "${modelMostViewer.uid}"
                    ), modelMostViewer.category
                )
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostDownload.id}",
                        "${modelMostDownload.category}",
                        "${modelMostDownload.uid}"
                    ), modelMostDownload.category
                )
                viewPagerAdapter.notifyDataSetChanged()

                //load từ firebasedb
                for (ds in snapshot.children){
                    val model=  ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewpager
                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}", "${model.category}", "${model.uid}"
                        ), model.category
                    )
                    //load lại list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        //giữ danh sách fragment, newInstance cùng một fragment cho mỗi danh mục
        private val fragmentList: ArrayList<BookUserFragment> = ArrayList()

        //danh sách tiêu đề thanh tab
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BookUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            binding.subTitleTv.text="Non sei Loggato"

            binding.btnProfile.visibility = View.GONE
            binding.loginBtn.visibility = View.GONE
        }else {
            val email = firebaseUser.email
            binding.subTitleTv.text= email

            binding.btnProfile.visibility = View.VISIBLE
            binding.loginBtn.visibility = View.VISIBLE


        }
    }


    }
