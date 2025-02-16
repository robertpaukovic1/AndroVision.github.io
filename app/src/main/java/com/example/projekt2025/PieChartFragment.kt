package com.example.projekt2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class PieChartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pie_chart, container, false)

        val pieChart: PieChart = view.findViewById(R.id.pieChart)

        val entries = arrayListOf(
            PieEntry(80f, "Happy"),
            PieEntry(90f, "Angry"),
            PieEntry(75f, "Sad"),
            PieEntry(60f, "In love")
        )

        val pieDataSet = PieDataSet(entries, "Emotions").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.animateX(1000)
        pieChart.invalidate()

        return view
    }
}
