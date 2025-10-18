package com.example.foodrecommendationapps.ui.reporting

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrecommendationapps.data.DataMakananNameOnly
import com.example.foodrecommendationapps.data.FoodConsumed
import com.example.foodrecommendationapps.data.UrtList
import com.example.foodrecommendationapps.databinding.FoodConsumedRowBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Food Consumed Adapter
class FoodConsumedAdapter(
    private val foodListFromDb: List<DataMakananNameOnly>,
    private val lifecycleOwner: LifecycleOwner,
    private val coroutineScope: CoroutineScope,
    private val onUrtQuery: suspend (foodId: Int) -> List<UrtList>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<FoodConsumedAdapter.ViewHolder>() {

    private var foodConsumedList: MutableList<FoodConsumed> = mutableListOf()
    private val urtCache = mutableMapOf<Int, MutableLiveData<List<UrtList>>>()

    fun setFoodList(list: List<FoodConsumed>) {
        foodConsumedList = list.toMutableList()
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: FoodConsumedRowBinding,
        private val foodListFromDb: List<DataMakananNameOnly>,
        private val lifecycleOwner: LifecycleOwner,
        private val coroutineScope: CoroutineScope,
        private val onUrtQuery: suspend (foodId: Int) -> List<UrtList>,
        private val onDelete: (Int) -> Unit,
        private val foodConsumedList: MutableList<FoodConsumed>,
        private val urtCache: MutableMap<Int, MutableLiveData<List<UrtList>>>
    ) : RecyclerView.ViewHolder(binding.root) {

        private var portionQuantityWatcher: TextWatcher? = null
        private var percentageWatcher: TextWatcher? = null

        fun bind(food: FoodConsumed, position: Int) {
            // Setup Food Name Dropdown from database
//            val availableFoods = foodListFromDb.filter { dbFood ->
//                dbFood.id == food.foodId || foodConsumedList.none { it.foodId == dbFood.id }
//            }
            val availableFoods = foodListFromDb
            val foodNames = availableFoods.map { it.nama_bahan }

            val foodAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_dropdown_item_1line,
                foodNames
            )
            binding.foodNameDropdown.setAdapter(foodAdapter)
            binding.foodNameDropdown.setText(food.foodName, false)

            binding.foodNameDropdown.setOnItemClickListener { _, _, index, _ ->
                if (index >= 0 && index < availableFoods.size) {
                    val selectedFood = availableFoods[index]
                    food.foodId = selectedFood.id
                    food.foodName = selectedFood.nama_bahan.toString()
                    setupPortionTypeObserver(food)
                }
            }

            binding.foodNameDropdown.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && binding.foodNameDropdown.text.toString().isNotEmpty()) {
                    val selectedFood = availableFoods.find {
                        it.nama_bahan == binding.foodNameDropdown.text.toString()
                    }
                    if (selectedFood != null) {
                        food.foodId = selectedFood.id
                        food.foodName = selectedFood.nama_bahan.toString()
                        setupPortionTypeObserver(food)
                    }
                }
            }

            // Setup portion type observer if food already has ID
            if (food.foodId > 0) {
                setupPortionTypeObserver(food)
            } else {
                // Clear the portion type dropdown if no food is selected
                binding.portionTypeDropdown.setAdapter(null)
                binding.portionTypeDropdown.setText("", false)
            }

            // Setup Portion Quantity Input
            // Remove old watcher if exists
            portionQuantityWatcher?.let { binding.portionSizeInput.removeTextChangedListener(it) }

            // Set text without triggering listener
            binding.portionSizeInput.setText(
                if (food.portionQuantity > 0) food.portionQuantity.toString() else ""
            )

            // Create and add new watcher
            portionQuantityWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val quantity = s.toString().toDoubleOrNull() ?: 0.0
                    food.portionQuantity = quantity
                }
            }
            binding.portionSizeInput.addTextChangedListener(portionQuantityWatcher)

            // Setup Percentage Input
            // Remove old watcher if exists
            percentageWatcher?.let { binding.percentageInput.removeTextChangedListener(it) }

            // Set text without triggering listener (default is 100.0)
            binding.percentageInput.setText(food.percentage.toString())

            // Create and add new watcher
            percentageWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val percentage = s.toString().toDoubleOrNull() ?: 100.0
                    food.percentage = percentage.coerceIn(0.1, 100.0)
                }
            }
            binding.percentageInput.addTextChangedListener(percentageWatcher)

            // Setup Delete Button
            binding.deleteButton.setOnClickListener {
                onDelete(position)
            }
        }

        private fun setupPortionTypeObserver(food: FoodConsumed) {
            // Get or create cache entry for this food
            val urtLiveData = urtCache.getOrPut(food.foodId) {
                MutableLiveData<List<UrtList>>()
            }

            // Remove old observer
            urtLiveData.removeObservers(lifecycleOwner)

            // Observe URT list
            urtLiveData.observe(lifecycleOwner) { portionTypes ->
                updatePortionTypeDropdown(binding, food, portionTypes)
            }

            // Query if empty
            if (urtLiveData.value?.isEmpty() != false) {
                coroutineScope.launch {
                    val portionTypes = ArrayList(onUrtQuery(food.foodId))
                    portionTypes.add(UrtList(0, "gram/ml", 1.0, ""))
                    urtLiveData.postValue(portionTypes)
                }
            }
        }

        private fun updatePortionTypeDropdown(
            binding: FoodConsumedRowBinding,
            food: FoodConsumed,
            portionTypes: List<UrtList>
        ) {
            val portionAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_dropdown_item_1line,
                portionTypes.map { it.nama_urt }
            )
            binding.portionTypeDropdown.setAdapter(portionAdapter)

            // Only set text if portionType is not empty, otherwise show hint
            if (food.portionType.isNotEmpty()) {
                binding.portionTypeDropdown.setText(food.portionType, false)
            } else {
                binding.portionTypeDropdown.setText("", false)
            }

            binding.portionTypeDropdown.setOnItemClickListener { _, _, index, _ ->
                if (index >= 0 && index < portionTypes.size) {
                    val selectedUrt = portionTypes[index]
                    food.urtId = selectedUrt.id
                    food.portionType = selectedUrt.nama_urt.toString()
                }
            }

            binding.portionTypeDropdown.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && binding.portionTypeDropdown.text.toString().isNotEmpty()) {
                    val selectedUrt = portionTypes.find {
                        it.nama_urt == binding.portionTypeDropdown.text.toString()
                    }
                    if (selectedUrt != null) {
                        food.urtId = selectedUrt.id
                        food.portionType = selectedUrt.nama_urt.toString()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FoodConsumedRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, foodListFromDb, lifecycleOwner, coroutineScope, onUrtQuery, onDelete, foodConsumedList, urtCache)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(foodConsumedList[position], position)
    }

    override fun getItemCount(): Int = foodConsumedList.size
}
