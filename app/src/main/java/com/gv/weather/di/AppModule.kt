package com.gv.weather.di

import android.app.Application
import androidx.room.Room
import com.gv.weather.data.RepositoryImpl
import com.gv.weather.data.database.MainDao
import com.gv.weather.data.database.WeatherDatabase
import com.gv.weather.domain.Interactor
import com.gv.weather.domain.InteractorImpl
import com.gv.weather.domain.Repository
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dbModule = module {
    fun Application.provideDatabase() =
        Room.databaseBuilder(this, WeatherDatabase::class.java, "Weather").build()
    single { androidApplication().provideDatabase() }
}

val daoModule = module {
    fun provideMainDao(database: WeatherDatabase) = database.mainDao
    single { provideMainDao(get()) }
}

val repositoriesModule = module {
    fun provideRepository(mainDao: MainDao): Repository = RepositoryImpl(mainDao)
    single { provideRepository(get()) }
}

val interactorsModule = module {
    fun provideInteractor(repository: Repository): Interactor = InteractorImpl(repository)
    single { provideInteractor(get()) }
}

val ktorModule = module {
    single {
        HttpClient(Android) {
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            HttpResponseValidator {
                handleResponseException {
                    println("handleRE = $it")
                }
                validateResponse {
                    println("validate response = $it")
                }
            }
        }
    }
}

