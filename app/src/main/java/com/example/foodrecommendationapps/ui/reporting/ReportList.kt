package com.example.foodrecommendationapps.ui.reporting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrecommendationapps.PrefsManager
import com.example.foodrecommendationapps.api.NetworkConnectivityChecker
import com.example.foodrecommendationapps.api.RetrofitBuilder
import com.example.foodrecommendationapps.api.SyncApiService
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.ConsumptionSyncRequest
import com.example.foodrecommendationapps.data.InitDatabase
import com.example.foodrecommendationapps.data.ReportDataClass
import com.example.foodrecommendationapps.data.UserProfile
import com.example.foodrecommendationapps.databinding.ActivityReportListBinding
import com.example.foodrecommendationapps.repo.DatabaseRepository
import com.example.foodrecommendationapps.ui.auth.viewmodel.UserViewModel
import com.example.foodrecommendationapps.ui.reporting.view_model.ReportViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportList : AppCompatActivity() {
    private val reportViewModel: ReportViewModel = ReportViewModel()
    private var allReportList: List<ReportDataClass> = listOf()
    private val userId by lazy { PrefsManager.getUserId(this) }
    private lateinit var repository: DatabaseRepository
    private lateinit var binding: ActivityReportListBinding
    private lateinit var adapter: ConsumptionHistoryAdapter
    private lateinit var syncApiService: SyncApiService
    private lateinit var userViewModel: UserViewModel
    private var alreadySync = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as InitDatabase).also {
            userViewModel = UserViewModel(it.repository)
            repository = it.repository
        }
        userViewModel.loggedUser.observe(this, Observer{
            lifecycleScope.launch {
                if (!alreadySync){
                    alreadySync = true
                    syncApiService = RetrofitBuilder.build(it.firstOrNull()?.latest_token?: "").create(SyncApiService::class.java)
                    syncProfileData()
                    syncData()
                }
            }
        })

        setupRecyclerView()
        setupDatePicker()
        setupObserver()
        setupFabButton()

        // Initialize with today's date
        val calendar = Calendar.getInstance()
        updateSelectedDate(calendar.get(Calendar.YEAR),
                          calendar.get(Calendar.MONTH),
                          calendar.get(Calendar.DAY_OF_MONTH))
    }
    private fun syncProfileData(){
        if (NetworkConnectivityChecker.isClientConnected(this)){
            lifecycleScope.launch {
                val data = repository.isUserUnsynced(userId)
                if (!data.isEmpty()){
                    val response = syncApiService.syncProfile(data[0])
                    if (response.isSuccessful && response.body()?.success == true) {
                        val userFromServer = response.body()!!.data
                        if (userFromServer != null){
                            val updatedUserProfile = UserProfile(
                                id = userId,
                                name = userFromServer.name,
                                email = userFromServer.email,
                                age = userFromServer.age,
                                gender = userFromServer.gender,
                                height = userFromServer.height,
                                weight = userFromServer.weight,
                                activity = userFromServer.activity,
                                latest_token = data[0].latest_token,
                                is_logged_in = data[0].is_logged_in,
                                updated_at = userFromServer.updated_at,
                                sync_status = 1,
                                last_sync = data[0].last_sync
                            )
                            repository.updateUser(updatedUserProfile)
                        }
                    }
                }
            }
        }
    }

    private fun syncData() {
        if (NetworkConnectivityChecker.isClientConnected(this)){
            lifecycleScope.launch {
                val data = repository.getAllUnSyncedHistory(userId)
                val last_sync = userViewModel.loggedUser.value?.get(0)?.last_sync
                var needUpdate = false
                val response = syncApiService.syncConsumption(ConsumptionSyncRequest(last_sync, data))
                if (response.isSuccessful && response.body()?.success == true) {
                    if (response.body()!!.data != null){
                        if (!response.body()!!.data!!.server_changes.isEmpty()){
                            needUpdate = true;
                            val data = response.body()!!.data!!.server_changes
                            val newDatas = ArrayList<ConsumptionHistory>()
                            data.forEach { it->
                                val newData = ConsumptionHistory(
                                    user_id =it.user_id,
                                    food_id = it.food_id,
                                    urt_id =it.urt_id,
                                    portion_quantity = it.portion_quantity,
                                    percentage = it.percentage,
                                    date_report = it.date_report,
                                    id = it.id,
                                    updated_at = it.updated_at,
                                    sync_status = 1
                                )
                                newDatas.add(newData)
                            }
                            repository.addConsumptionHistories(newDatas)
                        }
                        if (!response.body()!!.data!!.accepted.isEmpty()){
                            needUpdate = true;
                            repository.updateSyncedConsumptionHistory(response.body()!!.data!!.accepted)
                        }
                        if (!response.body()!!.data!!.conflicts.isEmpty()){
                            needUpdate = true;
                            response.body()!!.data!!.conflicts.forEach {repository.updateConsumptionHistory(it)}
                        }
                        if (!response.body()!!.data!!.food_recommendation.isEmpty()){
                            needUpdate = true;
                            Toast.makeText(this@ReportList, "Rekomendasi Makanan Terupdate", Toast.LENGTH_SHORT).show()
                            repository.deleteAllRecommendation(userId)
                            repository.addAllRecommendation(response.body()!!.data!!.food_recommendation)
                        }
                        if (needUpdate){
                            if (!response.body()!!.data!!.sync_timestamp.isEmpty()){
                                val currentUser = userViewModel.loggedUser.value?.firstOrNull()
                                if (currentUser != null){
                                    repository.updateLastSync(response.body()!!.data!!.sync_timestamp, userId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dateString = reportViewModel.selectedDate.value
            if (dateString != null) {
                allReportList = repository.getConsumptionByDate(userId, dateString)
            }
            adapter.setData(allReportList)
            // Update UI with count and toggle empty state
            binding.selectedDateText.text = "Selected: $dateString (${allReportList.size} items)"
            updateEmptyState(allReportList.isEmpty())
        }
    }


    private fun setupRecyclerView() {
        adapter = ConsumptionHistoryAdapter()
        binding.reportRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReportList)
            adapter = this@ReportList.adapter
        }
    }

    private fun setupDatePicker() {
        binding.datePicker.init(
            binding.datePicker.year,
            binding.datePicker.month,
            binding.datePicker.dayOfMonth
        ) { view, year, monthOfYear, dayOfMonth ->
            updateSelectedDate(year, monthOfYear, dayOfMonth)
        }
    }

    private fun setupObserver() {
        reportViewModel.selectedDate.observe(this, Observer { dateString ->
            lifecycleScope.launch {
                allReportList = repository.getConsumptionByDate(userId, dateString)
                adapter.setData(allReportList)

                // Update UI with count and toggle empty state
                binding.selectedDateText.text = "Selected: $dateString (${allReportList.size} items)"
                updateEmptyState(allReportList.isEmpty())
            }
        })
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.reportRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.reportRecyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    private fun updateSelectedDate(year: Int, month: Int, day: Int) {
        // Format date as YYYY-MM-DD
        val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
        reportViewModel.selectedDate.value = formattedDate
    }

    private fun setupFabButton() {
        binding.fabAddReport.setOnClickListener {
            val intent = Intent(this, FoodReportingFormActivity::class.java)
            startActivity(intent)
        }
    }
}