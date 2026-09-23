package com.senthil.AI.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Beautiful Dark theme color palette for Jetpack Compose matching Sentinel AI design system
val CyberBackground = Color(0xFF050816)
val CyberCard = Color(0xFF0F172A)
val CyberCardBorder = Color(0xFF1E293B)
val CyberPrimary = Color(0xFF00E5FF)
val CyberSecondary = Color(0xFF7C3AED)
val CyberSuccess = Color(0xFF00FF88)
val CyberWarning = Color(0xFFFFB020)
val CyberDanger = Color(0xFFFF4D4D)
val CyberTextMuted = Color(0xFF94A3B8)

@Composable
fun CyberBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StandardResultCard(
    title: String,
    risk: String,
    confidence: Float,
    whatDetected: String,
    whyItMatters: String,
    evidence: List<String>,
    recommendedAction: String,
    actionButtonText: String = "Take Action",
    onActionClick: () -> Unit = {},
    modelInfo: String = "Sentinel Cyber Intelligence Engine v2.5"
) {
    val badgeColor = when (risk.lowercase()) {
        "critical", "high threat" -> CyberDanger
        "high", "suspicious" -> CyberWarning
        "medium" -> CyberWarning
        "low", "safe" -> CyberSuccess
        else -> CyberPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                CyberBadge(text = risk.uppercase(), color = badgeColor)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Confidence: ${"%.1f".format(confidence)}% | Model: $modelInfo",
                color = CyberTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "WHAT WAS DETECTED", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = whatDetected, color = Color.White, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "WHY IT MATTERS", color = CyberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = whyItMatters, color = CyberTextMuted, fontSize = 12.sp)

            if (evidence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "EVIDENCE & SIGNALS", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                evidence.forEach { sig ->
                    Text(text = "• $sig", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "RECOMMENDED ACTION", color = CyberSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = recommendedAction, color = Color.White, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = actionButtonText, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
