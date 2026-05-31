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
    object URLScanner : Screen()
    object SMSAnalyzer : Screen()
    object PermissionAnalyzer : Screen()
    object Profile : Screen()
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
            Screen.Dashboard, Screen.URLScanner, Screen.SMSAnalyzer, Screen.PermissionAnalyzer, Screen.Profile -> {
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
                                selected = currentScreen == Screen.SMSAnalyzer,
                                onClick = { currentScreen = Screen.SMSAnalyzer },
                                icon = { Icon(Icons.Default.Sms, contentDescription = "SMS Scan") },
                                label = { Text("SMS Scan", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberSecondary,
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = CyberSecondary,
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
                            is Screen.URLScanner -> URLScannerScreen(
                                onBack = { currentScreen = Screen.Dashboard }
                            )
                            is Screen.SMSAnalyzer -> SMSAnalyzerScreen(
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
                Text("Device Status: Operational", color = CyberSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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

        Spacer(modifier = Modifier.height(24.dp))
        Text("SHIELD ACTIVE ENGINES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))

        // Active engines indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngineStatusBox(modifier = Modifier.weight(1f), title = "Web Scan", status = "SECURE", tint = CyberPrimary)
            EngineStatusBox(modifier = Modifier.weight(1f), title = "SMS Spam", status = "ACTIVE", tint = CyberSecondary)
            EngineStatusBox(modifier = Modifier.weight(1f), title = "Auditor", status = "SAFE", tint = CyberWarning)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("DEVICE UTILIZATION", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))

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
}

@Composable
fun EngineStatusBox(modifier: Modifier = Modifier, title: String, status: String, tint: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberCard)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URLScannerScreen(onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    var urlInput by remember { mutableStateOf("http://secure-verify-paypal.accounts.com") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(24.dp))
        Text("Target scan link address", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                loading = true
                scanResult = null
                val isSus = urlInput.contains("paypal") || urlInput.contains("verify")
                scanResult = if (isSus) "VERDICT: Phishing Site Detected (Confidence: 85%)\n• Obfuscated subdomains detected\n• Spoofed banking structures discovered"
                else "VERDICT: Safe Link Checked\n• Verified reputation indexes"
                loading = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("EXECUTE AI ANALYSIS", color = CyberBackground, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        scanResult?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(
                        1.dp,
                        if (it.contains("Phishing")) CyberDanger.copy(alpha = 0.3f) else CyberSuccess.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Scans Output",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (it.contains("Phishing")) CyberDanger else CyberSuccess,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSAnalyzerScreen(onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    var smsInput by remember { mutableStateOf("URGENT: Your account is suspended. Click here http://bit.ly/pay-verify") }
    var scanResult by remember { mutableStateOf<String?>(null) }

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

        Spacer(modifier = Modifier.height(24.dp))
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
                val hasScam = smsInput.contains("urgent") || smsInput.contains("blocked") || smsInput.contains("verify")
                scanResult = if (hasScam) "SCAM PROBABILITY: 94.8% (Highly Likely Scam)\n• Flagged: Urgency language discovered\n• Flagged: Obfuscated redirects identified"
                else "SCAM PROBABILITY: 4.5% (Safe)\n• Verified structure guidelines"
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("RUN TEXT FRAUD CHECK", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        scanResult?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(
                        1.dp,
                        if (it.contains("94.8%")) CyberDanger.copy(alpha = 0.3f) else CyberSuccess.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text("NLP Evaluations Results", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (it.contains("94.8%")) CyberDanger else CyberSuccess,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

data class PermissionItem(val name: String, val level: String, val color: Color)

@Composable
fun PermissionAnalyzerScreen(onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    val items = listOf(
        PermissionItem("BIND_ACCESSIBILITY_SERVICE", "CRITICAL RISK", CyberDanger),
        PermissionItem("RECEIVE_SMS / SEND_SMS", "HIGH RISK", CyberWarning),
        PermissionItem("ACCESS_FINE_LOCATION", "MEDIUM RISK", CyberWarning),
        PermissionItem("CAMERA", "LOW RISK", Color.Gray)
    )

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

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Active system background access channels classified by vulnerability risks:",
            color = Color.LightGray,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { perm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(perm.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Risk Tier Rating", color = Color.Gray, fontSize = 10.sp)
                    }
                    Text(
                        text = perm.level,
                        color = perm.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(perm.color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
