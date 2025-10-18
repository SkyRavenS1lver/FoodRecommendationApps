package com.example.foodrecommendationapps.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.foodrecommendationapps.PrefsManager
import com.example.foodrecommendationapps.R
import com.example.foodrecommendationapps.api.NetworkConnectivityChecker
import com.example.foodrecommendationapps.data.InitDatabase
import com.example.foodrecommendationapps.ui.auth.viewmodel.UserViewModel
import com.example.foodrecommendationapps.ui.reporting.ReportList

class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        (application as InitDatabase).also { it->
            userViewModel = UserViewModel(it.repository)
        }
        println("Connectivity Check")
        println(NetworkConnectivityChecker.isClientConnected(this))

        userViewModel.loggedUser.observe(this, Observer { data ->
            data?.let {
                if (data.isEmpty()) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    PrefsManager.saveToken(this@MainActivity, data[0].latest_token?: "")
                    PrefsManager.saveUserId(this@MainActivity, data[0].id)
                    val intent = Intent(this@MainActivity, ReportList::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
    }
}