package com.example.foodrecommendationapps.ui.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.foodrecommendationapps.PrefsManager
import com.example.foodrecommendationapps.api.AuthApiService
import com.example.foodrecommendationapps.api.NetworkConnectivityChecker
import com.example.foodrecommendationapps.api.RetrofitBuilder
import com.example.foodrecommendationapps.data.InitDatabase
import com.example.foodrecommendationapps.data.RegisterRequest
import com.example.foodrecommendationapps.data.UserProfile
import com.example.foodrecommendationapps.databinding.ActivityRegisterBinding
import com.example.foodrecommendationapps.ui.auth.viewmodel.UserViewModel
import com.example.foodrecommendationapps.ui.reporting.ReportList
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: ActivityRegisterBinding
    private val authApiService = RetrofitBuilder.build().create(AuthApiService::class.java)

    private val genderOptions = listOf("Laki-laki", "Perempuan")

    private val activityLevels = listOf(
        "Sangat Jarang berolahraga",
        "Jarang olahraga (1-3 kali per minggu)",
        "Cukup olahraga (3-5 kali per minggu)",
        "Sering olahraga (6-7 kali per minggu)"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as InitDatabase).also { it->
            userViewModel = UserViewModel(it.repository)
        }

        setupDropdowns()
        setupClickListeners()
        userViewModel.loggedUser.observe(this, Observer{ user ->
            user?.let {
                if (!user.isEmpty()) {
                    PrefsManager.saveToken(this@RegisterActivity, user[0].latest_token?: "")
                    PrefsManager.saveUserId(this@RegisterActivity, user[0].id)
                    Toast.makeText(this@RegisterActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, ReportList::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun setupDropdowns() {
        // Setup Gender Dropdown
        val genderAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, genderOptions)
        binding.actvGender.setAdapter(genderAdapter)

        // Setup Activity Level Dropdown
        val activityAdapter =
            ArrayAdapter(this, R.layout.simple_dropdown_item_1line, activityLevels)
        binding.actvLevelAktivitas.setAdapter(activityAdapter)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                performRegister()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val namaLengkap = binding.etNamaLengkap.text.toString().trim()
        val gender = binding.actvGender.text.toString()
        val umurText = binding.etUmur.text.toString().trim()
        val tinggiBadanText = binding.etTinggiBadan.text.toString().trim()
        val beratBadanText = binding.etBeratBadan.text.toString().trim()
        val levelAktivitas = binding.actvLevelAktivitas.text.toString()

        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            return false
        }
        binding.tilEmail.error = null

        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return false
        }
        binding.tilPassword.error = null

        // Nama Lengkap validation
        if (namaLengkap.isEmpty()) {
            binding.tilNamaLengkap.error = "Nama lengkap tidak boleh kosong"
            return false
        }
        binding.tilNamaLengkap.error = null

        // Gender validation
        if (gender.isEmpty()) {
            binding.tilGender.error = "Pilih jenis kelamin"
            return false
        }
        binding.tilGender.error = null

        // Umur validation
        if (umurText.isEmpty()) {
            binding.tilUmur.error = "Umur tidak boleh kosong"
            return false
        }
        val umur = umurText.toIntOrNull()
        if (umur == null || umur < 1 || umur > 120) {
            binding.tilUmur.error = "Umur tidak valid"
            return false
        }
        binding.tilUmur.error = null

        // Tinggi Badan validation
        if (tinggiBadanText.isEmpty()) {
            binding.tilTinggiBadan.error = "Tinggi badan tidak boleh kosong"
            return false
        }
        val tinggiBadan = tinggiBadanText.toDoubleOrNull()
        if (tinggiBadan == null || tinggiBadan < 50 || tinggiBadan > 250) {
            binding.tilTinggiBadan.error = "Tinggi badan tidak valid"
            return false
        }
        binding.tilTinggiBadan.error = null

        // Berat Badan validation
        if (beratBadanText.isEmpty()) {
            binding.tilBeratBadan.error = "Berat badan tidak boleh kosong"
            return false
        }
        val beratBadan = beratBadanText.toDoubleOrNull()
        if (beratBadan == null || beratBadan < 20 || beratBadan > 300) {
            binding.tilBeratBadan.error = "Berat badan tidak valid"
            return false
        }
        binding.tilBeratBadan.error = null

        // Level Aktivitas validation
        if (levelAktivitas.isEmpty()) {
            binding.tilLevelAktivitas.error = "Pilih level aktivitas"
            return false
        }
        binding.tilLevelAktivitas.error = null

        return true
    }

    private fun performRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val namaLengkap = binding.etNamaLengkap.text.toString().trim()
        val gender = genderOptions.indexOf(binding.actvGender.text.toString()) + 1
        val umur = binding.etUmur.text.toString().toInt()
        val tinggiBadan = binding.etTinggiBadan.text.toString().toDouble()
        val beratBadan = binding.etBeratBadan.text.toString().toDouble()
        val levelAktivitas = activityLevels.indexOf(binding.actvLevelAktivitas.text.toString()) + 1

        println("Internet Check")
        if(NetworkConnectivityChecker.isClientConnected(this)){
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            lifecycleScope.launch {
                try {
                    val request = RegisterRequest(
                        email = email,
                        password = password,
                        name = namaLengkap,
                        gender = gender,
                        age = umur,
                        height = tinggiBadan,
                        weight = beratBadan,
                        activity = levelAktivitas
                    )
                    val response = authApiService.register(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        if (response.body()!!.data != null){
                            val newUser = UserProfile(
                                response.body()!!.data!!.user_id,
                                request.name,
                                request.email,
                                request.age,
                                request.gender-1,
                                request.height,
                                request.weight,
                                request.activity,
                                response.body()!!.data!!.token,
                                true,
                                response.body()!!.data!!.updated_at,
                                1,
                                null
                            )
                            // Wait for registration to complete before the observer navigates
                            userViewModel.register(response.body()!!, newUser)
                        }
                    } else {
                        val message = response.body()?.message ?: "Registrasi gagal"
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val errorMessage = when {
                        e.message?.contains("FOREIGN KEY constraint failed") == true ->
                            "Terjadi kesalahan pada data rekomendasi. Silakan coba lagi."
                        e.message?.contains("network") == true || e.message?.contains("timeout") == true ->
                            "Koneksi terputus. Silakan periksa jaringan Anda."
                        else -> "Error: ${e.message ?: "Terjadi kesalahan tidak dikenal"}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                }
            }
        }
        else{
            Toast.makeText(this@RegisterActivity, "Mohon periksa jaringan internet Anda", Toast.LENGTH_SHORT).show()
        }
    }
}