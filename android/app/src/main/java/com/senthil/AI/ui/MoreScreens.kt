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
import com.senthil.AI.data.*

// ============================================================================
// MORE HUB (Tab 5)
// Contents: Alerts, Incident Center, History, Emergency Mode, Settings, Enterprise
// ============================================================================

@Composable
fun MoreHubScreen(
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "OPERATIONS & CONTROLS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Threat intelligence inboxes, emergency recovery & system administration",
                    color = CyberTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Emergency Response Highlight Card (Screen 22)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.EmergencyMode) },
                colors = CardDefaults.cardColors(containerColor = CyberDanger.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberDanger))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CyberDanger, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "EMERGENCY COMPROMISE MODE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Suspect device hack or stolen credentials? Run rapid 7-step lockdown.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberDanger)
                }
            }
        }

        // Operations List
        val moreItems = listOf(
            Triple("Security Alerts Inbox", "Central notification stream & required actions", Screen.SecurityAlerts),
            Triple("Incident Center", "Grouped security incidents & multi-vector evidence", Screen.IncidentCenter),
            Triple("Security History", "Audit logs of previous scans & assessments", Screen.SecurityHistory),
            Triple("Notification Settings", "Configure alert channels & trigger thresholds", Screen.NotificationsCenter),
            Triple("Application Settings", "Local data retention, scan schedule & about", Screen.Settings),
            Triple("Account & Subscription", "Manage device licensing & subscription state", Screen.AccountSubscription),
            Triple("Business & Enterprise MDM", "Organization enrollment & compliance policy", Screen.EnterpriseEnrollment),
            Triple("Enterprise Dashboard", "SOC overview, device fleet management & audit logs", Screen.EnterpriseDashboard)
        )

        items(moreItems) { (title, desc, targetScreen) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(targetScreen) },
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = desc, color = CyberTextMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberTextMuted)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = CyberDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Session", color = CyberDanger, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================================
// SCREEN 18: SECURITY ALERTS INBOX
// Purpose: Central inbox for security notifications with severity filters.
// ============================================================================

