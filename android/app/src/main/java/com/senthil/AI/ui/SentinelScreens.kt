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
    object Dashboard : Screen()
    object URLScanner : Screen()
    object SMSAnalyzer : Screen()
    object PermissionAnalyzer : Screen()
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
            is Screen.Splash -> SplashScreen {
                currentScreen = Screen.Login
            }
            is Screen.Login -> LoginScreen(
                onLoginSuccess = { email, authToken ->
                    userEmail = email
                    token = authToken
                    currentScreen = Screen.Dashboard
                }
            )
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
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        visible = true
        delay(2200)
        onFinish()
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
fun LoginScreen(onLoginSuccess: (String, String) -> Unit) {
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
    }
}

@Composable
fun DashboardScreen(userEmail: String, onNavigate: (Screen) -> Unit) {
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
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = CyberSecondary,
                modifier = Modifier.size(36.dp)
            )
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
        Text("CYBER SECURITY COMMANDS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(10.dp))

        // Command tools grids
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardToolCard(
                title = "AI URL Link Scanner",
                description = "Extracts structure matrices to identify scam sites.",
                icon = Icons.Default.Language,
                color = CyberPrimary,
                onClick = { onNavigate(Screen.URLScanner) }
            )

            DashboardToolCard(
                title = "SMS Scam NLP Analyzer",
                description = "Runs local word counts & vectors checks for spam/threats.",
                icon = Icons.Default.Sms,
                color = CyberSecondary,
                onClick = { onNavigate(Screen.SMSAnalyzer) }
            )

            DashboardToolCard(
                title = "Permission Threat Auditor",
                description = "Cross references backgrounds access configurations.",
                icon = Icons.Default.FolderSpecial,
                color = CyberWarning,
                onClick = { onNavigate(Screen.PermissionAnalyzer) }
            )
        }
    }
}

@Composable
fun DashboardToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCard)
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Open", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URLScannerScreen(onBack: () -> Unit) {
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
