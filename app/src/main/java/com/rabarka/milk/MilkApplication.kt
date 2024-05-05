package com.rabarka.milk

import android.app.Application
import com.rabarka.milk.data.AppContainer
import com.rabarka.milk.data.AppDataContainer

class MilkApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}