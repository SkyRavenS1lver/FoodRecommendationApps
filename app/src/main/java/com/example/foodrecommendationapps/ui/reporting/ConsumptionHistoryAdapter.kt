package com.example.foodrecommendationapps.ui.reporting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrecommendationapps.data.ReportDataClass
import com.example.foodrecommendationapps.databinding.ConsumptionHistoryItemBinding

class ConsumptionHistoryAdapter : RecyclerView.Adapter<ConsumptionHistoryAdapter.ViewHolder>() {

    private var historyList: List<ReportDataClass> = listOf()

    fun setData(list: List<ReportDataClass>) {
        historyList = list
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ConsumptionHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: ReportDataClass) {
            binding.foodNameText.text = "${history.food_name}"
            binding.portionText.text = "${history.portion_quantity} (${history.urt_name})"
            binding.percentageText.text = "${history.percentage}%"
            binding.timeText.text = history.date_report
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ConsumptionHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size
}
