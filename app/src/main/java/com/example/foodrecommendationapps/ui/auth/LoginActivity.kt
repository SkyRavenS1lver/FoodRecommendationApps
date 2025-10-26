package com.example.foodrecommendationapps.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.foodrecommendationapps.PrefsManager
import com.example.foodrecommendationapps.api.AuthApiService
import com.example.foodrecommendationapps.api.RetrofitBuilder
import com.example.foodrecommendationapps.data.InitDatabase
import com.example.foodrecommendationapps.data.LoginRequest
import com.example.foodrecommendationapps.databinding.ActivityLoginBinding
import com.example.foodrecommendationapps.ui.auth.viewmodel.UserViewModel
import com.example.foodrecommendationapps.ui.reporting.ReportList
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    private lateinit var binding: ActivityLoginBinding
    private val authApiService = RetrofitBuilder.build().create(AuthApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as InitDatabase).also { it->
            userViewModel = UserViewModel(it.repository)
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            return false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return false
        }

        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    private fun performLogin(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = authApiService.login(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()
                    if (response.body()!!.data != null){
                        userViewModel.loginUpdate(
                            loginResponse!!.data!!.token,
                            loginResponse.data.user_id,
                            loginResponse.data.food_recommendation,
                            request.email
                        )
                    }
                } else {
                    val message = response.body()?.message ?: "Login gagal"
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
        userViewModel.loggedUser.observe(this, Observer{ user ->
            user?.let {
                if (!user.isEmpty()) {
                    PrefsManager.saveToken(this@LoginActivity, user[0].latest_token?: "")
                    PrefsManager.saveUserId(this@LoginActivity, user[0].id)
                    Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, ReportList::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
    }
}