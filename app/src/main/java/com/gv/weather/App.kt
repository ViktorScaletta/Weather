package com.gv.weather

import android.app.Application
import com.gv.weather.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(dbModule, daoModule, repositoriesModule, interactorsModule, ktorModule)
        }
    }

}