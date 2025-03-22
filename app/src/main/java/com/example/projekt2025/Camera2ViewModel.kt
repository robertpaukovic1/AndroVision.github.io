package com.example.projekt2025

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class Camera2ViewModel : ViewModel() {
    private val _happyCount = MutableLiveData<Int>().apply { value = 0 }
    val happyCount: LiveData<Int> get() = _happyCount

    private val _angryCount = MutableLiveData<Int>().apply { value = 0 }
    val angryCount: LiveData<Int> get() = _angryCount

    private val _sadCount = MutableLiveData<Int>().apply { value = 0 }
    val sadCount: LiveData<Int> get() = _sadCount

    private val _surpriseCount = MutableLiveData<Int>().apply { value = 0 }
    val surpriseCount: LiveData<Int> get() = _surpriseCount

    // Ažuriranje broja emocije
    fun updateEmotionCount(emotionIndex: Float) {
        when (emotionIndex.toInt()) {
            0 -> _happyCount.value = (_happyCount.value ?: 0) + 1
            1 -> _angryCount.value = (_angryCount.value ?: 0) + 1
            2 -> _sadCount.value = (_sadCount.value ?: 0) + 1
            3 -> _surpriseCount.value = (_surpriseCount.value ?: 0) + 1
        }
    }

    // Promjena broja detektiranih emocija

    fun resetEmotionCount() {
        _happyCount.value = 0
        _angryCount.value = 0
        _sadCount.value = 0
        _surpriseCount.value = 0
    }
}