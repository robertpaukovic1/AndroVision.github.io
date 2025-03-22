package com.example.projekt2025

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class PieChartFragment : Fragment() {

    private lateinit var camera2ViewModel: Camera2ViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pie_chart, container, false)

        camera2ViewModel = ViewModelProvider(requireActivity()).get(Camera2ViewModel::class.java)

        // Povezivanje PieChart-a sa XML-om
        val pieChart: PieChart = view.findViewById(R.id.pieChart)

        // Slušanje promjena
        camera2ViewModel.happyCount.observe(viewLifecycleOwner, { updatePieChart(pieChart) })
        camera2ViewModel.angryCount.observe(viewLifecycleOwner, { updatePieChart(pieChart) })
        camera2ViewModel.sadCount.observe(viewLifecycleOwner, { updatePieChart(pieChart) })
        camera2ViewModel.surpriseCount.observe(viewLifecycleOwner, { updatePieChart(pieChart) })

        return view
    }

    private fun updatePieChart(pieChart: PieChart) {
        val happyCount = camera2ViewModel.happyCount.value ?: 0
        val angryCount = camera2ViewModel.angryCount.value ?: 0
        val sadCount = camera2ViewModel.sadCount.value ?: 0
        val surpriseCount = camera2ViewModel.surpriseCount.value ?: 0

        Log.d("PieChartUpdate", "Happy: $happyCount, Angry: $angryCount, Sad: $sadCount, Surprise: $surpriseCount")

        val total = happyCount + angryCount + sadCount + surpriseCount

        if (total == 0) return

        val entries = arrayListOf<PieEntry>().apply {
            if (happyCount > 0) add(PieEntry(happyCount.toFloat(), "Happy"))
            if (angryCount > 0) add(PieEntry(angryCount.toFloat(), "Angry"))
            if (sadCount > 0) add(PieEntry(sadCount.toFloat(), "Sad"))
            if (surpriseCount > 0) add(PieEntry(surpriseCount.toFloat(), "Surprise"))
        }

        val pieDataSet = PieDataSet(entries, "Emotions").apply {
            colors = listOf(Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW) // Zelena, Crvena, Plava, Žuta
        }

        pieChart.data = PieData(pieDataSet)
        pieChart.invalidate()
        pieChart.animateXY(1000, 1000)
    }
}