package com.senthil.AI.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

// Data classes matching schemas
data class UserLoginRequest(val email: String, val password: String)
data class TokenResponse(val access_token: String, val token_type: String, val role: String)

data class URLScanRequest(val url: String)
data class URLScanResponse(val url: String, val status: String, val score: Float, val details: List<String>)

data class SMSScanRequest(val content: String, val scan_type: String = "SMS")
data class SMSScanResponse(
    val scan_type: String,
    val original_text: String,
    val scam_probability: Float,
    val classification: String,
    val explanation: String
)

data class APKScanRequest(val app_name: String, val package_name: String, val permissions: List<String>)
data class APKScanResponse(
    val app_name: String,
    val package_name: String,
    val malware_score: Float,
    val threat_category: String,
    val flagged_permissions: List<String>,
    val status: String
)

interface SentinelApiService {
    @POST("api/auth/login")
    suspend fun login(@Body req: UserLoginRequest): TokenResponse

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
}
