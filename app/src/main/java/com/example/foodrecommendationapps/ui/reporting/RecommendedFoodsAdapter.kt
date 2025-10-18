package com.example.foodrecommendationapps.ui.reporting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import com.example.foodrecommendationapps.databinding.RecommendedFoodItemBinding

class RecommendedFoodsAdapter (
    private val foods: List<FoodRecommendationWithName>,
    private val onAddClick: (FoodRecommendationWithName) -> Unit
) : RecyclerView.Adapter<RecommendedFoodsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: RecommendedFoodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodRecommendationWithName, onAddClick: (FoodRecommendationWithName) -> Unit) {
            binding.foodNameText.text = food.nama_bahan
            binding.addButton.setOnClickListener {
                onAddClick(food)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecommendedFoodItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(foods[position], onAddClick)
    }

    override fun getItemCount(): Int = foods.size
}