package it.uninsubria.appappunti


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.uninsubria.appappunti.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() { //AppCompatActivity permette di usare il supportActionBar

    private lateinit var binding: ActivityMainBinding //lateinit perchè non è ancora inizializzato

    override fun onCreate(savedInstanceState: Bundle?) { // override perchè è un metodo di AppCompatActivity
        super.onCreate(savedInstanceState) // super perchè è un metodo di AppCompatActivity
        binding = ActivityMainBinding.inflate(layoutInflater) //inflate perchè è un metodo di LayoutInflater
        setContentView(binding.root) // setContentView perchè è un metodo di AppCompatActivity



        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }



    }
}