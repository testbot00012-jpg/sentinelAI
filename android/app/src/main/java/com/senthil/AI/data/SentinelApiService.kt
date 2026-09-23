package com.senthil.AI.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Header
import java.util.concurrent.TimeUnit

// ===== Live Backend URL (Supports local USB reverse forward 127.0.0.1:8000 and Railway cloud) =====
const val LOCAL_USB_URL = "http://127.0.0.1:8000/"
const val RAILWAY_CLOUD_URL = "https://sentinel-backend-production-16ff.up.railway.app/"
const val BASE_URL = LOCAL_USB_URL

// Singleton Retrofit client
object SentinelApiClient {
    private val endpointInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val originalUrl = original.url()

        // 1. Try local USB reverse-forwarded server (127.0.0.1:8000) first
        try {
            val localUrl = originalUrl.newBuilder()
                .scheme("http")
                .host("127.0.0.1")
                .port(8000)
                .build()
            val localRequest = original.newBuilder().url(localUrl).build()
            val response = chain.proceed(localRequest)
            if (response.isSuccessful || (response.code() != 404 && response.code() < 500)) {
                return@Interceptor response
            }
            response.close()
        } catch (e: Exception) {
            // Local 127.0.0.1:8000 not reachable (e.g. unplugged from USB), continue to Cloud
        }

        // 2. Fallback to Railway cloud server
        val cloudUrl = originalUrl.newBuilder()
            .scheme("https")
            .host("sentinel-backend-production-16ff.up.railway.app")
            .port(443)
            .build()
        val cloudRequest = original.newBuilder().url(cloudUrl).build()
        chain.proceed(cloudRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(endpointInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val instance: SentinelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_USB_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SentinelApiService::class.java)
    }
}

// Data classes matching FastAPI schemas
data class FirebaseTokenRequest(val id_token: String)
data class AuthResponse(val access_token: String, val token_type: String, val role: String, val email: String, val status: String)

// 1. URL Phishing (Screen 11)
data class URLScanRequest(val url: String)
data class URLScanResponse(val url: String, val status: String, val score: Float, val details: List<String>)

// 2. Scam Message / SMS (Screen 13)
data class SMSScanRequest(val content: String, val scan_type: String = "SMS")
data class SMSScanResponse(
    val scan_type: String,
    val original_text: String,
    val scam_probability: Float,
    val classification: String,
    val explanation: String,
    val contains_link: Boolean
)

// 3. APK Malware (Screen 07, 08)
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

// 4. Payment Screenshot Fraud (Screen 14)
data class PaymentScanRequest(val ocr_text: String, val image_base64: String? = null)
data class PaymentScanResponse(
    val extracted_amount: String,
    val extracted_date: String,
    val extracted_reference: String,
    val ecosystem: String,
    val fraud_score: Float,
    val classification: String,
    val confidence: Float,
    val evidence: List<String>,
    val recommended_action: String,
    val disclaimer: String
)

// 5. QR Code (Screen 12)
data class QRScanRequest(val payload: String)
data class QRScanResponse(
    val payload_type: String,
    val decoded_content: String,
    val target_destination: String,
    val risk_level: String,
    val confidence: Float,
    val threat_summary: String,
    val details: List<String>
)

// 6. Device Security (Screen 10)
data class DeviceScanRequest(
    val os_version: String = "14",
    val security_patch_level: String = "2024-01-01",
    val screen_lock_enabled: Boolean = true,
    val developer_options_enabled: Boolean = false,
    val usb_debugging_enabled: Boolean = false,
    val unknown_sources_allowed: Boolean = false,
    val device_admin_count: Int = 0,
    val root_detected: Boolean = false,
    val play_protect_enabled: Boolean = true,
    val encryption_enabled: Boolean = true
)
data class FindingItem(val severity: String, val title: String, val detail: String, val remedy: String)
data class DeviceScanResponse(
    val security_score: Int,
    val posture: String,
    val confidence: Float,
    val findings: List<FindingItem>,
    val recommendations_count: Int
)

// 7. Network Security (Screen 15)
data class NetworkScanRequest(
    val connection_type: String = "WIFI",
    val ssid: String = "Current Network",
    val encryption: String = "WPA2",
    val is_captive_portal: Boolean = false,
    val vpn_active: Boolean = false,
    val dns_servers: List<String> = listOf("8.8.8.8", "1.1.1.1")
)
data class NetworkScanResponse(
    val network_risk_score: Float,
    val status: String,
    val confidence: Float,
    val connection_type: String,
    val ssid: String,
    val encryption: String,
    val vpn_active: Boolean,
    val findings: List<FindingItem>,
    val recommendation: String
)

