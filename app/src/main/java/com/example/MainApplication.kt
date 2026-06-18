package com.example

import android.app.Application
import com.example.data.di.AppContainer
import com.example.data.di.AppContainerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Instantiate the local dependency graph
        container = AppContainerImpl(this)

        // Prepopulate the local Room database with seed data if cold-booting
        CoroutineScope(Dispatchers.IO).launch {
            container.projectRepository.seedDatabaseIfEmpty()
        }
    }
}
