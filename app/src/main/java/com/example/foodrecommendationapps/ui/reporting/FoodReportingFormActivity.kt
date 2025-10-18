package com.example.foodrecommendationapps.ui.reporting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrecommendationapps.PrefsManager
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.FoodConsumed
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import com.example.foodrecommendationapps.data.InitDatabase
import com.example.foodrecommendationapps.databinding.ActivityFoodReportingFormBinding
import com.example.foodrecommendationapps.repo.DatabaseRepository
import com.example.foodrecommendationapps.ui.auth.viewmodel.UserViewModel
import com.example.foodrecommendationapps.ui.reporting.view_model.FoodViewModel
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodReportingFormActivity : AppCompatActivity() {
    private lateinit var repository: DatabaseRepository
    private val foodConsumedList = MutableLiveData<MutableList<FoodConsumed>>(mutableListOf())
    private lateinit var binding: ActivityFoodReportingFormBinding
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var foodConsumedAdapter: FoodConsumedAdapter? = null
    private lateinit var foodViewModel: FoodViewModel
    private lateinit var userViewModel: UserViewModel
    private val userId by lazy {
        PrefsManager.getUserId(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodReportingFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val app = (application as InitDatabase).also {
            foodViewModel = FoodViewModel(it.repository, PrefsManager.getUserId(this))
            userViewModel = UserViewModel(it.repository)
        }
        repository = app.repository

        // Restore saved state if available
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        setupDatePicker()
        setupTimePicker()
        setupRecommendedFoodsRecyclerView()
        setupAddMoreButton()
        setupSubmitButton()

        lifecycleScope.launch {
            setupFoodConsumedRecyclerView(repository)
            observeFoodListChanges()
        }
    }

    private fun observeFoodListChanges() {
        foodConsumedList.observe(this) { list ->
            foodConsumedAdapter?.setFoodList(list)
        }
    }

    private fun setupRecommendedFoodsRecyclerView() {
        foodViewModel.recommendedFoods.observe(this, Observer { foodList ->
            val adapter = RecommendedFoodsAdapter(foodList) { food ->
                addFoodToConsumed(food)
            }
            binding.recommendedFoodsRecyclerView.adapter = adapter
            binding.recommendedFoodsRecyclerView.layoutManager = LinearLayoutManager(this)
        })
    }

    private fun setupDatePicker() {
        binding.dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        updateDateDisplay()
    }

    private fun setupTimePicker() {
        binding.timeInput.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    updateTimeDisplay()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        updateTimeDisplay()
    }

    private fun updateDateDisplay() {
        binding.dateInput.setText(dateFormat.format(calendar.time))
    }

    private fun updateTimeDisplay() {
        binding.timeInput.setText(timeFormat.format(calendar.time))
    }

    private fun addFoodToConsumed(food: FoodRecommendationWithName) {
        val currentList = foodConsumedList.value ?: mutableListOf()
        currentList.add(FoodConsumed(foodId = food.id, foodName = food.nama_bahan.toString()))
        foodConsumedList.postValue(currentList)
        Toast.makeText(this, "${food.nama_bahan} added", Toast.LENGTH_SHORT).show()
    }

    private suspend fun setupFoodConsumedRecyclerView(repository: DatabaseRepository) {
        foodViewModel.allFoods.observe(this, Observer { foodList ->
            foodConsumedAdapter = FoodConsumedAdapter(
                foodList,
                this,
                lifecycleScope,
                { foodId ->
                    repository.getPortionsListForFood(foodId, 0)
                },
                { position -> deleteFoodItem(position) }
            )
            binding.foodConsumedRecyclerView.adapter = foodConsumedAdapter
            binding.foodConsumedRecyclerView.layoutManager = LinearLayoutManager(this)
        })
    }

    private fun setupAddMoreButton() {
        binding.addMoreButton.setOnClickListener {
            val currentList = foodConsumedList.value ?: mutableListOf()
            currentList.add(FoodConsumed())
            foodConsumedList.value = currentList
        }
    }

    private fun deleteFoodItem(position: Int) {
        val currentList = foodConsumedList.value ?: mutableListOf()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            foodConsumedList.value = currentList
        }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                submitData()
            }
        }
    }

    private fun validateForm(): Boolean {
        val date = binding.dateInput.text.toString().trim()
        val time = binding.timeInput.text.toString().trim()
        val list = foodConsumedList.value ?: emptyList()

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return false
        }

        if (list.isEmpty()) {
            Toast.makeText(this, "Please add at least one food", Toast.LENGTH_SHORT).show()
            return false
        }

        for ((index, food) in list.withIndex()) {
            if (food.foodName.isEmpty()) {
                Toast.makeText(this, "Food name is required at row ${index + 1}", Toast.LENGTH_SHORT).show()
                return false
            }
            if (food.portionType.isEmpty()) {
                Toast.makeText(this, "Portion type is required at row ${index + 1}", Toast.LENGTH_SHORT).show()
                return false
            }
            if (food.portionQuantity <= 0) {
                Toast.makeText(this, "Portion quantity must be greater than 0 at row ${index + 1}", Toast.LENGTH_SHORT).show()
                return false
            }
            if (food.percentage <= 0 || food.percentage > 100) {
                Toast.makeText(this, "Percentage must be between 1 and 100 at row ${index + 1}", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun submitData() {
        val dateString = binding.dateInput.text.toString()
        val time = binding.timeInput.text.toString()

        // Parse date from dd/MM/yyyy to yyyy-MM-dd format
        val inputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: String = try {
            val parsedDate = inputFormatter.parse(dateString)
            outputFormatter.format(parsedDate ?: Date())
        } catch (e: ParseException) {
            outputFormatter.format(Date())
        }

        // Generate timestamp and current datetime
        val timestamp = System.currentTimeMillis() / 1000
        val datetimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatted = datetimeFormatter.format(Date())

        val newConsumptions = ArrayList<ConsumptionHistory>()

        (foodConsumedList.value ?: mutableListOf()).forEachIndexed { index, it ->
            val history = ConsumptionHistory(
                id = "${userId}|${index}|${timestamp}",
                user_id = userId,
                food_id = it.foodId,
                urt_id = it.urtId,
                portion_quantity = it.portionQuantity,
                percentage = it.percentage,
                date_report = "$date $time",
                updated_at = formatted,
                sync_status = 0,
            )
            newConsumptions.add(history)
        }
        foodViewModel.addFoodConsumptions(newConsumptions).also {
            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
            resetForm()
            val intent = Intent(this@FoodReportingFormActivity, ReportList::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun resetForm() {
        binding.dateInput.setText("")
        binding.timeInput.setText("")
        foodConsumedList.value = mutableListOf()
        calendar.timeInMillis = System.currentTimeMillis()
        updateDateDisplay()
        updateTimeDisplay()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save calendar timestamp
        outState.putLong(KEY_CALENDAR_TIME, calendar.timeInMillis)

        // Save food consumed list
        val list = foodConsumedList.value ?: mutableListOf()
        outState.putInt(KEY_FOOD_LIST_SIZE, list.size)

        list.forEachIndexed { index, food ->
            outState.putInt("${KEY_FOOD_ID}_$index", food.foodId)
            outState.putInt("${KEY_URT_ID}_$index", food.urtId)
            outState.putString("${KEY_FOOD_NAME}_$index", food.foodName)
            outState.putString("${KEY_PORTION_TYPE}_$index", food.portionType)
            outState.putDouble("${KEY_PORTION_QUANTITY}_$index", food.portionQuantity)
            outState.putDouble("${KEY_PERCENTAGE}_$index", food.percentage)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        // Restore calendar
        val calendarTime = savedInstanceState.getLong(KEY_CALENDAR_TIME, -1L)
        if (calendarTime != -1L) {
            calendar.timeInMillis = calendarTime
        }

        // Restore food consumed list
        val listSize = savedInstanceState.getInt(KEY_FOOD_LIST_SIZE, 0)
        if (listSize > 0) {
            val restoredList = mutableListOf<FoodConsumed>()
            for (index in 0 until listSize) {
                val food = FoodConsumed(
                    foodId = savedInstanceState.getInt("${KEY_FOOD_ID}_$index", 0),
                    urtId = savedInstanceState.getInt("${KEY_URT_ID}_$index", 0),
                    foodName = savedInstanceState.getString("${KEY_FOOD_NAME}_$index", ""),
                    portionType = savedInstanceState.getString("${KEY_PORTION_TYPE}_$index", ""),
                    portionQuantity = savedInstanceState.getDouble("${KEY_PORTION_QUANTITY}_$index", 0.0),
                    percentage = savedInstanceState.getDouble("${KEY_PERCENTAGE}_$index", 100.0)
                )
                restoredList.add(food)
            }
            foodConsumedList.value = restoredList
        }
    }

    companion object {
        private const val KEY_CALENDAR_TIME = "calendar_time"
        private const val KEY_FOOD_LIST_SIZE = "food_list_size"
        private const val KEY_FOOD_ID = "food_id"
        private const val KEY_URT_ID = "urt_id"
        private const val KEY_FOOD_NAME = "food_name"
        private const val KEY_PORTION_TYPE = "portion_type"
        private const val KEY_PORTION_QUANTITY = "portion_quantity"
        private const val KEY_PERCENTAGE = "percentage"
    }
}