// 8. Quick Scan (Screen 05)
data class QuickScanRequest(
    val device_signals: Map<String, Any> = emptyMap(),
    val apps_sample: List<Map<String, Any>> = emptyList()
)
data class QuickScanResponse(
    val scan_type: String,
    val overall_score: Int,
    val status: String,
    val device_posture: String,
    val device_findings: List<FindingItem>,
    val apps_scanned_count: Int,
    val threat_apps_count: Int,
    val total_findings_count: Int,
    val timestamp: String
)

// 9. Full Scan (Screen 06)
data class FullScanRequest(
    val device_signals: Map<String, Any> = emptyMap(),
    val installed_apps: List<Map<String, Any>> = emptyList(),
    val network_info: Map<String, Any> = emptyMap()
)
data class FullScanResponse(
    val scan_type: String,
    val composite_score: Int,
    val posture: String,
    val total_apps_scanned: Int,
    val flagged_apps_count: Int,
    val recommendations: List<String>,
    val timestamp: String
)

// 10. AI Security Assistant - Local In-Process LLM (Screen 16, 17)
data class ChatMessage(val role: String, val content: String)
data class ChatRequest(val message: String, val history: List<ChatMessage> = emptyList(), val scan_context: Map<String, Any>? = null)
data class ChatResponse(val reply: String, val model: String, val confidence: Float, val evidence: List<String>, val timestamp: String)

// 11. Model & AI Status (Screen 26)
data class ModelItem(
    val name: String,
    val type: String,
    val version: String,
    val status: String,
    val accuracy: String,
    val features_count: Any,
    val offline_ready: Boolean
)
data class ModelStatusResponse(
    val engine: String,
    val version: String,
    val inference_mode: String,
    val cloud_dependencies: String,
    val models: List<ModelItem>,
    val last_updated: String,
    val model_integrity: String
)

// 12. Incidents & Alerts (Screen 18, 19, 20)
data class TimelineEvent(val timestamp: String, val source: String, val event: String, val severity: String)
data class IncidentItem(
    val incident_id: String,
    val title: String,
    val severity: String,
    val confidence: Float,
    val affected_asset: String,
    val status: String,
    val timeline: List<TimelineEvent>,
    val evidence: List<String>,
    val recommended_actions: List<String>
)
data class IncidentsListResponse(val incidents: List<IncidentItem>, val total: Int)

data class AlertItem(
    val alert_id: String,
    val severity: String,
    val title: String,
    val summary: String,
    val action_required: String,
    val read: Boolean,
    val timestamp: String
)
data class AlertsListResponse(val alerts: List<AlertItem>, val total: Int)

// 13. Emergency Protocol (Screen 22)
data class ChecklistStep(val step_number: Int, val title: String, val description: String, val criticality: String)
data class EmergencyProtocolResponse(val mode: String, val checklist: List<ChecklistStep>, val disclaimer: String)

// 14. Executive Report (Screen 23)
data class SecurityReportResponse(
    val report_id: String,
    val generated_at: String,
    val security_score: Int,
    val key_threats_blocked: List<String>,
    val recommendations: List<String>,
    val compliance: String
)

// 15. Enterprise (Screen 28)
data class EnterpriseStatusResponse(
    val enrolled: Boolean,
    val organization_name: String,
    val organization_id: String,
    val device_id: String,
    val compliance_status: String,
    val managed_features: List<String>
)

// Telemetry & Metrics
data class MetricsSummary(val security_score: Int, val threats_blocked: Int, val total_scans: Int)
data class MetricsResponse(val summary: MetricsSummary)
data class DeviceTelemetryRequest(
    val device_model: String,
    val os_version: String,
    val security_score: Int,
    val battery_health: Int,
    val ram_usage_percent: Double,
    val storage_usage_percent: Double
)
data class TelemetryResponse(val message: String)

// 16. Security Scan History (Unified across Mobile & Web)
data class SecurityHistoryItemDto(
    val id: String = "",
    val scan_type: String = "Security Scan",
    val target: String = "",
    val verdict: String = "Safe",
    val score: Int = 100,
    val severity: String = "Safe",
    val timestamp: String = "",
    val created_at: String = ""
)

data class SecurityHistoryListResponse(
    val history: List<SecurityHistoryItemDto> = emptyList(),
    val total: Int = 0
)

data class RecordHistoryRequest(
    val scan_type: String,
    val target: String,
    val verdict: String,
    val score: Int = 100,
    val severity: String = "Safe",
    val timestamp: String? = null
)

