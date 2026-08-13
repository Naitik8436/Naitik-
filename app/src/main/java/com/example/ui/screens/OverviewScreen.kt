package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.LogLevel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.viewmodel.DashboardTab
import com.example.ui.viewmodel.DeviceDiagnostics
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OverviewScreen(
    vaultCount: Int,
    pendingTaskCount: Int,
    deviceDiagnostics: DeviceDiagnostics,
    auditLogs: List<AuditLog>,
    onNavigateTab: (DashboardTab) -> Unit,
    onLockClicked: () -> Unit,
    isExpandedScreen: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("overview_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            ExecutiveHeaderBanner(
                deviceDiagnostics = deviceDiagnostics,
                onLockClicked = onLockClicked
            )
        }

        // Top KPI Cards Grid
        item {
            if (isExpandedScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Vault Items",
                        value = "$vaultCount",
                        subtitle = "Encrypted Records",
                        icon = Icons.Default.VpnKey,
                        accentColor = PrimaryCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(DashboardTab.VAULT) }
                    )
                    KpiCard(
                        title = "Active Tasks",
                        value = "$pendingTaskCount",
                        subtitle = "Pending Execution",
                        icon = Icons.Default.CheckCircle,
                        accentColor = AccentAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(DashboardTab.TASKS) }
                    )
                    KpiCard(
                        title = "Battery & Health",
                        value = "${deviceDiagnostics.batteryLevel}%",
                        subtitle = if (deviceDiagnostics.isCharging) "Charging" else "Healthy",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = SecondaryEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(DashboardTab.DIAGNOSTICS) }
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "Vault Secrets",
                            value = "$vaultCount",
                            subtitle = "Encrypted",
                            icon = Icons.Default.VpnKey,
                            accentColor = PrimaryCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab(DashboardTab.VAULT) }
                        )
                        KpiCard(
                            title = "Pending Tasks",
                            value = "$pendingTaskCount",
                            subtitle = "To Do / Active",
                            icon = Icons.Default.CheckCircle,
                            accentColor = AccentAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab(DashboardTab.TASKS) }
                        )
                    }
                    KpiCard(
                        title = "System Diagnostics",
                        value = "${deviceDiagnostics.batteryLevel}% Power",
                        subtitle = "Storage ${deviceDiagnostics.storagePercentage}% Used | ${deviceDiagnostics.deviceModel}",
                        icon = Icons.Default.Speed,
                        accentColor = SecondaryEmerald,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateTab(DashboardTab.DIAGNOSTICS) }
                    )
                }
            }
        }

        // Live Telemetry Activity Graph
        item {
            TelemetryChartCard()
        }

        // Quick Actions Grid
        item {
            QuickActionsBar(
                onNavigateTab = onNavigateTab,
                onLockClicked = onLockClicked
            )
        }

        // Audit Logs Timeline Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Security Audit Stream",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time Event Log",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Recent Audit Logs Items
        items(auditLogs.take(5)) { log ->
            AuditLogItemRow(log = log)
        }
    }
}

@Composable
fun ExecutiveHeaderBanner(
    deviceDiagnostics: DeviceDiagnostics,
    onLockClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("executive_header_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryCyan.copy(alpha = 0.15f),
                            PrimaryBlue.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SecondaryEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM ACTIVE & PROTECTED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = SecondaryEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Executive Workspace",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${deviceDiagnostics.deviceModel} • ${deviceDiagnostics.osVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onLockClicked,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                        .testTag("quick_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Session",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("kpi_card_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TelemetryChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Telemetry Index",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Memory & CPU Load Dynamics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = PrimaryCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Smooth Canvas Curve
            val lineColor = PrimaryCyan
            val gradientBottom = PrimaryCyan.copy(alpha = 0.05f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height

                val points = listOf(
                    Offset(0f, height * 0.7f),
                    Offset(width * 0.2f, height * 0.4f),
                    Offset(width * 0.4f, height * 0.6f),
                    Offset(width * 0.6f, height * 0.25f),
                    Offset(width * 0.8f, height * 0.5f),
                    Offset(width, height * 0.2f)
                )

                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val control1 = Offset(prev.x + (curr.x - prev.x) / 2, prev.y)
                        val control2 = Offset(prev.x + (curr.x - prev.x) / 2, curr.y)
                        cubicTo(control1.x, control1.y, control2.x, control2.y, curr.x, curr.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), gradientBottom)
                    )
                )

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Point Dots
                for (p in points) {
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = p
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsBar(
    onNavigateTab: (DashboardTab) -> Unit,
    onLockClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(
                label = "Vault",
                icon = Icons.Default.VpnKey,
                onClick = { onNavigateTab(DashboardTab.VAULT) }
            )
            QuickActionButton(
                label = "Tasks",
                icon = Icons.Default.AddTask,
                onClick = { onNavigateTab(DashboardTab.TASKS) }
            )
            QuickActionButton(
                label = "Scan",
                icon = Icons.Default.Radar,
                onClick = { onNavigateTab(DashboardTab.DIAGNOSTICS) }
            )
            QuickActionButton(
                label = "Lock",
                icon = Icons.Default.Lock,
                onClick = onLockClicked
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryCyan
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AuditLogItemRow(log: AuditLog) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

    val (badgeColor, icon) = when (log.level) {
        LogLevel.SUCCESS -> SecondaryEmerald to Icons.Default.CheckCircle
        LogLevel.WARNING -> AccentAmber to Icons.Default.Warning
        LogLevel.SECURITY -> MaterialTheme.colorScheme.error to Icons.Default.Security
        LogLevel.INFO -> PrimaryCyan to Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_row_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = log.level.name,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
