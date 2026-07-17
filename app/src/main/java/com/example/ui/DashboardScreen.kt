package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ThreatLog
import com.example.viewmodel.GuardianViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: GuardianViewModel) {
    val isScanning by viewModel.isScanning.collectAsState()
    val riskScore by viewModel.riskScore.collectAsState()
    val threatLogs by viewModel.threatLogs.collectAsState()

    val micMonitored by viewModel.micMonitoringEnabled.collectAsState()
    val camMonitored by viewModel.camMonitoringEnabled.collectAsState()
    val locMonitored by viewModel.locationMonitoringEnabled.collectAsState()
    val overlayAlertsEnabled by viewModel.screenOverlayAlertsEnabled.collectAsState()

    var showSimulatedOverlayDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Guardian Protection Shield",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Active Cyber Guard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "24/7 Shielding is guarding your camera, mic, and personal vault.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Circular Threat Risk Gauge Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VULNERABILITY INDEX",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        // Scan Pulse Ring Animation
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        if (isScanning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            )
                        }

                        // Circular Progress Indicator
                        val animatedProgress by animateFloatAsState(
                            targetValue = riskScore.toFloat() / 100f,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing),
                            label = "riskProgress"
                        )

                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(140.dp),
                            color = when {
                                riskScore < 20 -> MaterialTheme.colorScheme.primary
                                riskScore < 45 -> Color(0xFFFFAA00)
                                else -> MaterialTheme.colorScheme.error
                            },
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$riskScore%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    riskScore < 20 -> "HARDENED"
                                    riskScore < 45 -> "MODERATE"
                                    else -> "AT RISK"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    riskScore < 20 -> MaterialTheme.colorScheme.primary
                                    riskScore < 45 -> Color(0xFFFFAA00)
                                    else -> MaterialTheme.colorScheme.error
                                },
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.runFullSecurityScan() },
                        enabled = !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("scan_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("SCANNING CORE SYSTEM...")
                        } else {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Security Scanner")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUN HARDENING AUDIT")
                        }
                    }
                }
            }
        }

        // Active Monitor Controls
        item {
            Text(
                text = "ACTIVE INTERCEPT SHIELDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Mic Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Microphone Protection",
                                tint = if (micMonitored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Microphone Sentinel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Intercepts rogue background records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = micMonitored,
                            onCheckedChange = { viewModel.micMonitoringEnabled.value = it },
                            modifier = Modifier.testTag("mic_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Camera Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera Shield",
                                tint = if (camMonitored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Camera Guard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Locks lens access of unverified APIs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = camMonitored,
                            onCheckedChange = { viewModel.camMonitoringEnabled.value = it },
                            modifier = Modifier.testTag("camera_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Location Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Location Access Tracking",
                                tint = if (locMonitored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Fuzzy Geofencing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("De-coordinates exact GPS pins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = locMonitored,
                            onCheckedChange = { viewModel.locationMonitoringEnabled.value = it },
                            modifier = Modifier.testTag("location_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Overlay Protection Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Screen Overlay Defeat",
                                tint = if (overlayAlertsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Overlay Interdiction", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Blocks phantom touch overlays", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = overlayAlertsEnabled,
                            onCheckedChange = { viewModel.screenOverlayAlertsEnabled.value = it },
                            modifier = Modifier.testTag("overlay_toggle")
                        )
                    }
                }
            }
        }

        // Action: Simulate Screen Overlay Threat Detection
        item {
            OutlinedButton(
                onClick = { showSimulatedOverlayDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("overlay_simulator_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.WarningAmber, contentDescription = "Warning")
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEST SCREEN OVERLAY DEFENSE")
            }
        }

        // Threat Logs Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEFENSE SHIELD LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                if (threatLogs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllThreatLogs() }) {
                        Text("Clear logs", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Logs listing
        if (threatLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No threats detected. Secure Shield is reporting healthy status.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(threatLogs) { log ->
                ThreatLogCard(log)
            }
        }
    }

    // Custom overlay simulated attack popup dialog
    if (showSimulatedOverlayDialog) {
        AlertDialog(
            onDismissRequest = { showSimulatedOverlayDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Threat Warning Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text("Threat Mitigated: Phantom Overlay Detected", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "An untrusted package tried to render a transparent window overlay over your active biometric login gate.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Action: Interdiction Lockout applied.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Origin: Simulated Overlay (com.test.malware_overlay)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSimulatedOverlayDialog = false
                        viewModel.sendMessage("I experienced an overlay attack. Can you check my log details?")
                    }
                ) {
                    Text("ASK GUARDIAN AI TO DEBRIEF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulatedOverlayDialog = false }) {
                    Text("DISMISS")
                }
            }
        )
    }
}

@Composable
fun ThreatLogCard(log: ThreatLog) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { sdf.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when (log.riskLevel) {
                            "CRITICAL" -> MaterialTheme.colorScheme.error
                            "WARNING" -> Color(0xFFFFAA00)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.threatType,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (log.riskLevel) {
                            "CRITICAL" -> MaterialTheme.colorScheme.error
                            "WARNING" -> Color(0xFFD48800)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Extension function for scale animation
fun Modifier.scale(scale: Float): Modifier = this.then(
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)

private fun graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier {
    return Modifier.graphicsLayer(block)
}
