package com.senthil.AI.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header

// ===== Live Backend URL (Render deployed) =====
const val BASE_URL = "https://sentinelai-6kf0.onrender.com/"

// Singleton Retrofit client
object SentinelApiClient {
    val instance: SentinelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SentinelApiService::class.java)
    }
}

// Data classes matching FastAPI schemas
data class FirebaseTokenRequest(val id_token: String)
data class AuthResponse(val access_token: String, val token_type: String, val role: String, val email: String, val status: String)

data class URLScanRequest(val url: String)
data class URLScanResponse(val url: String, val status: String, val score: Float, val details: List<String>)

data class SMSScanRequest(val content: String, val scan_type: String = "SMS")
data class SMSScanResponse(
    val scan_type: String,
    val original_text: String,
    val scam_probability: Float,
    val classification: String,
    val explanation: String,
    val contains_link: Boolean
)

data class APKScanRequest(val app_name: String, val package_name: String, val permissions: List<String>)
data class APKScanResponse(
    val app_name: String,
    val package_name: String,
    val malware_score: Float,
    val threat_category: String,
    val flagged_permissions: List<String>,
    val total_permissions_scanned: Int,
    val status: String
)

data class MetricsSummary(val security_score: Int, val threats_blocked: Int, val total_scans: Int)
data class MetricsResponse(val summary: MetricsSummary)

interface SentinelApiService {
    @POST("api/auth/verify")
    suspend fun verifyFirebaseToken(@Body req: FirebaseTokenRequest): AuthResponse

    @POST("api/scan/url")
    suspend fun scanUrl(
        @Header("Authorization") token: String,
        @Body req: URLScanRequest
    ): URLScanResponse

    @POST("api/scan/fraud")
    suspend fun scanSMS(
        @Header("Authorization") token: String,
        @Body req: SMSScanRequest
    ): SMSScanResponse

    @POST("api/scan/apk")
    suspend fun scanAPK(
        @Header("Authorization") token: String,
        @Body req: APKScanRequest
    ): APKScanResponse

    @GET("api/analytics/metrics")
    suspend fun getMetrics(
        @Header("Authorization") token: String
    ): MetricsResponse
}
