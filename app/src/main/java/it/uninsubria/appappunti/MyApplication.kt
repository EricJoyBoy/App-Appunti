package it.uninsubria.appappunti

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import android.text.format.DateFormat
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, PdfTitle: String, tvSize: TextView) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener {
                val bytes = it.sizeBytes.toDouble()
                //chuyển byte sang KB/MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb > 1) {
                    tvSize.text = "${String.format("%.2f", mb)} + MB"
                } else if (kb >= 1) {
                    tvSize.text = "${String.format("%.2f", kb)} + KB"
                } else {
                    tvSize.text = "${String.format("%.2f", bytes)} + bytes"
                }
            }.addOnFailureListener {

            }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            tvPages: TextView?
        ) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->

                pdfView.fromBytes(bytes)
                    .pages(0) //trang đầu tiên
                    .spacing(0)
                    .swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError {
                        progressBar.visibility = View.INVISIBLE
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                    }
                    .onLoad { nbPages ->
                        //pdf được tải, có thể đặt số lượng trang, hình thu nhỏ pdf
                        progressBar.visibility = View.INVISIBLE

                        //nếu tvPages param không phải là null thì hãy đặt số trang
                        if (tvPages != null) {
                            tvPages.text = "$nbPages"
                        }
                    }.load()
            }.addOnFailureListener {
                //Log.d("ndt", "failed")
            }
        }

        fun loadCategory(categoryId: String, tvCategory: TextView) {
            //load category dùng category id từ firebase
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val category: String = "${snapshot.child("category").value}"

                    tvCategory.text = category
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Cancellazioine del File")
            progressDialog.setMessage("Cancellazione $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Cancellazione andata a buon fine", Toast.LENGTH_SHORT)
                                .show()

                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Connessione Fallita al Database", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Fallita la connessione allo Storage", Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementBookViewCount(bookId: String) {
            //get current book views count
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get luot xem
                        var viewCount = "${snapshot.child("viewsCount").value}"

                        if (viewCount == "" || viewCount == "null") {
                            viewCount = "0"
                        }
                        //tang view
                        val newViewCount = viewCount.toLong() + 1

                        //setup data va update trong db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun removeFromFavorite(context: Context, bookId: String) {

            val firebaseAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance("https://app-appunti-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Thất bại", Toast.LENGTH_SHORT).show()
                }
        }
    }
}