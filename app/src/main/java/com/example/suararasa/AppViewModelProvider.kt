package com.example.suararasa

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.suararasa.data.AppDataContainer
import com.example.suararasa.item.MoodEditViewModel
import com.example.suararasa.item.MoodEntryViewModel
import com.example.suararasa.item.MoodDetailsViewModel
import com.example.suararasa.home.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for MoodEditViewModel
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as Application
            val container = AppDataContainer(application)
            MoodEditViewModel(
                this.createSavedStateHandle(),
                container.moodRepository
            )
        }

        // Initializer for MoodEntryViewModel
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as Application
            val container = AppDataContainer(application)
            MoodEntryViewModel(container.moodRepository)
        }

        // Initializer for MoodDetailsViewModel
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as Application
            val container = AppDataContainer(application)
            MoodDetailsViewModel(
                this.createSavedStateHandle(),
                container.moodRepository
            )
        }

        // Initializer for HomeViewModel
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as Application
            val container = AppDataContainer(application)
            HomeViewModel(container.moodRepository)
        }
    }
}
