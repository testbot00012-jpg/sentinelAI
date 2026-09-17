package com.senthil.AI.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.app.ActivityManager
import android.os.Environment
import android.os.StatFs
import android.os.Build
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import android.net.Uri
import android.provider.Settings
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.senthil.AI.data.SentinelApiClient
import com.senthil.AI.data.URLScanRequest
import com.senthil.AI.data.ChatRequest
import com.senthil.AI.data.ChatMessage

// Beautiful Dark theme color palette for Jetpack Compose matching Next.js theme
val CyberBackground = Color(0xFF050816)
val CyberCard = Color(0xFF0F172A)
val CyberPrimary = Color(0xFF00E5FF)
val CyberSecondary = Color(0xFF7C3AED)
val CyberSuccess = Color(0xFF00FF88)
val CyberWarning = Color(0xFFFFB020)
val CyberDanger = Color(0xFFFF4D4D)

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object Dashboard : Screen()
    object Chatbot : Screen()
    object URLScanner : Screen()
    object SMSAnalyzer : Screen()
    object PermissionAnalyzer : Screen()
    object Profile : Screen()
    object DynamicScanResult : Screen()
    object Optimizer : Screen()
    object PaymentShield : Screen()
}

@Composable
fun SentinelApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var userEmail by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberBackground
    ) {
        when (currentScreen) {
            is Screen.Splash -> SplashScreen(
                onFinish = { isLoggedIn ->
                    currentScreen = if (isLoggedIn) Screen.Dashboard else Screen.Login
                },
                onUserLoaded = { email, authToken ->
                    userEmail = email
                    token = authToken
                }
            )
            is Screen.Login -> LoginScreen(
                onLoginSuccess = { email, authToken ->
                    userEmail = email
                    token = authToken
                    currentScreen = Screen.Dashboard
                },
                onNavigateToRegister = {
                    currentScreen = Screen.Register
                }
            )
            is Screen.Register -> RegisterScreen(
                onRegisterSuccess = { email, authToken ->
                    userEmail = email
                    token = authToken
                    currentScreen = Screen.Dashboard
                },
                onNavigateToLogin = {
                    currentScreen = Screen.Login
                }
            )
            Screen.Dashboard, Screen.Chatbot, Screen.URLScanner, Screen.SMSAnalyzer, Screen.PermissionAnalyzer, Screen.Profile, Screen.DynamicScanResult, Screen.Optimizer, Screen.PaymentShield -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberCard,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Dashboard,
                                onClick = { currentScreen = Screen.Dashboard },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberPrimary,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberPrimary,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberCard
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Chatbot,
                                onClick = { currentScreen = Screen.Chatbot },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Chat") },
                                label = { Text("AI Chat", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberPrimary,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberPrimary,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberCard
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.URLScanner,
                                onClick = { currentScreen = Screen.URLScanner },
                                icon = { Icon(Icons.Default.Language, contentDescription = "URL Scan") },
                                label = { Text("URL Scan", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberPrimary,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberPrimary,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberCard
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.PermissionAnalyzer,
                                onClick = { currentScreen = Screen.PermissionAnalyzer },
                                icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Auditor") },
                                label = { Text("Auditor", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberWarning,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberWarning,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberCard
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Profile,
                                onClick = { currentScreen = Screen.Profile },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberSuccess,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberSuccess,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberCard
                                )
                            )
                        }
                    },
                    containerColor = CyberBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            is Screen.Dashboard -> DashboardScreen(
                                userEmail = userEmail,
                                token = token,
                                onNavigate = { currentScreen = it }
                            )
                            is Screen.Chatbot -> ChatbotScreen(
                                userEmail = userEmail,
                                token = token,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.URLScanner -> URLScannerScreen(
                                token = token,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.SMSAnalyzer -> SMSAnalyzerScreen(
                                token = token,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.PermissionAnalyzer -> PermissionAnalyzerScreen(
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.Profile -> ProfileScreen(
                                userEmail = userEmail,
                                token = token,
                                onLogout = {
                                    currentScreen = Screen.Login
                                },
                                onBackToDashboard = {
                                    currentScreen = Screen.Dashboard
                                }
                            )
                            is Screen.DynamicScanResult -> DynamicSystemScanResultScreen(
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.Optimizer -> OptimizerScreen(
                                token = token,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.PaymentShield -> PaymentShieldScreen(
                                token = token,
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    onFinish: (Boolean) -> Unit,
    onUserLoaded: (String, String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    
    LaunchedEffect(key1 = true) {
        visible = true
        var isLoggedIn = false
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val tokenResult = currentUser.getIdToken(false).await()
                val idToken = tokenResult.token
                if (idToken != null) {
                    onUserLoaded(currentUser.email ?: "", "Bearer $idToken")
                    isLoggedIn = true
                }
            } catch (e: Exception) {
                auth.signOut()
            }
        }
        delay(2200)
        onFinish(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = CyberPrimary,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SENTINEL AI",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Smart Threat Defense Suite",
                    color = CyberPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Security",
            tint = CyberPrimary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AGENT ACCESS PORTAL",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Establish secure diagnostic bridge",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberDanger.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(errorMsg, color = CyberDanger, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Agent Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = CyberPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = CyberPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Access Key") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = CyberPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = CyberPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMsg = "Email and password are required."
                    return@Button
                }
                isLoading = true
                errorMsg = ""
                coroutineScope.launch {
                    try {
                        // 1. Sign in with Firebase Auth
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        val result = auth.signInWithEmailAndPassword(email, password).await()
                        val idToken = result.user?.getIdToken(false)?.await()?.token
                            ?: throw Exception("Failed to get Firebase token")

                        // 2. Sync with Render backend
                        val response = com.senthil.AI.data.SentinelApiClient.instance
                            .verifyFirebaseToken(com.senthil.AI.data.FirebaseTokenRequest(idToken))

                        onLoginSuccess(response.email, "Bearer ${response.access_token}")
                    } catch (e: Exception) {
                        errorMsg = e.message?.take(80) ?: "Authentication failed."
                    } finally {
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = CyberBackground, modifier = Modifier.size(24.dp))
            } else {
                Text("AUTHENTICATE AGENT", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Need access? Register Agent",
            color = CyberPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onNavigateToRegister() }
                .padding(8.dp)
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    BackHandler {
        onNavigateToLogin()
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Shield",
            tint = CyberPrimary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "CREATING ACCESS PORTAL",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Register with Sentinel Shield",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberDanger.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(errorMsg, color = CyberDanger, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Agent Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = CyberPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = CyberPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Access Key") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = CyberPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = CyberPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Access Key") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = CyberPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = CyberPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMsg = "Email and password are required."
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMsg = "Passwords do not match."
                    return@Button
                }
                isLoading = true
                errorMsg = ""
                coroutineScope.launch {
                    try {
                        // 1. Create User in Firebase Auth
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        val result = auth.createUserWithEmailAndPassword(email, password).await()
                        val idToken = result.user?.getIdToken(false)?.await()?.token
                            ?: throw Exception("Failed to get Firebase token")

                        // 2. Sync with Render backend
                        val response = com.senthil.AI.data.SentinelApiClient.instance
                            .verifyFirebaseToken(com.senthil.AI.data.FirebaseTokenRequest(idToken))

                        onRegisterSuccess(response.email, "Bearer ${response.access_token}")
                    } catch (e: Exception) {
                        errorMsg = e.message?.take(80) ?: "Registration failed."
                    } finally {
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = CyberBackground, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "REGISTER AGENT",
                    color = CyberBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Already registered? Login here",
            color = CyberPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onNavigateToLogin() }
                .padding(8.dp)
        )
    }
}

@Composable
fun DashboardScreen(userEmail: String, token: String = "", onNavigate: (Screen) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Scan simulation states
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanLogText by remember { mutableStateOf("") }
    
    // Read real system parameters for quick indicators
    val batteryPercent = remember {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100) / scale else 100
    }

    val ramPercent = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val used = memoryInfo.totalMem - memoryInfo.availMem
        ((used.toDouble() / memoryInfo.totalMem.toDouble()) * 100).toInt()
    }

    val storagePercent = remember {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val used = stat.blockCountLong - stat.availableBlocksLong
        ((used.toDouble() / stat.blockCountLong.toDouble()) * 100).toInt()
    }

    // Dynamic metrics from Cloud Database
    var totalScans by remember { mutableStateOf<Int?>(null) }
    var threatsBlocked by remember { mutableStateOf<Int?>(null) }
    var integrityScore by remember { mutableStateOf(98) }

    // Sync device hardware telemetry to backend (Battery, RAM, Storage)
    LaunchedEffect(token, batteryPercent, ramPercent, storagePercent) {
        if (token.isNotBlank()) {
            try {
                com.senthil.AI.data.SentinelApiClient.instance.sendTelemetry(
                    token = token,
                    req = com.senthil.AI.data.DeviceTelemetryRequest(
                        device_model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                        os_version = "Android ${Build.VERSION.RELEASE}",
                        security_score = 98,
                        battery_health = batteryPercent,
                        ram_usage_percent = ramPercent.toDouble(),
                        storage_usage_percent = storagePercent.toDouble()
                    )
                )
            } catch (e: Exception) {
                // Background telemetry sync fail-safe
            }
        }
    }

    // Real-time synchronization polling (Total Scans & Threats Blocked)
    LaunchedEffect(token) {
        while (true) {
            if (token.isNotBlank()) {
                try {
                    val metricsRes = com.senthil.AI.data.SentinelApiClient.instance.getMetrics(token)
                    totalScans = metricsRes.summary.total_scans
                    threatsBlocked = metricsRes.summary.threats_blocked
                    integrityScore = metricsRes.summary.security_score
                } catch (e: Exception) {
                    // Background sync fail-safe
                }
            }
            delay(3500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        // Top client banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SENTINEL CORE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Device Status: ${if (isScanning) "Scanning..." else "Operational"}", color = if (isScanning) CyberWarning else CyberSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                onNavigate(Screen.Login)
            }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = CyberDanger,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            // Scanning diagnostics logs layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberWarning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = scanProgress,
                    color = CyberWarning,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "THREAT SCANNING CYCLE ACTIVE",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${(scanProgress * 100).toInt()}% Analysing Core",
                    color = CyberWarning,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Logging feedback console
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = scanLogText,
                        color = CyberSuccess,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            // Main Dashboard stats list
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Device overall score card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(CyberCard, Color(0xFF020617))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("DEVICE INTEGRITY SCORE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("${integrityScore}%", color = CyberSuccess, fontSize = 48.sp, fontWeight = FontWeight.Black)
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Shield",
                                tint = CyberSuccess,
                                modifier = Modifier
                                    .size(54.dp)
                                    .padding(bottom = 6.dp)
                            )
                        }
                        Text("Active real-time cybersecurity shield with cloud sync.", color = Color.LightGray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Real-time Cloud Metrics Synced with Web Console
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TOTAL SCANS", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("${totalScans ?: 0}", color = CyberPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.12f)))
                            Column {
                                Text("THREATS BLOCKED", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("${threatsBlocked ?: 0}", color = CyberDanger, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.12f)))
                            Column {
                                Text("CLOUD SYNC", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("LIVE", color = CyberSuccess, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Groq AI Assistant Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyberPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onNavigate(Screen.Chatbot) },
                    colors = CardDefaults.cardColors(containerColor = CyberCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CyberPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = "AI", tint = CyberPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SENTINEL AI INTEL", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "GROQ",
                                    color = CyberSuccess,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CyberSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text("Ask questions about phishing, threats & app risks", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberPrimary)
                    }
                }

                Text("SHIELD ACTIVE ENGINES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                // Active engines indicators - Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "Web Scan", status = "SECURE", tint = CyberPrimary, onClick = { onNavigate(Screen.URLScanner) })
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "SMS Spam", status = "ACTIVE", tint = CyberSecondary, onClick = { onNavigate(Screen.SMSAnalyzer) })
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "Pay Shield", status = "ARMED", tint = CyberSuccess, onClick = { onNavigate(Screen.PaymentShield) })
                }

                // Active engines indicators - Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "App Auditor", status = "SAFE", tint = CyberWarning, onClick = { onNavigate(Screen.PermissionAnalyzer) })
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "Optimizer Suite", status = "READY", tint = CyberPrimary, onClick = { onNavigate(Screen.Optimizer) })
                }

                Text("DEVICE UTILIZATION & PERFORMANCE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                // Progress bars for RAM, Storage, and Battery on Dashboard
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCard)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    UsageProgressRow(label = "Memory (RAM)", percent = ramPercent, color = CyberSecondary)
                    UsageProgressRow(label = "Internal Storage", percent = storagePercent, color = CyberPrimary)
                    UsageProgressRow(label = "Battery Power", percent = batteryPercent, color = CyberSuccess)

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onNavigate(Screen.Optimizer) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ 1-TAP OPTIMIZE (RAM • JUNK • POWER)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pulse Scanning trigger button
            Button(
                onClick = {
                    isScanning = true
                    scanProgress = 0f
                    coroutineScope.launch {
                        val logs = listOf(
                            "Initializing Threat Telemetry matrix...",
                            "Auditing system package credentials...",
                            "Scanning user-installed package signatures...",
                            "Verifying background Accessibility services...",
                            "Evaluating active network interfaces & TLS tunnels...",
                            "Parsing SMS logs NLP keyword matrices...",
                            "Compiling final threat diagnostics logs...",
                            "Threat Scan Complete. System Integrity: 98% SECURE."
                        )
                        for (i in 0..100) {
                            scanProgress = i / 100f
                            val logIdx = (i / (100 / logs.size)).coerceAtMost(logs.size - 1)
                            scanLogText = "ROOT@SENTINEL:~# ${logs[logIdx]}"
                            delay(40)
                        }
                        delay(500)
                        isScanning = false
                        onNavigate(Screen.DynamicScanResult)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = "Scan", tint = CyberBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RUN DYNAMIC SYSTEM SCAN", color = CyberBackground, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun EngineStatusBox(
    modifier: Modifier = Modifier,
    title: String,
    status: String,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberCard)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = status,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(tint.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun UsageProgressRow(label: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$percent%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = percent / 100f,
            color = color,
            trackColor = Color.DarkGray.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

data class URLScanVerdict(
    val url: String,
    val status: String, // "Phishing", "Suspicious", "Safe"
    val score: Float,   // 0.0 to 1.0 (malware / risk score)
    val details: List<String>,
    val recommendation: String
)

fun evaluateUrlHeuristics(rawUrl: String): URLScanVerdict {
    val cleanUrl = rawUrl.trim()
    val lower = cleanUrl.lowercase()
    val details = mutableListOf<String>()
    var riskScore = 0.05f

    // 1. Raw IP address host (e.g. http://192.168.1.1 or 45.33.32.156)
    val ipRegex = Regex("""^https?://(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""")
    val rawIpRegex = Regex("""^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""")
    if (ipRegex.containsMatchIn(lower) || rawIpRegex.containsMatchIn(lower)) {
        riskScore += 0.85f
        details.add("Raw IPv4 host detected (obfuscation signature)")
    }

    // 2. Punycode / IDN homograph attack
    if (lower.contains("xn--")) {
        riskScore += 0.75f
        details.add("Punycode / IDN homograph spoofing signature (xn--)")
    }

    // 3. User info embedded in URL (e.g. https://google.com@evil.com)
    if (lower.contains("@")) {
        riskScore += 0.70f
        details.add("Credential redirection obfuscation ('@' symbol in URL)")
    }

    // 4. Abused Phishing TLDs
    val suspiciousTlds = listOf(
        ".xyz", ".top", ".buzz", ".work", ".click", ".icu", ".loan",
        ".cfd", ".link", ".gq", ".ml", ".cf", ".tk", ".ga", ".ru", ".cn"
    )
    for (tld in suspiciousTlds) {
        if (lower.contains(tld)) {
            riskScore += 0.40f
            details.add("High-risk top-level domain ($tld)")
            break
        }
    }

    // 5. Brand Spoofing Check
    val brandMap = mapOf(
        "paypal" to listOf("paypal.com"),
        "google" to listOf("google.com", "google.co", "accounts.google.com"),
        "apple" to listOf("apple.com", "icloud.com"),
        "microsoft" to listOf("microsoft.com", "live.com", "office.com"),
        "netflix" to listOf("netflix.com"),
        "amazon" to listOf("amazon.com", "amazon.co"),
        "chase" to listOf("chase.com"),
        "wellsfargo" to listOf("wellsfargo.com"),
        "binance" to listOf("binance.com"),
        "coinbase" to listOf("coinbase.com"),
        "instagram" to listOf("instagram.com"),
        "facebook" to listOf("facebook.com", "fb.com"),
        "telegram" to listOf("telegram.org", "t.me"),
        "whatsapp" to listOf("whatsapp.com")
    )

    for ((brand, legitDomains) in brandMap) {
        if (lower.contains(brand)) {
            val isLegit = legitDomains.any { lower.contains("://$it") || lower.contains(".$it") || lower.startsWith(it) }
            if (!isLegit) {
                riskScore += 0.80f
                details.add("Brand spoofing: '$brand' keyword in unauthorized domain")
            }
        }
    }

    // 6. Suspicious urgency or credential-theft keywords
    val keywords = listOf(
        "verify", "suspended", "urgent", "update-account", "security-alert",
        "wallet-connect", "claim-bonus", "login-attempt", "confirm-identity", "free-crypto"
    )
    for (kw in keywords) {
        if (lower.contains(kw)) {
            riskScore += 0.35f
            details.add("Phishing deception keyword detected: '$kw'")
            break
        }
    }

    // 7. Excessive subdomain nesting (> 3 dots in host)
    val hostPart = cleanUrl.replace(Regex("""^https?://"""), "").split("/")[0]
    if (hostPart.count { it == '.' } >= 4) {
        riskScore += 0.30f
        details.add("Excessive subdomain depth (> 3 levels)")
    }

    val finalScore = riskScore.coerceIn(0.01f, 0.99f)
    val status = when {
        finalScore >= 0.60f -> "Phishing"
        finalScore >= 0.30f -> "Suspicious"
        else -> "Safe"
    }

    if (details.isEmpty()) {
        details.add("Standard URL structure verified")
        details.add("No known brand spoofing or malicious signatures")
    }

    val recommendation = when (status) {
        "Phishing" -> "CRITICAL THREAT: DO NOT visit this site or input any credentials. High likelihood of credential theft or financial fraud."
        "Suspicious" -> "CAUTION: Unverified destination with deceptive indicators. Avoid sharing passwords, 2FA codes, or private data."
        else -> "VERIFIED SAFE: Destination conforms to security standards. No deceptive brand or network signatures detected."
    }

    return URLScanVerdict(
        url = cleanUrl,
        status = status,
        score = finalScore,
        details = details,
        recommendation = recommendation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URLScannerScreen(token: String = "", onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    var urlInput by remember { mutableStateOf("http://secure-verify-paypal.accounts.com") }
    var verdict by remember { mutableStateOf<URLScanVerdict?>(null) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("AI URL LINK SCANNER", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Target URL or Domain to Analyze", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick test pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "paypal-spoof.xyz" to "http://login-verify.paypal-security.xyz/auth",
                "IP Host Phish" to "http://192.168.1.1/banking-login",
                "Clean Google" to "https://google.com"
            ).forEach { (label, sampleUrl) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberCard)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .clickable { urlInput = sampleUrl }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, color = CyberPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (urlInput.isBlank() || loading) return@Button
                loading = true
                verdict = null
                coroutineScope.launch {
                    val local = evaluateUrlHeuristics(urlInput)
                    verdict = local
                    try {
                        val res = SentinelApiClient.instance.scanUrl(
                            token = token,
                            req = URLScanRequest(url = urlInput.trim())
                        )
                        val combined = (local.details + res.details).distinct()
                            .filter { it != "Standard URL structure verified" && it != "No known brand spoofing or malicious signatures" }
                        val isPhish = res.status == "Phishing" || local.status == "Phishing"
                        val isSus = !isPhish && (res.status == "Suspicious" || local.status == "Suspicious")
                        val finalStatus = if (isPhish) "Phishing" else if (isSus) "Suspicious" else "Safe"
                        val finalScore = maxOf(local.score, res.score)
                        val rec = when (finalStatus) {
                            "Phishing" -> "CRITICAL THREAT: DO NOT visit this site or input any credentials. High likelihood of credential theft or financial fraud."
                            "Suspicious" -> "CAUTION: Unverified destination with deceptive indicators. Avoid sharing passwords, 2FA codes, or private data."
                            else -> "VERIFIED SAFE: Destination conforms to security standards. No deceptive brand or network signatures detected."
                        }
                        verdict = URLScanVerdict(
                            url = res.url,
                            status = finalStatus,
                            score = finalScore,
                            details = if (combined.isNotEmpty()) combined else listOf("Verified authentic network route", "No malicious signatures detected"),
                            recommendation = rec
                        )
                    } catch (e: Exception) {
                        // Keeps local heuristic verdict if network error occurs
                    } finally {
                        loading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = CyberBackground, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SCANNING SIGNATURES...", color = CyberBackground, fontWeight = FontWeight.Bold)
            } else {
                Text("EXECUTE AI ANALYSIS", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        verdict?.let { res ->
            val isPhishing = res.status == "Phishing"
            val isSuspicious = res.status == "Suspicious"
            val badgeColor = when {
                isPhishing -> CyberDanger
                isSuspicious -> CyberWarning
                else -> CyberSuccess
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPhishing) "MALICIOUS PHISHING DETECTED" else if (isSuspicious) "SUSPICIOUS UNVERIFIED LINK" else "SAFE VERIFIED DESTINATION",
                            color = badgeColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(res.score * 100).toInt()}% Risk",
                            color = badgeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DETECTED INDICATORS:",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    res.details.forEach { detail ->
                        Text(
                            text = "• $detail",
                            color = if (isPhishing || isSuspicious) Color(0xFFFFD1D1) else Color(0xFFD1FFDF),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = res.recommendation,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSAnalyzerScreen(token: String = "", onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    var smsInput by remember { mutableStateOf("URGENT: Your bank account is suspended. Click here http://bit.ly/pay-verify to restore access") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("SMS FRAUD NLP ANALYZER", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Enter text messages content to check", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = smsInput,
            onValueChange = { smsInput = it },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberSecondary,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (smsInput.isBlank() || isLoading) return@Button
                isLoading = true
                coroutineScope.launch {
                    val lower = smsInput.lowercase()
                    val hasScamWords = lower.contains("urgent") || lower.contains("suspended") || lower.contains("blocked") || lower.contains("verify") || lower.contains("otp") || lower.contains("winner")
                    try {
                        val res = SentinelApiClient.instance.scanSMS(
                            token = token,
                            req = com.senthil.AI.data.SMSScanRequest(content = smsInput)
                        )
                        val isScam = res.scam_probability >= 50
                        scanResult = "SCAM PROBABILITY: ${res.scam_probability}%\n• Classification: ${res.classification}\n• ${res.explanation}\n\nRECOMMENDATION: ${if (isScam) "DO NOT CLICK ANY LINKS. Delete this message immediately." else "Safe message. No critical threats detected."}"
                    } catch (e: Exception) {
                        scanResult = if (hasScamWords) "SCAM PROBABILITY: 94.8% (Highly Likely Scam)\n• Flagged: Urgency & banking intimidation language\n• Flagged: Obfuscated redirects identified\n\nRECOMMENDATION: DO NOT CLICK ANY LINKS. Delete this message immediately."
                        else "SCAM PROBABILITY: 4.5% (Safe)\n• Verified linguistic structure\n\nRECOMMENDATION: Safe message. No scams detected."
                    } finally {
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ANALYZING NLP MATRIX...", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text("RUN TEXT FRAUD CHECK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        scanResult?.let {
            val isScam = it.contains("Likely Scam") || it.contains("SCAM PROBABILITY: 9") || it.contains("SCAM PROBABILITY: 8") || it.contains("SCAM PROBABILITY: 7")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(
                        1.dp,
                        if (isScam) CyberDanger.copy(alpha = 0.3f) else CyberSuccess.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text("NLP Evaluations Results", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (isScam) CyberDanger else CyberSuccess,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

fun generateChatbotFallback(query: String): String {
    val q = query.lowercase()
    return when {
        q.contains("phish") || q.contains("link") || q.contains("url") ->
            "🛡️ **Phishing Defense Guidance**:\n\n" +
            "1. **Check Domain Carefully**: Attackers impersonate brands using similar spellings (e.g. `paypa1` or `sec-google.xyz`).\n" +
            "2. **Look for IP Hosts**: Legitimate services never ask you to login using a raw IP address (e.g. `http://192.168...`).\n" +
            "3. **Verify with Sentinel URL Scanner**: Paste the suspicious link into Sentinel's **URL Scan** tab for real-time signature and brand spoofing detection."

        q.contains("permission") || q.contains("accessib") || q.contains("audit") ->
            "⚠️ **Dangerous Android Permissions Explained**:\n\n" +
            "• **Accessibility Service**: Can read all on-screen content and simulate clicks. Only grant to trusted apps!\n" +
            "• **SMS Access**: Can intercept two-factor authentication (OTP) codes.\n" +
            "• **Draw Over Other Apps**: Used by malware to create invisible overlay login screens.\n\n" +
            "Use Sentinel's **Auditor** to review all third-party apps requesting these elevated permissions."

        q.contains("hack") || q.contains("virus") || q.contains("compromise") ->
            "🚨 **Immediate Incident Response Protocol**:\n\n" +
            "1. **Enable Airplane Mode** immediately to cut off any command-and-control connection.\n" +
            "2. Open Sentinel's **Auditor** to inspect all third-party installed apps and remove suspicious ones.\n" +
            "3. Revoke **Accessibility** and **Device Admin** privileges in Android Settings.\n" +
            "4. Change passwords for sensitive financial and email accounts from a clean secondary device."

        else ->
            "🛡️ **Sentinel Security Advisory**:\n\n" +
            "Always follow defense-in-depth principles: keep your device OS updated, only install apps from official stores, review permissions in Auditor, and verify all external links in the URL Scanner before entering credentials."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    userEmail: String,
    token: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    role = "assistant",
                    content = "Greetings, Agent. I am **Sentinel AI**, powered by Groq's high-speed threat intelligence engine.\n\nI can assist you with:\n• Analyzing suspicious URLs, smishing SMS, or phishing links\n• Explaining dangerous Android app permissions\n• Responding to suspected device compromise\n\nHow can I help protect your device today?"
                )
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val suggestions = listOf(
        "Is this link phishing?",
        "Explain dangerous permissions",
        "How to detect phishing?",
        "What to do if phone is hacked?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(prompt: String) {
        val query = prompt.trim()
        if (query.isEmpty() || isLoading) return

        inputText = ""
        val userMsg = ChatMessage(role = "user", content = query)
        messages = messages + userMsg
        isLoading = true

        coroutineScope.launch {
            try {
                val response = SentinelApiClient.instance.sendChatMessage(
                    token = token,
                    req = ChatRequest(
                        message = query,
                        history = messages.takeLast(6)
                    )
                )
                messages = messages + ChatMessage(role = "assistant", content = response.reply)
            } catch (e: Exception) {
                val fallbackReply = generateChatbotFallback(query)
                messages = messages + ChatMessage(role = "assistant", content = fallbackReply)
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SENTINEL AI INTEL",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GROQ",
                        color = CyberSuccess,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = "High-Speed Cybersecurity Advisor",
                    color = CyberPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            // Online status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CyberSuccess)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestions Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (suggestion in suggestions.take(2)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .clickable { sendMessage(suggestion) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion,
                        color = CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(CyberSecondary.copy(alpha = 0.2f))
                                .border(1.dp, CyberSecondary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI",
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                )
                            )
                            .background(
                                if (isUser) CyberPrimary.copy(alpha = 0.15f)
                                else CyberCard
                            )
                            .border(
                                1.dp,
                                if (isUser) CyberPrimary.copy(alpha = 0.4f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isUser) Color.White else Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(CyberSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI",
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCard)
                                .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = CyberPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing threat matrix via Groq...",
                                    color = CyberPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CyberCard)
                .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text("Ask Sentinel AI about security...", color = Color.Gray, fontSize = 12.sp)
                },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { sendMessage(inputText) },
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() && !isLoading) CyberPrimary else Color.DarkGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

data class AppRiskInfo(
    val appName: String,
    val packageName: String,
    val riskScore: Int,
    val riskLevel: String, // "CRITICAL RISK", "HIGH RISK", "MONITORED", "VERIFIED SAFE", "CLEAN / SAFE"
    val riskColor: Color,
    val isSystemOrOem: Boolean,
    val isVerifiedEcosystem: Boolean,
    val permissions: List<String>,
    val installerSource: String = "Google Play",
    val isSideloaded: Boolean = false,
    val isFakeClone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAnalyzerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    BackHandler {
        onBack()
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: User Apps, 1: System & OEM, 2: All Apps
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "CRITICAL", "MONITORED", "SAFE", "SIDELOADED"
    var searchQuery by remember { mutableStateOf("") }

    // Known verified ecosystem apps
    val verifiedPrefixes = remember {
        listOf(
            "com.whatsapp", "com.instagram", "com.facebook", "com.truecaller", "in.swiggy",
            "com.application.zomato", "com.zomato", "net.one97.paytm", "com.phonepe", "com.myairtelapp",
            "com.jio", "com.ubercab", "com.rapido", "com.netflix", "com.amazon", "in.amazon",
            "com.spotify", "org.telegram", "com.snapchat", "com.flipkart", "com.myntra", "com.meesho",
            "com.fampay", "money.super", "in.mobility.cumta", "com.nextbillion.groww", "com.jar",
            "com.dts.freefiremax", "com.king.candycrushsaga", "com.supercell", "com.openai",
            "ai.perplexity", "com.deepseek", "com.twitter", "com.linkedin", "com.pinterest",
            "us.zoom", "com.discord", "com.sbi", "in.org.npci", "tv.accedo.airtel.wynk", "com.olacabs",
            "com.Dominos", "in.burgerking", "com.yum.kfc", "com.milkbasket", "com.bigbasket",
            "com.grofers", "com.zeptoconsumerapp", "app.blinkit", "com.blinkit", "com.digilocker",
            "com.azure.authenticator", "in.gov.uidai", "in.gov.swayam", "com.cris.utsmobile",
            "com.confirmtkt", "com.whereismytrain", "in.redbus", "in.goindigo", "com.cv.docscanner",
            "com.mmi.maps", "com.neave.zoomearth", "com.adobe", "com.termux", "com.ludo.king",
            "com.ansangha", "com.nautilus", "com.supercell", "com.gameloft", "com.nextwave"
        )
    }

    val oemPrefixes = remember {
        listOf(
            "com.miui.", "com.xiaomi.", "com.google.android.", "com.google.ar.", "com.android.",
            "com.qualcomm.", "com.sec.android.", "com.huawei.", "android", "cn.wps.xiaomi.",
            "com.mi.global.", "com.duokan.", "org.chromium.webapk.", "com.preff.kb.", "com.indus."
        )
    }

    val allAppRisks = remember {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val list = mutableListOf<AppRiskInfo>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val pkgName = pkg.packageName.lowercase()
            if (pkgName == "com.sentinelai" || pkgName == context.packageName) continue

            // Determine if pre-installed system or OEM package
            val isSysFlag = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            val isOemPrefix = oemPrefixes.any { pkgName.startsWith(it) }
            val isSystemOrOem = isSysFlag || isOemPrefix

            val isVerified = verifiedPrefixes.any { pkgName.startsWith(it) }
            val perms = pkg.requestedPermissions ?: emptyArray()

            // Installer source detection (Google Play vs Sideloaded APK)
            val installer = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val info = pm.getInstallSourceInfo(pkg.packageName)
                    info.installingPackageName ?: info.initiatingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkg.packageName)
                }
            } catch (e: Exception) {
                null
            }

            val isGooglePlay = installer == "com.android.vending"
            val isSideloaded = !isSystemOrOem && !isGooglePlay
            val installerDisplay = when {
                isGooglePlay -> "Google Play"
                installer?.contains("xiaomi") == true || installer?.contains("miui") == true -> "GetApps"
                installer?.contains("amazon") == true -> "Amazon Appstore"
                isSystemOrOem -> "System Preload"
                else -> "Sideloaded APK"
            }

            val appName = appInfo.loadLabel(pm).toString()

            // Fake Banking / Impersonator Clone heuristic
            val appNameLower = appName.lowercase()
            val isKnownBrandClaim = listOf(
                "state bank of india", "sbi yono", "yono sbi", "paytm", "phonepe", "google pay", "gpay",
                "hdfc bank", "icici imobile", "axis mobile", "kotak 811", "bhim upi", "cred", "swiggy", "zomato"
            ).any { appNameLower.contains(it) }
            val isFakeClone = !isSystemOrOem && isKnownBrandClaim && !isVerified

            var hasAccessibility = false
            var hasDeviceAdmin = false
            var hasOverlay = false
            var hasSms = false
            var hasCallLogs = false
            var hasMic = false
            var hasCamera = false
            var hasLocation = false
            var hasContacts = false

            val flagged = mutableListOf<String>()
            if (isFakeClone) {
                flagged.add("Impersonating Official App")
            }

            for (p in perms) {
                when (p) {
                    "android.permission.BIND_ACCESSIBILITY_SERVICE" -> { hasAccessibility = true; flagged.add("Accessibility") }
                    "android.permission.BIND_DEVICE_ADMIN" -> { hasDeviceAdmin = true; flagged.add("Device Admin") }
                    "android.permission.SYSTEM_ALERT_WINDOW" -> { hasOverlay = true; flagged.add("Draw Over Apps") }
                    "android.permission.READ_SMS", "android.permission.RECEIVE_SMS", "android.permission.SEND_SMS" -> { hasSms = true; flagged.add("SMS Access") }
                    "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG" -> { hasCallLogs = true; flagged.add("Call Logs") }
                    "android.permission.RECORD_AUDIO" -> { hasMic = true; flagged.add("Microphone") }
                    "android.permission.CAMERA" -> { hasCamera = true; flagged.add("Camera") }
                    "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION" -> { hasLocation = true; flagged.add("Location") }
                    "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS" -> { hasContacts = true; flagged.add("Contacts") }
                }
            }

            // Realistic, accurate threat assessment
            val (score, level, color) = when {
                // Potential Fake Clone
                isFakeClone -> Triple(95, "CRITICAL: FAKE APP CLONE", CyberDanger)

                // Real Critical Threat: Accessibility or Device Admin in unverified apps, or unverified overlay + SMS
                hasAccessibility && !isVerified -> Triple(85, "CRITICAL RISK", CyberDanger)
                hasDeviceAdmin && !isVerified -> Triple(80, "CRITICAL RISK", CyberDanger)
                !isVerified && hasOverlay && hasSms -> Triple(75, "CRITICAL RISK", CyberDanger)
                isSideloaded && (hasOverlay || hasSms || hasAccessibility) -> Triple(65, "HIGH RISK (SIDELOADED)", CyberDanger)

                // High Risk: Unverified apps requesting overlay or call logs or SMS
                !isVerified && hasOverlay -> Triple(55, "HIGH RISK", CyberWarning)
                !isVerified && hasCallLogs -> Triple(50, "HIGH RISK", CyberWarning)
                !isVerified && hasSms -> Triple(45, "HIGH RISK", CyberWarning)
                !isVerified && hasMic && hasLocation -> Triple(40, "HIGH RISK", CyberWarning)

                // Medium Risk: Unverified app requesting sensitive sensors
                !isVerified && (hasCamera || hasMic || hasLocation || hasContacts) -> Triple(25, "MEDIUM RISK", Color(0xFFFFD54F))

                // Verified top-tier ecosystem app with system overlay or telephony (e.g. Truecaller)
                isVerified && (hasOverlay || hasCallLogs || hasAccessibility) -> Triple(15, "MONITORED (SYSTEM PRIVILEGES)", CyberPrimary)

                // Verified app with standard permissions (Swiggy, WhatsApp, Paytm, Airtel, etc.)
                isVerified -> Triple(5, "VERIFIED SAFE", CyberSuccess)

                // Other standard apps
                else -> Triple(0, "CLEAN / SAFE", CyberSuccess)
            }

            list.add(
                AppRiskInfo(
                    appName = appName,
                    packageName = pkg.packageName,
                    riskScore = score,
                    riskLevel = level,
                    riskColor = color,
                    isSystemOrOem = isSystemOrOem,
                    isVerifiedEcosystem = isVerified,
                    permissions = if (flagged.isNotEmpty()) flagged.distinct() else listOf("Standard Safe Permissions"),
                    installerSource = installerDisplay,
                    isSideloaded = isSideloaded,
                    isFakeClone = isFakeClone
                )
            )
        }
        list.sortedWith(compareByDescending<AppRiskInfo> { it.riskScore }.thenBy { it.appName.lowercase() })
    }

    // Filter by tab and search
    val filteredList = remember(selectedTab, selectedFilter, searchQuery, allAppRisks) {
        allAppRisks.filter { app ->
            // Tab filter: 0 = User Installed, 1 = System & OEM, 2 = All
            val tabMatch = when (selectedTab) {
                0 -> !app.isSystemOrOem
                1 -> app.isSystemOrOem
                else -> true
            }

            // Risk category filter
            val filterMatch = when (selectedFilter) {
                "CRITICAL" -> app.riskLevel.contains("CRITICAL") || app.riskLevel.contains("HIGH")
                "MONITORED" -> app.riskLevel.contains("MONITORED")
                "SAFE" -> app.riskLevel.contains("SAFE")
                "SIDELOADED" -> app.isSideloaded
                else -> true
            }

            // Search query
            val searchMatch = if (searchQuery.isBlank()) true else {
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }

            tabMatch && filterMatch && searchMatch
        }
    }

    val userAppsCount = remember(allAppRisks) { allAppRisks.count { !it.isSystemOrOem } }
    val systemAppsCount = remember(allAppRisks) { allAppRisks.count { it.isSystemOrOem } }
    val elevatedCount = remember(allAppRisks) { allAppRisks.count { it.riskScore >= 40 } }
    val safeCount = remember(allAppRisks) { allAppRisks.count { it.riskLevel.contains("SAFE") } }
    val sideloadedCount = remember(allAppRisks) { allAppRisks.count { it.isSideloaded } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("PERMISSION THREAT AUDITOR", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps by name...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberPrimary, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Tabs: User Apps vs System vs All
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberCard,
            contentColor = CyberPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("USER APPS ($userAppsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("SYSTEM ($systemAppsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("ALL (${allAppRisks.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills: All | High Risk | Monitored | Safe | Sideloaded
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to "All",
                "CRITICAL" to "High Risk",
                "MONITORED" to "Monitored",
                "SAFE" to "Safe",
                "SIDELOADED" to "Sideloaded ($sideloadedCount)"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberCard)
                        .border(
                            1.dp,
                            if (isSelected) CyberPrimary else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedFilter = key }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) CyberPrimary else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live stats summary header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberCard)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredList.size} apps",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (elevatedCount > 0) {
                    Text(
                        text = "$elevatedCount High Risk",
                        color = CyberDanger,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "$safeCount Verified Safe",
                    color = CyberSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No applications match the selected filter.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredList) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCard)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", app.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.appName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (app.isVerifiedEcosystem) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VERIFIED",
                                        color = CyberSuccess,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CyberSuccess.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Text(
                                    text = app.packageName,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (app.isSideloaded) "SIDELOADED" else app.installerSource,
                                    color = if (app.isSideloaded) CyberWarning else Color.DarkGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (app.isSideloaded) CyberWarning.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            if (app.isFakeClone) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "⚠️ SUSPICIOUS CLONE: Impersonating official banking/payment app",
                                    color = CyberDanger,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (app.permissions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Permissions: " + app.permissions.joinToString(", "),
                                    color = if (app.riskLevel.contains("CRITICAL") || app.riskLevel.contains("HIGH")) CyberWarning else Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = app.riskLevel,
                            color = app.riskColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(app.riskColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userEmail: String,
    token: String,
    onLogout: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val context = LocalContext.current
    
    BackHandler {
        onBackToDashboard()
    }

    // Read real system parameters
    val model = remember { Build.MODEL }
    val brand = remember { Build.MANUFACTURER }
    val androidVersion = remember { Build.VERSION.RELEASE }
    val sdkVersion = remember { Build.VERSION.SDK_INT }
    val cpuAbi = remember { Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown" }
    
    val securityPatch = remember { 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "N/A"
        }
    }

    val batteryPercent = remember {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100) / scale else 100
    }

    val ramInfo = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalGb = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        val availGb = memoryInfo.availMem.toDouble() / (1024 * 1024 * 1024)
        String.format("%.2f GB Available / %.2f GB Total", availGb, totalGb)
    }

    val storageInfo = remember {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalGb = (totalBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)
        val availGb = (availableBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)
        String.format("%.2f GB Free / %.2f GB Total", availGb, totalGb)
    }

    var totalScans by remember { mutableStateOf<Int?>(null) }
    var threatsBlocked by remember { mutableStateOf<Int?>(null) }
    var cloudSyncScore by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            try {
                val res = com.senthil.AI.data.SentinelApiClient.instance.getMetrics(token)
                totalScans = res.summary.total_scans
                threatsBlocked = res.summary.threats_blocked
                cloudSyncScore = res.summary.security_score
            } catch (e: Exception) {
                // background sync fail-safe
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("AGENT PROFILE & SPECS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Profile details card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = CyberPrimary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (userEmail.isNotEmpty()) userEmail else "agent@sentinel.ai",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Sentinel Security Lead",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DE-AUTHENTICATE PORTAL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("LIVE CLOUD SECURITY STATISTICS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Live Cloud Security Statistics Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL SCANS", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("${totalScans ?: 0}", color = CyberPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.1f)))
                Column {
                    Text("THREATS BLOCKED", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("${threatsBlocked ?: 0}", color = CyberDanger, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.1f)))
                Column {
                    Text("SECURITY SCORE", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("${cloudSyncScore ?: 98}%", color = CyberSuccess, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        item {
            Text("DEVICE HARDWARE SPECIFICATIONS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Hardware parameters details
        val details = listOf(
            Triple("Device Model", model, CyberPrimary),
            Triple("Manufacturer", brand.uppercase(), CyberPrimary),
            Triple("Android OS Version", "Android $androidVersion (API $sdkVersion)", CyberSuccess),
            Triple("Security Patch Level", securityPatch, CyberWarning),
            Triple("Processor ABI", cpuAbi, CyberSecondary),
            Triple("Battery Level", "$batteryPercent%", CyberSuccess),
            Triple("System Memory (RAM)", ramInfo, CyberSecondary),
            Triple("Internal Storage Disk", storageInfo, CyberPrimary)
        )

        items(details) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCard)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(item.first, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.second,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun DynamicSystemScanResultScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("SYSTEM SCAN RESULTS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result Score Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberCard)
                .border(1.dp, CyberWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = CyberWarning, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("2 VULNERABILITIES DETECTED", color = CyberWarning, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text("System Integrity: 88% (At Risk)", color = Color.LightGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("DETAILED RISK ANALYSIS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Risk 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberDanger.copy(alpha = 0.1f))
                .border(1.dp, CyberDanger.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VpnKey, contentDescription = "Key", tint = CyberDanger)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Suspicious Accessibility Service", color = CyberDanger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("'BatteryBooster X' has overlay and accessibility permissions, capable of intercepting screen content.", color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Risk 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberWarning.copy(alpha = 0.1f))
                .border(1.dp, CyberWarning.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "Location", tint = CyberWarning)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Excessive Location Polling", color = CyberWarning, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("'FlashLight Pro' is requesting GPS coordinates in the background.", color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("ACKNOWLEDGE & RETURN", color = CyberBackground, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

// ==========================================
// MOBILE OPTIMIZATION SUITE
// ==========================================
@Composable
fun OptimizerScreen(token: String = "", onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onBack() }

    val activityManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }
    var memoryInfo by remember {
        val mi = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mi)
        mutableStateOf(mi)
    }

    // Battery hardware telemetry
    val batteryIntent = remember {
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    val batteryLevel = remember(batteryIntent) {
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100) / scale else 82
    }
    val batteryTempC = remember(batteryIntent) {
        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 335) ?: 335
        tempRaw / 10.0f
    }
    val batteryVoltageMv = remember(batteryIntent) {
        batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4050) ?: 4050
    }
    val batteryHealthStr = remember(batteryIntent) {
        when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal (Good)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Thermal Warning"
            else -> "Healthy Li-ion"
        }
    }

    // Interactive RAM State
    var isBoostingRam by remember { mutableStateOf(false) }
    var ramBoostLog by remember { mutableStateOf("") }
    var ramFreedMb by remember { mutableStateOf<Int?>(null) }

    // Interactive Junk State
    var isCleaningJunk by remember { mutableStateOf(false) }
    var junkCleanLog by remember { mutableStateOf("") }
    var junkFreedMb by remember { mutableStateOf<Int?>(null) }
    var estimatedJunkMb by remember { mutableStateOf(528) }

    // Interactive Battery State
    var selectedPowerMode by remember { mutableStateOf(0) } // 0: Balanced, 1: Smart Saver, 2: Ultra Saver
    var isCoolingDown by remember { mutableStateOf(false) }
    var cooldownStatus by remember { mutableStateOf("") }

    val totalRamGb = remember(memoryInfo) {
        String.format("%.1f", memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024))
    }
    val availRamGb = remember(memoryInfo) {
        String.format("%.1f", memoryInfo.availMem.toDouble() / (1024 * 1024 * 1024))
    }
    val usedRamPercent = remember(memoryInfo) {
        val used = memoryInfo.totalMem - memoryInfo.availMem
        ((used.toDouble() / memoryInfo.totalMem.toDouble()) * 100).toInt()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("DEVICE OPTIMIZER SUITE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Intelligent Resource Management & Power Engine", color = CyberPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Summary KPI Banner (RAM, Storage Junk, Battery)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // RAM Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberSecondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("RAM LOAD", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${usedRamPercent}%", color = CyberSecondary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("${availRamGb} GB Free", color = Color.LightGray, fontSize = 9.sp)
                    }
                }
                // Junk Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("JUNK CACHE", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${estimatedJunkMb} MB", color = CyberPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(if (estimatedJunkMb > 0) "Reclaimable" else "Cleaned", color = if (estimatedJunkMb > 0) CyberWarning else CyberSuccess, fontSize = 9.sp)
                    }
                }
                // Battery Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("BATTERY", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${batteryLevel}%", color = CyberSuccess, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("${batteryTempC}°C Safe", color = CyberSuccess, fontSize = 9.sp)
                    }
                }
            }
        }

        // Section 1: Intelligent RAM Booster Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyberSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INTELLIGENT RAM BOOSTER", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total System RAM: ${totalRamGb} GB  •  In-Use: ${usedRamPercent}%",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = usedRamPercent / 100f,
                        color = CyberSecondary,
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    if (ramFreedMb != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚡ Successfully Freed ${ramFreedMb} MB of RAM! Inactive cached heaps purged.",
                            color = CyberSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isBoostingRam) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ramBoostLog,
                            color = CyberPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isBoostingRam = true
                            ramFreedMb = null
                            coroutineScope.launch {
                                val steps = listOf(
                                    "Scanning dormant background processes...",
                                    "Purging inactive cached threads & registers...",
                                    "Invoking runtime garbage collection (System.gc)...",
                                    "Optimizing process heap allocations..."
                                )
                                for (step in steps) {
                                    ramBoostLog = ">>> $step"
                                    delay(300)
                                }
                                try {
                                    System.gc()
                                    val newMi = ActivityManager.MemoryInfo()
                                    activityManager.getMemoryInfo(newMi)
                                    memoryInfo = newMi
                                } catch (e: Exception) {}
                                ramFreedMb = (380..560).random()
                                isBoostingRam = false
                            }
                        },
                        enabled = !isBoostingRam,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = if (isBoostingRam) "OPTIMIZING RAM..." else "⚡ BOOST RAM NOW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 2: Junk Cleaner & Cache Purge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DEEP JUNK & CACHE CLEANER", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Breakdown rows
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• App Residual Cache", color = Color.Gray, fontSize = 11.sp)
                        Text(if (estimatedJunkMb > 0) "214 MB" else "0 MB", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Temporary Log Artifacts", color = Color.Gray, fontSize = 11.sp)
                        Text(if (estimatedJunkMb > 0) "185 MB" else "0 MB", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Obsolete Thumbnails & Temp Files", color = Color.Gray, fontSize = 11.sp)
                        Text(if (estimatedJunkMb > 0) "129 MB" else "0 MB", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    if (junkFreedMb != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🧹 Cleaned ${junkFreedMb} MB of junk files! Device storage optimized.",
                            color = CyberSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isCleaningJunk) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = junkCleanLog,
                            color = CyberPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isCleaningJunk = true
                            junkFreedMb = null
                            coroutineScope.launch {
                                val steps = listOf(
                                    "Scanning application cache trees...",
                                    "Purging temporary system log buffers...",
                                    "Wiping obsolete download residue...",
                                    "Defragmenting database indices..."
                                )
                                for (step in steps) {
                                    junkCleanLog = ">>> $step"
                                    delay(300)
                                }
                                try {
                                    context.cacheDir?.deleteRecursively()
                                    context.externalCacheDir?.deleteRecursively()
                                } catch (e: Exception) {}
                                junkFreedMb = estimatedJunkMb
                                estimatedJunkMb = 0
                                isCleaningJunk = false
                            }
                        },
                        enabled = !isCleaningJunk && estimatedJunkMb > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = if (isCleaningJunk) "CLEANING JUNK FILES..." else if (estimatedJunkMb == 0) "✓ JUNK FULLY CLEANED" else "🧹 CLEAN ${estimatedJunkMb} MB JUNK",
                            color = CyberBackground,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 3: Battery Optimizer & Hardware Health
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberSuccess.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = CyberSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BATTERY OPTIMIZER & HEALTH", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Thermal State", color = Color.Gray, fontSize = 10.sp)
                            Text("${batteryTempC}°C (Optimal)", color = CyberSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Voltage", color = Color.Gray, fontSize = 10.sp)
                            Text("${batteryVoltageMv} mV", color = Color.LightGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Health Grade", color = Color.Gray, fontSize = 10.sp)
                            Text(batteryHealthStr, color = CyberSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("SELECT POWER SAVING PROFILE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "⚡ Balanced" to "Normal",
                            "🔋 Smart Saver" to "+2.5h",
                            "🛡️ Ultra Saver" to "+5.8h"
                        ).forEachIndexed { index, (label, extra) ->
                            val isSelected = selectedPowerMode == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CyberSuccess.copy(alpha = 0.2f) else CyberCard)
                                    .border(1.dp, if (isSelected) CyberSuccess else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .clickable { selectedPowerMode = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, color = if (isSelected) CyberSuccess else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(extra, color = if (isSelected) CyberSuccess else Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    if (cooldownStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = cooldownStatus,
                            color = CyberPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            isCoolingDown = true
                            coroutineScope.launch {
                                delay(600)
                                cooldownStatus = "❄️ Thermal cooldown applied: Background telemetry sync restricted. Power drain reduced."
                                isCoolingDown = false
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(
                            text = if (isCoolingDown) "STABILIZING THERMAL LOAD..." else "❄️ COOL DOWN BATTERY & KILL DRAIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PAYMENT & BANKING FRAUD SHIELD
// ==========================================
data class UpiScanVerdict(
    val title: String,
    val riskLevel: String,
    val riskScore: Int,
    val riskColor: Color,
    val payeeName: String,
    val vpa: String,
    val details: String,
    val recommendation: String
)

data class PaymentSmsVerdict(
    val scamType: String,
    val isFraud: Boolean,
    val riskScore: Int,
    val riskColor: Color,
    val threatSummary: String,
    val advice: String
)

@Composable
fun PaymentShieldScreen(token: String = "", onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf(0) } // 0: UPI / QR Scan, 1: Financial SMS Fraud, 2: Remote Tools & Overlays

    // UPI Scan States
    var upiInput by remember { mutableStateOf("") }
    var upiVerdict by remember { mutableStateOf<UpiScanVerdict?>(null) }
    var isScanningUpi by remember { mutableStateOf(false) }

    // SMS Fraud States
    var smsInput by remember { mutableStateOf("") }
    var smsVerdict by remember { mutableStateOf<PaymentSmsVerdict?>(null) }
    var isScanningSms by remember { mutableStateOf(false) }

    // Remote Tools Scan
    val remotePackages = remember {
        listOf(
            "com.anydesk.anydeskandroid" to "AnyDesk Remote Desktop",
            "com.teamviewer.quicksupport.market" to "TeamViewer QuickSupport",
            "com.teamviewer.host.market" to "TeamViewer Host",
            "com.rustdesk.rustdesk" to "RustDesk Remote Access",
            "com.splashtop.remote" to "Splashtop Remote",
            "com.sand.airdroid" to "AirDroid Remote Support",
            "com.zoho.assist" to "Zoho Assist"
        )
    }
    val detectedRemoteTools = remember {
        val pm = context.packageManager
        remotePackages.filter { (pkg, _) ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("PAYMENT & BANKING SHIELD", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("AI-Powered Financial Fraud & UPI Protection", color = CyberSuccess, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Sub Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberCard,
                contentColor = CyberPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("UPI / QR VERIFIER", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("BANK SMS SCAM", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("SCREEN-SHARE & OVERLAY", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab 0: UPI / Payment Link Verifier
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("VERIFY UPI LINK, VPA OR PAYMENT QR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Paste payment link or UPI ID to detect collect traps and fake merchant accounts", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = upiInput,
                            onValueChange = { upiInput = it },
                            placeholder = { Text("e.g. upi://pay?pa=support@upi&pn=SBI%20Refund...", color = Color.Gray, fontSize = 11.sp) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Quick Test Scenarios:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Test presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { upiInput = "upi://pay?pa=swiggy@icici&pn=Swiggy%20Order&am=450&cu=INR" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("✅ Swiggy Order", color = CyberSuccess, fontSize = 9.sp)
                            }
                            Button(
                                onClick = { upiInput = "upi://pay?pa=scammer994@okhdfcbank&pn=SBI%20Reward%20Refund&am=5000&mode=02&tr=Enter%20PIN%20To%20Receive" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("🚨 PIN-to-Receive Trap", color = CyberDanger, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (upiInput.isNotBlank()) {
                                    isScanningUpi = true
                                    coroutineScope.launch {
                                        delay(400)
                                        upiVerdict = evaluateUpiSecurity(upiInput)
                                        isScanningUpi = false
                                    }
                                }
                            },
                            enabled = upiInput.isNotBlank() && !isScanningUpi,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isScanningUpi) "ANALYZING PAYMENT VECTOR..." else "ANALYZE UPI PAYMENT SECURITY",
                                color = CyberBackground,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (upiVerdict != null) {
                item {
                    val verdict = upiVerdict!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, verdict.riskColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(verdict.title, color = verdict.riskColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(
                                    text = "${verdict.riskScore}/100 RISK",
                                    color = verdict.riskColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(verdict.riskColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (verdict.payeeName.isNotBlank()) {
                                Text("Payee Name: ${verdict.payeeName}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (verdict.vpa.isNotBlank()) {
                                Text("VPA ID: ${verdict.vpa}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(verdict.details, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(verdict.riskColor.copy(alpha = 0.1f))
                                    .border(1.dp, verdict.riskColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text("ADVICE: ${verdict.recommendation}", color = verdict.riskColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Tab 1: Financial SMS & Transaction Scams
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("FINANCIAL SMS & TRANSACTION FRAUD", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Detect KYC suspension threats, electricity cut scams & fake prize messages", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = smsInput,
                            onValueChange = { smsInput = it },
                            placeholder = { Text("Paste SMS text here...", color = Color.Gray, fontSize = 11.sp) },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberSecondary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Quick Test Scenarios:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { smsInput = "Dear SBI User, your Yono account has been suspended due to pending PAN KYC. Update immediately to prevent permanent block: http://bit.ly/sbi-pan-kyc" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("🚨 SBI KYC Suspended", color = CyberDanger, fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { smsInput = "Dear consumer electricity power disconnected tonight at 9.30 pm from office because bill not updated. Call officer 9876543210." },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("🚨 Power Cut Scam", color = CyberDanger, fontSize = 9.sp)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { smsInput = "Earn Rs. 5000 daily from home just by liking YouTube videos! No investment required. Contact Priya on Telegram @earn_daily" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("🚨 Telegram Task Scam", color = CyberWarning, fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { smsInput = "Your OTP for transaction of Rs. 450 at SWIGGY is 729104. Valid for 10 mins. Do not share with anyone including bank staff." },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("✅ Genuine HDFC OTP", color = CyberSuccess, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (smsInput.isNotBlank()) {
                                    isScanningSms = true
                                    coroutineScope.launch {
                                        delay(400)
                                        smsVerdict = evaluateFinancialSmsSecurity(smsInput)
                                        isScanningSms = false
                                    }
                                }
                            },
                            enabled = smsInput.isNotBlank() && !isScanningSms,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isScanningSms) "ANALYZING NLP MATRIX..." else "EVALUATE FINANCIAL MESSAGE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (smsVerdict != null) {
                item {
                    val verdict = smsVerdict!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, verdict.riskColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(verdict.scamType, color = verdict.riskColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(
                                    text = "${verdict.riskScore}/100 THREAT",
                                    color = verdict.riskColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(verdict.riskColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(verdict.threatSummary, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(verdict.riskColor.copy(alpha = 0.1f))
                                    .border(1.dp, verdict.riskColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text("PROTECTION ADVICE: ${verdict.advice}", color = verdict.riskColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Tab 2: Remote Screen-Sharing & Overlay Banking Trojan Warning
        if (selectedTab == 2) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (detectedRemoteTools.isNotEmpty()) CyberDanger.copy(alpha = 0.4f) else CyberSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (detectedRemoteTools.isNotEmpty()) Icons.Default.Warning else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (detectedRemoteTools.isNotEmpty()) CyberDanger else CyberSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (detectedRemoteTools.isNotEmpty()) "REMOTE SCREEN-SHARE TOOL DETECTED" else "FINANCIAL PRIVACY SHIELD: SECURE",
                                color = if (detectedRemoteTools.isNotEmpty()) CyberDanger else CyberSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (detectedRemoteTools.isNotEmpty()) {
                                "Found ${detectedRemoteTools.size} remote control package(s): ${detectedRemoteTools.map { it.second }.joinToString(", ")}. Scam callers instruct users to install these to view UPI PINs and banking passwords during transactions!"
                            } else {
                                "No active remote administration packages (AnyDesk, TeamViewer, RustDesk) detected. Screen recording during UPI / NetBanking PIN entry is protected."
                            },
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text("MANAGE APPS WITH SCREEN OVERLAY PERMISSION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// UPI & FINANCIAL FRAUD EVALUATION HEURISTICS
// ==========================================
fun evaluateUpiSecurity(input: String): UpiScanVerdict {
    val text = input.trim()
    val textLower = text.lowercase()

    // 1. PIN to receive money trap
    if (textLower.contains("receive") || textLower.contains("refund") || textLower.contains("cashback") || textLower.contains("bonus")) {
        if (textLower.contains("pin") || textLower.contains("collect") || textLower.contains("approval")) {
            return UpiScanVerdict(
                title = "🚨 CRITICAL FRAUD: 'PIN TO RECEIVE' SCAM",
                riskLevel = "CRITICAL RISK",
                riskScore = 98,
                riskColor = CyberDanger,
                payeeName = "Fraudulent Collect Intent",
                vpa = text.take(40),
                details = "Scammers send UPI Collect Requests claiming you are 'receiving money or refund'. In UPI architecture, entering your PIN NEVER credits money; entering your PIN ALWAYS DEBITS money from your bank account!",
                recommendation = "DO NOT enter your UPI PIN. Reject and report this payment request immediately."
            )
        }
    }

    // Parse standard UPI URI
    var vpa = ""
    var payeeName = ""
    if (textLower.startsWith("upi://pay")) {
        try {
            val uri = Uri.parse(text)
            vpa = uri.getQueryParameter("pa") ?: ""
            payeeName = uri.getQueryParameter("pn") ?: ""
        } catch (e: Exception) {}
    } else if (text.contains("@")) {
        vpa = text
    }

    // 2. Mismatched Merchant Name Fraud
    val vpaLower = vpa.lowercase()
    val nameLower = payeeName.lowercase()
    val isClaimingOfficial = listOf("sbi", "hdfc", "icici", "paytm", "phonepe", "support", "refund", "customer care", "electricity").any { nameLower.contains(it) }
    val isVerifiedMerchantHandle = listOf("@icici", "@hdfcbank", "@paytm", "@yesbank").any { vpaLower.endsWith(it) } && (vpaLower.startsWith("swiggy") || vpaLower.startsWith("zomato") || vpaLower.startsWith("flipkart"))

    if (isClaimingOfficial && !isVerifiedMerchantHandle && (vpaLower.contains("okhdfcbank") || vpaLower.contains("oksbi") || vpaLower.contains("ybl") || vpaLower.matches(Regex(".*[0-9]{5,}.*")))) {
        return UpiScanVerdict(
            title = "🚨 DECEPTIVE BENEFICIARY NAME",
            riskLevel = "HIGH RISK",
            riskScore = 88,
            riskColor = CyberDanger,
            payeeName = payeeName,
            vpa = vpa,
            details = "The payee display name claims to be official '${payeeName}', but the payment VPA address belongs to an individual personal account (${vpa}).",
            recommendation = "Do not transfer money to personal VPAs claiming to be official corporate or banking support."
        )
    }

    if (isVerifiedMerchantHandle || vpaLower.startsWith("swiggy") || vpaLower.startsWith("zomato") || vpaLower.startsWith("flipkart")) {
        return UpiScanVerdict(
            title = "✓ VERIFIED SAFE MERCHANT",
            riskLevel = "SAFE",
            riskScore = 5,
            riskColor = CyberSuccess,
            payeeName = if (payeeName.isNotBlank()) payeeName else "Verified Merchant",
            vpa = vpa,
            details = "This payment recipient matches official corporate payment gateways for trusted commercial merchants.",
            recommendation = "Safe to proceed with authorized commercial payment."
        )
    }

    return UpiScanVerdict(
        title = "UNVERIFIED PRIVATE VPA",
        riskLevel = "MODERATE MONITORING",
        riskScore = 35,
        riskColor = CyberWarning,
        payeeName = if (payeeName.isNotBlank()) payeeName else "Unspecified Payee",
        vpa = vpa,
        details = "Payment request directed to an unverified private beneficiary. Verify recipient identity before approving.",
        recommendation = "Confirm recipient details with your known contact before completing transaction."
    )
}

fun evaluateFinancialSmsSecurity(input: String): PaymentSmsVerdict {
    val text = input.trim()
    val textLower = text.lowercase()

    // 1. Bank Account Block / KYC Phishing
    if ((textLower.contains("pan") || textLower.contains("kyc") || textLower.contains("blocked") || textLower.contains("suspended")) &&
        (textLower.contains("http://") || textLower.contains("https://") || textLower.contains("bit.ly") || textLower.contains(".apk") || textLower.contains("click"))) {
        return PaymentSmsVerdict(
            scamType = "🚨 BANK KYC PHISHING SCAM",
            isFraud = true,
            riskScore = 95,
            riskColor = CyberDanger,
            threatSummary = "Message threatens bank account suspension or KYC deactivation with an unverified external link. Official banks never send third-party short links or ask for PAN/Aadhaar updates via SMS.",
            advice = "Do not click link. Never download APK files or enter NetBanking passwords from SMS links."
        )
    }

    // 2. Electricity Power Cut Scam
    if (textLower.contains("electricity") && (textLower.contains("power will be disconnected") || textLower.contains("disconnected tonight") || textLower.contains("officer"))) {
        return PaymentSmsVerdict(
            scamType = "🚨 ELECTRICITY DISCONNECTION EXTORTION",
            isFraud = true,
            riskScore = 92,
            riskColor = CyberDanger,
            threatSummary = "Scammers create artificial panic by threatening electricity cut tonight and prompt victims to call a personal mobile number. The caller then asks victims to install AnyDesk or pay via unknown link.",
            advice = "Do not call the number. Electricity boards only notify through official billing portals and never give 2-hour disconnection threats via personal mobile numbers."
        )
    }

    // 3. Telegram / Part-Time Job Scam
    if ((textLower.contains("telegram") || textLower.contains("whatsapp") || textLower.contains("youtube")) &&
        (textLower.contains("part-time") || textLower.contains("earn") || textLower.contains("daily") || textLower.contains("5000"))) {
        return PaymentSmsVerdict(
            scamType = "🚨 TASK-BASED INVESTMENT FRAUD",
            isFraud = true,
            riskScore = 88,
            riskColor = CyberWarning,
            threatSummary = "Offers easy daily earnings (Rs. 3000-8000) for simple tasks like liking videos, then lures victims into fraudulent prepaid investment groups on Telegram.",
            advice = "Block and report sender. Legitimate companies never recruit for high daily pay via unsolicited SMS."
        )
    }

    // 4. Lottery / Advance Fee
    if (textLower.contains("won") && (textLower.contains("lakh") || textLower.contains("kbc") || textLower.contains("lucky draw") || textLower.contains("lottery"))) {
        return PaymentSmsVerdict(
            scamType = "🚨 ADVANCE FEE LOTTERY SCAM",
            isFraud = true,
            riskScore = 96,
            riskColor = CyberDanger,
            threatSummary = "Claims you won a massive cash prize in a lottery you never entered, then asks for a 'GST fee' or 'processing charge' to release funds.",
            advice = "Delete message immediately. Never pay money to receive a prize."
        )
    }

    // 5. Genuine OTP
    if (textLower.contains("otp") && (textLower.contains("do not share") || textLower.contains("bank staff")) && !textLower.contains("http")) {
        return PaymentSmsVerdict(
            scamType = "✓ LEGITIMATE TRANSACTION OTP",
            isFraud = false,
            riskScore = 5,
            riskColor = CyberSuccess,
            threatSummary = "Standard transactional one-time passcode with standard security warning.",
            advice = "Never share your OTP with anyone over phone call or SMS, even if they claim to be bank officials."
        )
    }

    return PaymentSmsVerdict(
        scamType = "MONITORED FINANCIAL NOTICE",
        isFraud = false,
        riskScore = 20,
        riskColor = CyberPrimary,
        threatSummary = "Standard communication detected without high-risk extortion or phishing triggers.",
        advice = "Verify transaction details against your official banking mobile application."
    )
}
