package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.*
import com.example.data.remote.*
import com.example.data.repository.ProjectRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val database: AppDatabase
    val preferencesManager: PreferencesManager
    val apiService: ApiService
    val projectRepository: ProjectRepository
    val tokenProvider: TokenProvider
}

class AppContainerImpl(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "veo_doc_builder_db"
        )
        .addMigrations(MIGRATION_2_3)
        .fallbackToDestructiveMigration()
        .build()
    }

    override val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context.applicationContext)
    }

    override val tokenProvider: TokenProvider by lazy {
        TokenProvider()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    override val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://placeholder-url.api/") // Overridden dynamically by @Url in ApiService
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    override val projectRepository: ProjectRepository by lazy {
        ProjectRepository(
            projectDao = database.projectDao(),
            generatedContentDao = database.generatedContentDao(),
            customStyleDao = database.customStyleDao(),
            languageDao = database.languageDao(),
            apiService = apiService,
            preferencesManager = preferencesManager,
            tokenProvider = tokenProvider,
            pipelineDao = database.pipelineDao()
        )
    }
}
