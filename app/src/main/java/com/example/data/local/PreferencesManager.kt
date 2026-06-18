package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "veo_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
        val DEFAULT_STYLE = stringPreferencesKey("default_style")
        val USE_MODEL_PRO = booleanPreferencesKey("use_model_pro")
    }

    val backendUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKEND_URL] ?: "https://ais-dev-6vamkjatr5nzuixwjdntbd-1026175467955.asia-southeast1.run.app"
    }

    val defaultLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_LANGUAGE] ?: "English"
    }

    val defaultStyleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_STYLE] ?: "Hyper-realistic 3D"
    }

    val useModelProFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_MODEL_PRO] ?: true
    }

    suspend fun saveBackendUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKEND_URL] = url
        }
    }

    suspend fun saveDefaultLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_LANGUAGE] = language
        }
    }

    suspend fun saveDefaultStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_STYLE] = style
        }
    }

    suspend fun saveUseModelPro(usePro: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_MODEL_PRO] = usePro
        }
    }
}
