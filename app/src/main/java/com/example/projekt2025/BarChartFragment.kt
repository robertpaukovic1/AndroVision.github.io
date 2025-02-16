package com.example.projekt2025

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class BarChartFragment : Fragment() {

    private val xValues: List<String> = listOf("person", "car", "bicycle", "traffic light")
    private lateinit var camera1ViewModel: Camera1ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bar_chart, container, false)

        camera1ViewModel = ViewModelProvider(requireActivity()).get(Camera1ViewModel::class.java)

        val barChart = view.findViewById<BarChart>(R.id.barChart)
        setupBarChart(barChart)

        camera1ViewModel.objCounts.observe(viewLifecycleOwner, { detectedObjects ->
            updateBarChart(detectedObjects, barChart)
        })

        return view
    }

    private fun setupBarChart(barChart: BarChart) {
        // Set up the BarChart properties
        barChart.description.isEnabled = false
        barChart.axisRight.setDrawLabels(false)

        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 30f
        yAxis.axisLineWidth = 2f
        yAxis.axisLineColor = Color.BLACK
        yAxis.labelCount = 10

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xValues)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
        }

        barChart.invalidate()
    }

    private fun updateBarChart(detectedObjects: Map<String, Int>, barChart: BarChart) {
        val entries = mutableListOf<BarEntry>()


        xValues.forEachIndexed { index, label ->
            val count = detectedObjects[label] ?: 0
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
        }


        val dataSet = BarDataSet(entries, "Objects Detected")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()
    }
}
