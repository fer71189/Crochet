package com.nexdev.crochet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nexdev.crochet.data.repository.CrochetRepository

class CrochetViewModelFactory(
    private val repository: CrochetRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CatalogViewModel::class.java) -> {
                CatalogViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TrackerViewModel::class.java) -> {
                TrackerViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
