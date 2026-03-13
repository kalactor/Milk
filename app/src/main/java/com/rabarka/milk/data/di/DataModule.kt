package com.rabarka.milk.data.di

import android.content.Context
import com.rabarka.milk.data.MilkDao
import com.rabarka.milk.data.MilkDatabase
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.data.OfflineMilkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideMilkDatabase(
        @ApplicationContext context: Context
    ): MilkDatabase = MilkDatabase.getDatabase(context)

    @Provides
    fun provideMilkDao(database: MilkDatabase): MilkDao = database.milkDao()

    @Provides
    @Singleton
    fun provideMilkRepository(milkDao: MilkDao): MilkRepository = OfflineMilkRepository(milkDao)
}
