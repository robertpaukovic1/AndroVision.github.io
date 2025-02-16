package com.example.projekt2025

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Camera1ViewModel: ViewModel() {

    private val  _objCounts= MutableLiveData<Map<String, Int>>()
    val objCounts: LiveData<Map<String, Int>> get() = _objCounts


    fun updateObjectCounts(counts: Map<String, Int>) {
        _objCounts.postValue(counts)
    }

}

