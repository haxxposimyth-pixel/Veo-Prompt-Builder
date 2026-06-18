package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

// ==========================================
// MODELS
// ==========================================

data class GenerateRequest(
    val mode: String,                // "analysis", "phase", "titles"
    val niche: String,               // e.g. "Health & Biology"
    val customNiche: String?,        // multiline description if "Custom"
    val videoStyle: String,          // style name
    val customStyleDescription: String?, // custom description if not built-in
    val topic: String,               // topic description
    val aspectRatio: String,         // "16:9" or "9:16"
    val language: String,            // language name
    val phase: Int?,                 // 1 to 10 or null
    val model: String,               // "pro" or "flash"
    val bible: String? = null,
    val blueprint: String? = null
)

data class GenerateResponse(
    val text: String
)

// ==========================================
// API INTERFACE
// ==========================================

interface ApiService {
    @GET
    suspend fun health(@Url url: String): retrofit2.Response<Unit>

    @POST
    suspend fun generate(
        @Url url: String,
        @Body request: GenerateRequest,
        @Header("X-Firebase-AppCheck") appCheckToken: String,
        @Header("Authorization") bearerToken: String
    ): GenerateResponse
}

// ==========================================
// STUBBED TOKEN PROVIDER
// ==========================================

class TokenProvider {
    /**
     * Stubbed Firebase App Check token. In production, this would use:
     * FirebaseAppCheck.getInstance().getAppCheckToken(false)
     */
    fun getAppCheckToken(): String {
        return "xml-firebase-appcheck-prod-stub-7182910"
    }

    /**
     * Stubbed Firebase Auth Bearer token. In production, this would fetch from:
     * FirebaseAuth.getInstance().currentUser?.getIdToken(false)
     */
    fun getBearerToken(): String {
        return "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6InN0dWItZmlyZWJhc2UtYXV0aC10b2tlbi0xMjM0NTY3ODkwIn0.eyJzdWIiOiJzdHViX3VzZXIsImVtYWlsIijoYXJkaWtzYXJpeWFAZ21haWwuY29tIn0"
    }
}
