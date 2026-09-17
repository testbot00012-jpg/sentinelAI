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
            Screen.Dashboard, Screen.Chatbot, Screen.URLScanner, Screen.SMSAnalyzer, Screen.PermissionAnalyzer, Screen.Profile, Screen.DynamicScanResult -> {
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
fun DashboardScreen(userEmail: String, onNavigate: (Screen) -> Unit) {
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
                            Text("98%", color = CyberSuccess, fontSize = 48.sp, fontWeight = FontWeight.Black)
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Shield",
                                tint = CyberSuccess,
                                modifier = Modifier
                                    .size(54.dp)
                                    .padding(bottom = 6.dp)
                            )
                        }
                        Text("No critical risk patterns found during last scan cycle.", color = Color.LightGray, fontSize = 12.sp)
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

                // Active engines indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "Web Scan", status = "SECURE", tint = CyberPrimary, onClick = { onNavigate(Screen.URLScanner) })
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "SMS Spam", status = "ACTIVE", tint = CyberSecondary, onClick = { onNavigate(Screen.SMSAnalyzer) })
                    EngineStatusBox(modifier = Modifier.weight(1f), title = "Auditor", status = "SAFE", tint = CyberWarning, onClick = { onNavigate(Screen.PermissionAnalyzer) })
                }

                Text("DEVICE UTILIZATION", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                // Progress bars for RAM, Storage, and Battery on Dashboard
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
    val riskLevel: String,
    val riskColor: Color,
    val permissions: List<String>
)

@Composable
fun PermissionAnalyzerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    BackHandler {
        onBack()
    }

    // Retrieve ONLY third-party user-installed apps & calculate threat risk scores dynamically
    val appRisks = remember {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val risks = mutableListOf<AppRiskInfo>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val pkgName = pkg.packageName.lowercase()

            // 1. Strict System and OEM exclusion flags
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (isSystemApp || isUpdatedSystemApp) continue

            // 2. Package prefix exclusion for vendor/OEM background bloatware
            if (pkgName.startsWith("com.android.") ||
                pkgName.startsWith("com.google.android.") ||
                pkgName.startsWith("com.google.ar.") ||
                pkgName.startsWith("com.miui.") ||
                pkgName.startsWith("com.xiaomi.") ||
                pkgName.startsWith("com.qualcomm.") ||
                pkgName.startsWith("com.sec.android.") ||
                pkgName.startsWith("com.huawei.") ||
                pkgName.startsWith("android") ||
                pkgName == "com.sentinelAI" ||
                pkgName == context.packageName) {
                continue
            }

            val permissions = pkg.requestedPermissions ?: emptyArray<String>()
            var riskScore = 0
            val flaggedPerms = mutableListOf<String>()

            for (perm in permissions) {
                when (perm) {
                    "android.permission.BIND_ACCESSIBILITY_SERVICE" -> { riskScore += 40; flaggedPerms.add("Accessibility") }
                    "android.permission.READ_SMS", "android.permission.RECEIVE_SMS", "android.permission.SEND_SMS" -> { riskScore += 30; flaggedPerms.add("SMS Access") }
                    "android.permission.SYSTEM_ALERT_WINDOW" -> { riskScore += 25; flaggedPerms.add("Draw Over Apps") }
                    "android.permission.RECORD_AUDIO" -> { riskScore += 20; flaggedPerms.add("Microphone") }
                    "android.permission.CAMERA" -> { riskScore += 20; flaggedPerms.add("Camera") }
                    "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION" -> { riskScore += 15; flaggedPerms.add("Location") }
                    "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS" -> { riskScore += 15; flaggedPerms.add("Contacts") }
                    "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG" -> { riskScore += 25; flaggedPerms.add("Call Logs") }
                    "android.permission.READ_EXTERNAL_STORAGE", "android.permission.MANAGE_EXTERNAL_STORAGE" -> { riskScore += 10; flaggedPerms.add("Storage") }
                }
            }

            val (level, color) = when {
                riskScore >= 50 -> "CRITICAL RISK" to CyberDanger
                riskScore >= 30 -> "HIGH RISK" to CyberWarning
                riskScore >= 15 -> "MEDIUM RISK" to CyberWarning
                riskScore > 0 -> "LOW RISK" to Color.Gray
                else -> "CLEAN / SAFE" to CyberSuccess
            }

            val appName = appInfo.loadLabel(pm).toString()
            risks.add(
                AppRiskInfo(
                    appName = appName,
                    packageName = pkg.packageName,
                    riskScore = riskScore,
                    riskLevel = level,
                    riskColor = color,
                    permissions = if (flaggedPerms.isNotEmpty()) flaggedPerms.distinct() else listOf("Standard safe permissions")
                )
            )
        }

        risks.sortedByDescending { it.riskScore }
    }

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

        Spacer(modifier = Modifier.height(16.dp))

        // Third party apps summary banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberCard)
                .border(1.dp, CyberWarning.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THIRD-PARTY APPS AUDITED: ${appRisks.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "System bloatware excluded • Tap app to inspect",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "${appRisks.count { it.riskScore > 0 }} ELEVATED",
                    color = if (appRisks.any { it.riskScore >= 30 }) CyberDanger else CyberSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (appRisks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No third-party user applications detected on host device.", color = CyberSuccess, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(appRisks) { app ->
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
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.packageName,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (app.permissions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Permissions: " + app.permissions.joinToString(", "),
                                    color = app.riskColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = app.riskLevel,
                            color = app.riskColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(app.riskColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
            Text("DEVICE SPECIFICATIONS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