@Composable
fun SecurityAlertsScreen(
    token: String,
    onBack: () -> Unit
) {
    var alertsList by remember {
        mutableStateOf(
            listOf(
                AlertItem("ALT-101", "Critical", "Rogue Device Administrator Detected", "Application 'MediaOptimizer' requested BIND_DEVICE_ADMIN privileges.", "Revoke in Settings > Security > Device Admin.", false, "10 mins ago"),
                AlertItem("ALT-102", "High", "Unencrypted Public Wi-Fi Active", "Connected to open SSID 'FreeAirportWiFi' without WPA2 encryption.", "Enable Sentinel VPN.", false, "1 hour ago"),
                AlertItem("ALT-103", "Medium", "USB Debugging Enabled", "ADB bridge is active on device.", "Turn off in Developer Options.", true, "Yesterday"),
                AlertItem("ALT-104", "Low", "Security Patch 60 Days Old", "Your device security patch is aging.", "Check for system update.", true, "3 days ago")
            )
        )
    }
    var selectedFilter by remember { mutableStateOf("All") }

    val filtered = alertsList.filter { a ->
        selectedFilter == "All" || a.severity.equals(selectedFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Security Alerts Inbox", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Critical", "High", "Medium", "Low").forEach { f ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(f, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyberPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { alert ->
                val badgeColor = when (alert.severity) {
                    "Critical" -> CyberDanger
                    "High" -> CyberWarning
                    "Medium" -> CyberPrimary
                    else -> CyberSuccess
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(text = alert.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            CyberBadge(text = alert.severity.uppercase(), color = badgeColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = alert.summary, color = CyberTextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Action: ${alert.action_required}", color = CyberSuccess, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = alert.timestamp, color = CyberTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 19: INCIDENT CENTER
// Purpose: Group related findings into security incidents.
// ============================================================================

@Composable
fun IncidentCenterScreen(
    onBack: () -> Unit,
    onOpenTimeline: (String) -> Unit
) {
    val incidents = remember {
        listOf(
            IncidentItem(
                incident_id = "INC-2026-001",
                title = "Suspected Banking Trojan & Smishing Attack",
                severity = "Critical",
                confidence = 98.2f,
                affected_asset = "Samsung Galaxy S24",
                status = "In Progress",
                timeline = emptyList(),
                evidence = listOf("BIND_ACCESSIBILITY_SERVICE active", "C2 connection attempted"),
                recommended_actions = listOf("Revoke Accessibility", "Boot into Safe Mode")
            ),
            IncidentItem(
                incident_id = "INC-2026-002",
                title = "Credential Harvester Smishing Intercepted",
                severity = "High",
                confidence = 99.1f,
                affected_asset = "SMS Gateway",
                status = "Resolved",
                timeline = emptyList(),
                evidence = listOf("Deceptive .top TLD", "Impersonating Chase Bank"),
                recommended_actions = listOf("Block sender 7726")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Incident Center", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "MULTI-VECTOR SECURITY INCIDENTS", color = CyberTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(incidents) { inc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTimeline(inc.incident_id) },
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = inc.incident_id, color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            CyberBadge(text = inc.severity.uppercase(), color = if (inc.severity == "Critical") CyberDanger else CyberWarning)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = inc.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Target: ${inc.affected_asset} | Status: ${inc.status}", color = CyberTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Inspect Attack Timeline & Evidence", color = CyberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 20: INCIDENT TIMELINE
// Purpose: Present a chronological view of evidence and actions.
// ============================================================================

@Composable
fun IncidentTimelineScreen(
    incidentId: String,
    onBack: () -> Unit
) {
    val timelineEvents = listOf(
        TimelineEvent("10:14:22", "App Auditor", "Detected application requesting BIND_ACCESSIBILITY_SERVICE.", "High"),
        TimelineEvent("10:14:35", "Network Shield", "Connection attempted to C2 host http://89.208.107.123:8080.", "Critical"),
        TimelineEvent("10:15:00", "Sentinel Local AI", "Automated defensive advisory issued to isolate device.", "Informational")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Timeline: $incidentId", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(timelineEvents) { ev ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (ev.severity == "Critical") CyberDanger else CyberPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(50.dp)
                                .background(CyberCardBorder)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = ev.source, color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = ev.timestamp, color = CyberTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = ev.event, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 21: SECURITY HISTORY
// Purpose: Let the user review previous scans and security events.
// ============================================================================

@Composable
fun SecurityHistoryScreen(
    token: String? = null,
    userEmail: String? = null,
    onBack: () -> Unit,
    onNavigateToScan: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var historyList by remember { mutableStateOf<List<SecurityHistoryItem>>(emptyList()) }
    var isSyncing by remember { mutableStateOf(false) }

    val effectiveEmail = remember(userEmail) {
        if (!userEmail.isNullOrBlank()) userEmail.trim().lowercase() else SecurityHistoryManager.getCachedEmail(context)
    }
    val effectiveToken = remember(token) {
        if (!token.isNullOrBlank()) token else SecurityHistoryManager.getCachedToken(context)
    }

    // High-speed real-time polling to keep scan history 100% synchronized with Web
    LaunchedEffect(effectiveEmail, effectiveToken) {
        historyList = SecurityHistoryManager.getHistory(context)
        var isFirst = true
        while (isActive) {
            if (isFirst) isSyncing = true
            try {
                historyList = SecurityHistoryManager.fetchAndSyncWithBackend(context, effectiveToken, effectiveEmail)
            } catch (e: Exception) {
                // Keep local cache on intermittent connection
            } finally {
                if (isFirst) {
                    isSyncing = false
                    isFirst = false
                }
            }
            delay(3000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Security Scan History", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isSyncing) "Syncing with cloud..." else "${historyList.size} synchronized security events",
                    color = if (isSyncing) CyberPrimary else CyberTextMuted,
                    fontSize = 11.sp
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        isSyncing = true
                        historyList = SecurityHistoryManager.fetchAndSyncWithBackend(context, token, userEmail)
                        isSyncing = false
                    }
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberPrimary)
            }
            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = {
                        historyList = emptyList()
                        coroutineScope.launch {
                            SecurityHistoryManager.clearAllHistory(context, token, userEmail)
                        }
                    }
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All History", tint = CyberDanger)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty() && !isSyncing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CyberCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "No Scan History Recorded", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Live security events will be logged here whenever you run a Quick Scan, URL check, QR Code scanner, Payment verification, or App audit. All scans sync across your mobile and web dashboard.",
                        color = CyberTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToScan,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Security Scanner", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(historyList, key = { it.id }) { item ->
                    val badgeColor = when (item.severity.lowercase()) {
                        "critical", "high" -> CyberDanger
                        "medium" -> CyberWarning
                        else -> CyberSuccess
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when {
                                            item.scanType.contains("QR", ignoreCase = true) -> Icons.Default.QrCodeScanner
                                            item.scanType.contains("Payment", ignoreCase = true) -> Icons.Default.ReceiptLong
                                            item.scanType.contains("URL", ignoreCase = true) -> Icons.Default.Link
                                            item.scanType.contains("SMS", ignoreCase = true) -> Icons.Default.Sms
                                            item.scanType.contains("App", ignoreCase = true) -> Icons.Default.Apps
                                            else -> Icons.Default.Security
                                        },
                                        contentDescription = null,
                                        tint = CyberPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = item.scanType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CyberBadge(text = item.severity.uppercase(), color = badgeColor)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            val delId = item.id
                                            historyList = historyList.filter { it.id != delId }
                                            coroutineScope.launch {
                                                SecurityHistoryManager.deleteScanItem(context, delId, token, userEmail)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete item",
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.target,
                                color = CyberTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.verdict, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = item.timestamp, color = CyberTextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 22: EMERGENCY MODE
// Purpose: Guide a user who believes the device or account may be compromised.
// ============================================================================

@Composable
fun EmergencyModeScreen(
    onBack: () -> Unit,
    onActionClick: (String) -> Unit
) {
    val steps = listOf(
        ChecklistStep(1, "Air-Gap Device (Airplane Mode)", "Sever Wi-Fi, Bluetooth, and cellular data to stop data exfiltration.", "Immediate"),
        ChecklistStep(2, "Revoke Accessibility Services", "Inspect Android Settings > Accessibility and turn off all non-system services.", "Critical"),
        ChecklistStep(3, "Revoke Device Administrators", "Remove administrator rights from unfamiliar apps in Security Settings.", "Critical"),
        ChecklistStep(4, "Freeze Banking & UPI Cards", "Call your bank from a secondary phone to lock credit/debit cards and netbanking.", "Urgent"),
        ChecklistStep(5, "Audit Call Forwarding (*#21#)", "Dial *#21# on keypad to verify calls and OTPs are not being redirected.", "High"),
        ChecklistStep(6, "Boot Android into Safe Mode", "Hold power button, long-press 'Power Off' and tap 'Safe Mode' to remove persistent droppers.", "High"),
        ChecklistStep(7, "File Cyber Crime Complaint", "Preserve screenshots and lodge official complaint (1930 / cybercrime.gov.in).", "Legal")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Emergency Recovery Protocol", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberDanger.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberDanger))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "ACTIVE COMPROMISE SUSPECTED", color = CyberDanger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Execute the following containment steps sequentially to prevent ongoing financial and data loss.", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(steps) { st ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberDanger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${st.step_number}", color = CyberDanger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = st.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = st.description, color = CyberTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 24: NOTIFICATIONS CENTER
// Purpose: Manage SentinelAI notification categories.
// ============================================================================

@Composable
fun NotificationsCenterScreen(
    onBack: () -> Unit
) {
    var threatAlerts by remember { mutableStateOf(true) }
    var scanCompletion by remember { mutableStateOf(true) }
    var riskChanges by remember { mutableStateOf(true) }
    var modelUpdates by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Notification Preferences", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val options = listOf(
            Triple("Critical Threat Alerts", "Immediate alerts on detected trojans or phishing URLs", threatAlerts to { threatAlerts = !threatAlerts }),
            Triple("Scan Completion", "Notify when Full Scan or Quick Scan finishes", scanCompletion to { scanCompletion = !scanCompletion }),
            Triple("Device Risk Changes", "Alert when new dangerous permissions are granted", riskChanges to { riskChanges = !riskChanges }),
            Triple("Model Intelligence Updates", "Notify when local ML threat matrices are updated", modelUpdates to { modelUpdates = !modelUpdates })
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(options) { (title, subtitle, statePair) ->
                val (checked, toggle) = statePair
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = subtitle, color = CyberTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = checked,
                            onCheckedChange = { toggle() },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 25: SETTINGS
// Purpose: Control app behavior, privacy and security.
// ============================================================================

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAndroidPermissions: () -> Unit
) {
    var offlineMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Sentinel Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
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
                        Column {
                            Text(text = "Strict Offline Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Disable all external network calls; run 100% on local models", color = CyberTextMuted, fontSize = 11.sp)
                        }
                        Switch(checked = offlineMode, onCheckedChange = { offlineMode = it })
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAndroidPermissions),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "App System Permissions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Manage storage and phone permissions in Android Settings", color = CyberTextMuted, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyberTextMuted)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "SentinelAI Mobile v1.0.0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Self-hosted AI cybersecurity engine. No generative-AI cloud APIs.", color = CyberTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 27: ACCOUNT & SUBSCRIPTION
// Purpose: Manage cloud and enterprise licensing.
// ============================================================================

@Composable
fun AccountSubscriptionScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Account & Subscription", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CURRENT PLAN", color = CyberPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    CyberBadge(text = "ENTERPRISE", color = CyberSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Sentinel Defense Enterprise Fleet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Active Devices: 1 / 10 Licensed", color = CyberTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Includes real-time local LLM inference, 42-feature URL classification, and MDM policy enforcement.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

// ============================================================================
// SCREEN 28: BUSINESS / ENTERPRISE ENROLLMENT
// Purpose: Enroll a device into an organization with authorized admin flow.
// ============================================================================

@Composable
fun EnterpriseEnrollmentScreen(
    onBack: () -> Unit
) {
    var orgId by remember { mutableStateOf("ORG-SEC-9920") }
    var enrollKey by remember { mutableStateOf("••••••••••••") }
    var isEnrolled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Enterprise Device Enrollment", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Enrolled in Organization", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Sentinel Defense Labs Global SOC", color = CyberSuccess, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Organization ID: $orgId", color = CyberTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Device ID: DEV-AND-S24-9102", color = CyberTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Policy Sync: Compliant (v3.2.0-enforced)", color = CyberSuccess, fontSize = 11.sp)
            }
        }
    }
}

// ============================================================================
// SECTION 9: ENTERPRISE DASHBOARD & AUDIT LOGS
// ============================================================================

@Composable
fun EnterpriseDashboardScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Organization Fleet SOC", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "FLEET DEVICES", color = CyberTextMuted, fontSize = 10.sp)
                    Text(text = "248", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "100% Compliant", color = CyberSuccess, fontSize = 10.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "THREATS BLOCKED", color = CyberTextMuted, fontSize = 10.sp)
                    Text(text = "1,429", color = CyberPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Zero Day Phishing", color = CyberTextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "SOC AUDIT LOG STREAM", color = CyberTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        val logs = listOf(
            Triple("Policy Enforcement Sync", "Device DEV-9102 synced policy v3.2", "08:30 AM"),
            Triple("Smishing Attempt Neutralized", "URL 'http://chase-login.xyz' blocked on endpoint", "07:15 AM"),
            Triple("Integrity Check Passed", "SELinux enforcing & zero root binaries confirmed", "Yesterday")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(logs) { (event, detail, time) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = event, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = time, color = CyberTextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = detail, color = CyberTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
