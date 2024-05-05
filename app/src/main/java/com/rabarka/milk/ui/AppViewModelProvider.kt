package com.rabarka.milk.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rabarka.milk.MilkApplication
import com.rabarka.milk.ui.home.HomeScreenViewModel
import com.rabarka.milk.ui.milk.MilkDetailsViewModel
import com.rabarka.milk.ui.milk.MilkEditViewModel
import com.rabarka.milk.ui.milk.MilkEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            MilkEditViewModel(
                this.createSavedStateHandle(),
                milkApplication().container.milkRepository
            )
        }

        initializer {
            MilkEntryViewModel(milkApplication().container.milkRepository)
        }
        initializer {
            MilkDetailsViewModel(
                this.createSavedStateHandle(),
                milkApplication().container.milkRepository
            )
        }

        initializer {
            HomeScreenViewModel(milkApplication().container.milkRepository)
        }
    }
}

fun CreationExtras.milkApplication(): MilkApplication {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MilkApplication)
}