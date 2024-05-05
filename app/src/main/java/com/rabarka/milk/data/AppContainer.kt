package com.rabarka.milk.data

import android.content.Context

interface AppContainer {
    val milkRepository: MilkRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val milkRepository: MilkRepository by lazy {
        OfflineMilkRepository(MilkDatabase.getDatabase(context).milkDao())
    }
}