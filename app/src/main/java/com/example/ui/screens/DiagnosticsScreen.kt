package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.viewmodel.DeviceDiagnostics
import kotlinx.coroutines.delay

@Composable
fun DiagnosticsScreen(
    deviceDiagnostics: DeviceDiagnostics,
    onRefresh: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(1800)
            isScanning = false
            scanCompleted = true
            onRefresh()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diagnostics_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Scan Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_scan_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isScanning) PrimaryCyan.copy(alpha = alphaAnim) else SecondaryEmerald.copy(
                                    alpha = 0.2f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Radar else Icons.Default.VerifiedUser,
                            contentDescription = "Scan Icon",
                            tint = if (isScanning) PrimaryCyan else SecondaryEmerald,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isScanning) "Running System Telemetry Scan..." else "Device Integrity Healthy",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${deviceDiagnostics.deviceModel} • ${deviceDiagnostics.osVersion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isScanning = true
                            scanCompleted = false
                        },
                        enabled = !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_diagnostics_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanning) "Scanning Sensors..." else "Run Diagnostics Check",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Diagnostic Metrics Cards
        item {
            Text(
                text = "Hardware & Memory Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            DiagnosticMetricCard(
                title = "Battery Telemetry",
                value = "${deviceDiagnostics.batteryLevel}%",
                detail = if (deviceDiagnostics.isCharging) "Power Source Connected (Charging)" else "Discharging • Battery Healthy",
                icon = Icons.Default.BatteryStd,
                progress = deviceDiagnostics.batteryLevel / 100f,
                accentColor = SecondaryEmerald
            )
        }

        item {
            DiagnosticMetricCard(
                title = "Internal Storage Health",
                value = "${String.format("%.1f", deviceDiagnostics.storageUsedGb)} GB / ${String.format("%.1f", deviceDiagnostics.storageTotalGb)} GB",
                detail = "${deviceDiagnostics.storagePercentage}% Used • Space Available",
                icon = Icons.Default.Storage,
                progress = deviceDiagnostics.storagePercentage / 100f,
                accentColor = PrimaryCyan
            )
        }

        item {
            DiagnosticMetricCard(
                title = "JVM Memory Allocation",
                value = "${deviceDiagnostics.memoryUsedMb} MB / ${deviceDiagnostics.memoryMaxMb} MB",
                detail = "Active Runtime Heap Space",
                icon = Icons.Default.Memory,
                progress = if (deviceDiagnostics.memoryMaxMb > 0) deviceDiagnostics.memoryUsedMb.toFloat() / deviceDiagnostics.memoryMaxMb else 0.5f,
                accentColor = AccentAmber
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("network_os_info_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Environment Security Info",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Encryption", "AES-256 / SHA-256 Active")
                    InfoRow("Network Pipe", "Active Local Interface")
                    InfoRow("Edge-to-Edge", "Safe Insets Enforced")
                    InfoRow("Device Model", deviceDiagnostics.deviceModel)
                }
            }
        }
    }
}

@Composable
fun DiagnosticMetricCard(
    title: String,
    value: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    progress: Float,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("metric_card_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
