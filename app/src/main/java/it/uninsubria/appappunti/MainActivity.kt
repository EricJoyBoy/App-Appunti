package it.uninsubria.appappunti


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.uninsubria.appappunti.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // handle click on the button, login
        binding.loginBtn.setOnClickListener {
            // do something, login
        }
        // handle click on the button, register
        binding.skipBtn.setOnClickListener {
            // do something, register
        }



    }
}