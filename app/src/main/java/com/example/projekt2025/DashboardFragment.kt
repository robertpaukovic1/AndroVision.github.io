package com.example.projekt2025

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast


class DashboardFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myButton1: Button = view.findViewById(R.id.button4)
        val myButton2: Button= view.findViewById(R.id.button5)

        myButton1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BarChartFragment())
                .addToBackStack(null)
                .commit()
        }

        myButton2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PieChartFragment())
                .addToBackStack(null)
                .commit()
        }








    }

}