data class DeleteHistoryResponse(
    val message: String = "",
    val id: String? = null,
    val deleted_count: Int = 0,
    val deleted: Boolean = true
)

interface SentinelApiService {
    @POST("api/auth/verify")
    suspend fun verifyFirebaseToken(@Body req: FirebaseTokenRequest): AuthResponse

    @POST("api/analytics/telemetry")
    suspend fun sendTelemetry(
        @Header("Authorization") token: String,
        @Body req: DeviceTelemetryRequest
    ): TelemetryResponse

    @GET("api/analytics/metrics")
    suspend fun getMetrics(
        @Header("Authorization") token: String? = null,
        @Header("X-User-Email") email: String? = null
    ): MetricsResponse

    // Scanners
    @POST("api/scan/url")
    suspend fun scanUrl(
        @Header("Authorization") token: String? = null,
        @Body req: URLScanRequest,
        @Header("X-User-Email") email: String? = null
    ): URLScanResponse

    @POST("api/scan/fraud")
    suspend fun scanSMS(
        @Header("Authorization") token: String? = null,
        @Body req: SMSScanRequest,
        @Header("X-User-Email") email: String? = null
    ): SMSScanResponse

    @POST("api/scan/apk")
    suspend fun scanAPK(
        @Header("Authorization") token: String? = null,
        @Body req: APKScanRequest,
        @Header("X-User-Email") email: String? = null
    ): APKScanResponse

    @POST("api/scan/payment")
    suspend fun scanPaymentScreenshot(
        @Header("Authorization") token: String? = null,
        @Body req: PaymentScanRequest,
        @Header("X-User-Email") email: String? = null
    ): PaymentScanResponse

    @POST("api/scan/qr")
    suspend fun scanQRCode(
        @Header("Authorization") token: String? = null,
        @Body req: QRScanRequest,
        @Header("X-User-Email") email: String? = null
    ): QRScanResponse

    @POST("api/scan/device")
    suspend fun scanDevice(
        @Header("Authorization") token: String? = null,
        @Body req: DeviceScanRequest,
        @Header("X-User-Email") email: String? = null
    ): DeviceScanResponse

    @POST("api/scan/network")
    suspend fun scanNetwork(
        @Header("Authorization") token: String? = null,
        @Body req: NetworkScanRequest,
        @Header("X-User-Email") email: String? = null
    ): NetworkScanResponse

    @POST("api/scan/quick")
    suspend fun runQuickScan(
        @Header("Authorization") token: String? = null,
        @Body req: QuickScanRequest,
        @Header("X-User-Email") email: String? = null
    ): QuickScanResponse

    @POST("api/scan/full")
    suspend fun runFullScan(
        @Header("Authorization") token: String? = null,
        @Body req: FullScanRequest,
        @Header("X-User-Email") email: String? = null
    ): FullScanResponse

    @GET("api/scan/models/status")
    suspend fun getModelsStatus(): ModelStatusResponse

    // Local Neural LLM Chat
    @POST("api/chat")
    suspend fun sendChatMessage(@Header("Authorization") token: String, @Body req: ChatRequest): ChatResponse

    // Incidents, Alerts & Operations
    @GET("api/incidents")
    suspend fun getIncidents(): IncidentsListResponse

    @GET("api/incidents/alerts/inbox")
    suspend fun getAlerts(): AlertsListResponse

    @GET("api/incidents/emergency/protocol")
    suspend fun getEmergencyProtocol(): EmergencyProtocolResponse

    @GET("api/incidents/reports/latest")
    suspend fun getSecurityReport(): SecurityReportResponse

    @GET("api/incidents/enterprise/status")
    suspend fun getEnterpriseStatus(): EnterpriseStatusResponse

    // Unified Scan History & Deletions
    @GET("api/scan/history")
    suspend fun getScanHistory(
        @Header("Authorization") token: String? = null,
        @Header("X-User-Email") email: String? = null
    ): SecurityHistoryListResponse

    @POST("api/scan/history")
    suspend fun recordScanHistory(
        @Header("Authorization") token: String? = null,
        @Header("X-User-Email") email: String? = null,
        @Body req: RecordHistoryRequest
    ): SecurityHistoryItemDto

    @DELETE("api/scan/history/{item_id}")
    suspend fun deleteScanHistoryItem(
        @Path("item_id") itemId: String,
        @Header("Authorization") token: String? = null,
        @Header("X-User-Email") email: String? = null
    ): DeleteHistoryResponse

    @DELETE("api/scan/history")
    suspend fun clearScanHistory(
        @Header("Authorization") token: String? = null,
        @Header("X-User-Email") email: String? = null
    ): DeleteHistoryResponse
}
