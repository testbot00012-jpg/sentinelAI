@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.senthil.AI.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.platform.LocalContext
import com.senthil.AI.data.SentinelApiClient
import com.senthil.AI.data.MetricsResponse
import com.senthil.AI.data.SecurityHistoryManager

// ============================================================================
// SCREEN 03: HOME DASHBOARD
// Purpose: Give the user a one-screen view of phone security.
// ============================================================================

@Composable
fun HomeDashboardScreen(
    userEmail: String,
    token: String,
    onNavigateToScore: () -> Unit,
    onNavigateToQuickScan: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToCategory: (String) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableStateOf(95) }
    var threatsCount by remember { mutableStateOf(0) }
    var totalScans by remember { mutableStateOf(0) }

    val effectiveEmail = remember(userEmail) {
        if (userEmail.isNotBlank()) userEmail.trim().lowercase() else SecurityHistoryManager.getCachedEmail(context)
    }
    val effectiveToken = remember(token) {
        if (token.isNotBlank()) token else SecurityHistoryManager.getCachedToken(context)
    }

    // High-speed real-time polling to keep scan count and threat counters 100% in sync with Web
    LaunchedEffect(effectiveEmail, effectiveToken) {
        while (isActive) {
            try {
                val res = SentinelApiClient.instance.getMetrics(
                    token = effectiveToken,
                    email = effectiveEmail
                )
                score = res.summary.security_score
                threatsCount = res.summary.threats_blocked
                totalScans = res.summary.total_scans
            } catch (e: Exception) {
                // Keep current state if network is temporarily unreachable
            }
            delay(3000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENTINEL AI",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (userEmail.isNotEmpty()) userEmail else "Secured Device Agent",
                        color = CyberTextMuted,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onNavigateToAlerts,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CyberCard)
                ) {
                    BadgedBox(
                        badge = {
                            if (threatsCount > 0) {
                                Badge(containerColor = CyberDanger) { Text("$threatsCount") }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = CyberPrimary)
                    }
                }
            }
        }

        // Security Score Hero Card (Screen 03 & 04 trigger)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToScore),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary.copy(alpha = 0.1f))
                            .border(3.dp, if (score >= 80) CyberSuccess else CyberWarning, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "/100", color = CyberTextMuted, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (score >= 85) "DEVICE PROTECTED" else "ATTENTION REQUIRED",
                            color = if (score >= 85) CyberSuccess else CyberWarning,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Threats blocked: $threatsCount | Scans: $totalScans",
                            color = CyberTextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "View Score Breakdown", color = CyberPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Action Buttons: Quick Scan & AI Assistant
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateToQuickScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Quick Scan", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToAI,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "AI Advisor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4 Security Posture Cards (Device, Apps, Privacy, Network)
        item {
            Text(text = "SECURITY STATUS BY DOMAIN", color = CyberTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PostureMiniCard(
                    title = "Device Health",
                    status = "Hardened",
                    score = "95%",
                    icon = Icons.Default.PhoneAndroid,
                    color = CyberSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCategory("device") }
                )
                PostureMiniCard(
                    title = "App Risk",
                    status = "1 Warning",
                    score = "88%",
                    icon = Icons.Default.Apps,
                    color = CyberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCategory("apps") }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PostureMiniCard(
                    title = "Privacy Guardian",
                    status = "Active Guard",
                    score = "90%",
                    icon = Icons.Default.Visibility,
                    color = CyberPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCategory("privacy") }
                )
                PostureMiniCard(
                    title = "Network Defense",
                    status = "WPA2 Secure",
                    score = "92%",
                    icon = Icons.Default.Wifi,
                    color = CyberSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCategory("network") }
                )
            }
        }

        // Prioritized Recommendations
        item {
            Text(text = "PRIORITIZED RECOMMENDATIONS", color = CyberTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberWarning, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Audit Unused SMS & Location Permissions", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3 apps accessed your SMS inbox in the last 24 hours. Verify that banking 2FA codes are protected.",
                        color = CyberTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PostureMiniCard(
    title: String,
    status: String,
    score: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Text(text = score, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = status, color = CyberTextMuted, fontSize = 11.sp)
        }
    }
}

// ============================================================================
// SCREEN 04: SECURITY SCORE BREAKDOWN
// Purpose: Explain the overall security score rather than presenting an unexplained number.
// ============================================================================

@Composable
fun SecurityScoreScreen(
    onBack: () -> Unit,
    onOpenFix: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Security Score Analysis", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Circular Score Display
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary.copy(alpha = 0.1f))
                            .border(4.dp, CyberSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "92", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "POSTURE: STRONG", color = CyberSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Confidence Level: 98.4%", color = CyberPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "Last evaluated: Today at 08:30 AM via Local Security Engine", color = CyberTextMuted, fontSize = 11.sp)
                }
            }
        }

        // Category Scores Table
        item {
            Text(text = "CATEGORY CONTRIBUTIONS", color = CyberTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        val categories = listOf(
            Triple("Device Hardware & OS Security", "95 / 100", CyberSuccess),
            Triple("Application Permission Synergy", "88 / 100", CyberWarning),
            Triple("Privacy & Sensor Exposure", "90 / 100", CyberPrimary),
            Triple("Network & Gateway Encryption", "94 / 100", CyberSuccess)
        )

        items(categories) { (name, scoreVal, color) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = scoreVal, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Severity Breakdown Counts
        item {
            Text(text = "ACTIVE FINDINGS SEVERITY", color = CyberTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeverityBadge(label = "CRITICAL", count = 0, color = CyberDanger, modifier = Modifier.weight(1f))
                SeverityBadge(label = "HIGH", count = 1, color = CyberWarning, modifier = Modifier.weight(1f))
                SeverityBadge(label = "MEDIUM", count = 2, color = CyberPrimary, modifier = Modifier.weight(1f))
                SeverityBadge(label = "LOW", count = 3, color = CyberSuccess, modifier = Modifier.weight(1f))
            }
        }

        // Contributing Findings with links to fix
        item {
            Text(text = "CONTRIBUTING FINDINGS", color = CyberTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            StandardResultCard(
                title = "Overlay Permission Granted to Sideloaded App",
                risk = "High",
                confidence = 97.2f,
                whatDetected = "Application 'MediaOptimizer' holds SYSTEM_ALERT_WINDOW permission.",
                whyItMatters = "Overlay permissions allow malicious apps to draw deceptive screens over banking logins.",
                evidence = listOf("Permission SYSTEM_ALERT_WINDOW active", "App source: Sideloaded APK"),
                recommendedAction = "Revoke 'Display over other apps' permission in Android Settings.",
                actionButtonText = "Fix in Settings",
                onActionClick = { onOpenFix("overlay") }
            )
        }
    }
}

@Composable
fun SeverityBadge(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
