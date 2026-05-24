package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.DnsProvider
import com.example.network.TunnelState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WarpDashboardScreen(
    viewModel: WarpViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val traceData by viewModel.detectedTrace.collectAsStateWithLifecycle()
    val logs by viewModel.logsHistory.collectAsStateWithLifecycle()
    val stage by viewModel.benchmarkStage.collectAsStateWithLifecycle()
    val progress by viewModel.benchmarkProgress.collectAsStateWithLifecycle()
    val statusText by viewModel.currentStatusText.collectAsStateWithLifecycle()
    val liveSpeed by viewModel.liveSpeed.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Formatting date
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header styled with Geometric Balance branding
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Warp4K",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Cloud Engine v4.2",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSecondary,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        Toast.makeText(context, "Geometric Balance Profile", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // The central, beautiful Warp connection state selector and connection button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Connection status text
                Text(
                    text = when (connectionState) {
                        TunnelState.DISCONNECTED -> "WARP4K IS DISCONNECTED"
                        TunnelState.CONNECTING -> "ESTABLISHING SECURE HANDSHAKE..."
                        TunnelState.CONNECTED -> "WARP4K ACTIVE & BYPASS ENABLED"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = when (connectionState) {
                        TunnelState.DISCONNECTED -> Color.Gray
                        TunnelState.CONNECTING -> MaterialTheme.colorScheme.secondary
                        TunnelState.CONNECTED -> MaterialTheme.colorScheme.primary
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful, animated glowing warp button
                WarpCenterToggle(
                    connectionState = connectionState,
                    onClick = { viewModel.toggleWarp() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Diagnostic connection sub-details under toggle button
                if (connectionState == TunnelState.CONNECTED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Shield Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your DNS Queries are fully encrypted via DNS-over-HTTPS (DoH)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Connection Tunnel diagnostics and Details card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("tunnel_diagnostics_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Connection Live Diagnostic",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DiagnosticRow(
                        label = "Secure DNS Tunnel",
                        value = if (connectionState == TunnelState.CONNECTED) "SECURE ACTIVE" else "INACTIVE",
                        icon = Icons.Default.Security,
                        valueColor = if (connectionState == TunnelState.CONNECTED) MaterialTheme.colorScheme.primary else Color.Gray
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                    DiagnosticRow(
                        label = "Gateway IP Address",
                        value = traceData.ip,
                        icon = Icons.Default.Lan
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                    DiagnosticRow(
                        label = "Warp Gateway Edge",
                        value = "${traceData.colo} (${traceData.loc})",
                        icon = Icons.Default.CloudQueue
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                    DiagnosticRow(
                        label = "DNS Host Server",
                        value = selectedProvider.primaryIp,
                        icon = Icons.Default.Dns
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                    DiagnosticRow(
                        label = "Device Live Latency",
                        value = "${traceData.rttMs} ms",
                        icon = Icons.Default.Speed,
                        valueColor = if (traceData.rttMs < 30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                    DiagnosticRow(
                        label = "Provider Network ISP",
                        value = traceData.isp,
                        icon = Icons.Default.Router
                    )
                }
            }
        }

        // DNS SECURE PROVIDER SELECTOR CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("dns_provider_selector_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Choose Secure DNS Protocol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Secure DNS reroutes visual stream lookup queries away from ISP throttling. Choose the protocol that fits your configuration:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DnsProvider.values().forEach { provider ->
                        val isSelected = selectedProvider == provider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setDnsProvider(provider) }
                                .padding(16.dp)
                                .testTag("dns_provider_${provider.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setDnsProvider(provider) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "IP: ${provider.primaryIp} • Host: ${provider.hostname}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // MAIN 4K RESOLUTION SPEED TEST & DECODER OPTIMIZER CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("stream_optimizer_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "4K Bitrate & Stream Diagnostic",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ensure your connection is fast enough to support 4K visual displays. Unlocking 4K requires solid DNS routing bypass and bandwidth over 25 Mbps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = stage,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) with fadeOut(animationSpec = spring())
                        }
                    ) { currentStage ->
                        when (currentStage) {
                            is BenchmarkStage.Idle -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { viewModel.startStreamOptimizerTest() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("run_optimization_test_button")
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Run")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "RUN DETECTOR & 4K BENCHMARK",
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            BenchmarkStage.ResolvingDns,
                            BenchmarkStage.TestingPing,
                            BenchmarkStage.SimulatingDownload,
                            BenchmarkStage.AnalyzingBypass -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val trackColorChoice = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    // Progress state circle/arc in elegant glowing way
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(140.dp)
                                    ) {
                                        // Background Track
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(
                                                color = trackColorChoice,
                                                startAngle = -225f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(width = 14f, cap = Stroke.DefaultCap)
                                            )
                                        }

                                        // Glowing loading Arc
                                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "")
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        GeoPrimary,
                                                        GeoOnSecondary
                                                    )
                                                ),
                                                startAngle = -225f,
                                                sweepAngle = 270f * animatedProgress,
                                                useCenter = false,
                                                style = Stroke(width = 16f, cap = Stroke.DefaultCap)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = if (currentStage == BenchmarkStage.SimulatingDownload) {
                                                        "${liveSpeed.roundToInt()}"
                                                    } else {
                                                        "${(progress * 100).roundToInt()}"
                                                    },
                                                    style = MaterialTheme.typography.displayMedium.copy(
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = FontFamily.Monospace
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (currentStage == BenchmarkStage.SimulatingDownload) " Mbps" else "%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    LinearProgressIndicator(
                                        progress = progress,
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            is BenchmarkStage.Completed -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // SUCCESS OPTIMIZER GRAPHIC
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tv,
                                                contentDescription = "4K Shield",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "4K UNLOCKED ACTIVE",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Maximum Screen Playback Option Resolved Successfully",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Display Benchmark Metrics grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BenchmarkMetricCard(
                                            modifier = Modifier.weight(1f),
                                            title = "Download Speed",
                                            value = "${currentStage.downloadMbps}",
                                            unit = "Mbps",
                                            icon = Icons.Default.Download,
                                            iconColor = MaterialTheme.colorScheme.secondary
                                        )
                                        BenchmarkMetricCard(
                                            modifier = Modifier.weight(1f),
                                            title = "Reroute Ping",
                                            value = "${currentStage.latencyMs}",
                                            unit = "ms",
                                            icon = Icons.Default.Timer,
                                            iconColor = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BenchmarkMetricCard(
                                            modifier = Modifier.weight(1f),
                                            title = "Quality Index",
                                            value = "${currentStage.finalScore}",
                                            unit = "/100",
                                            icon = Icons.Default.Grade,
                                            iconColor = Color.Yellow
                                        )
                                        BenchmarkMetricCard(
                                            modifier = Modifier.weight(1f),
                                            title = "Carrier Bypass",
                                            value = if (currentStage.throttleBypassed) "Bypassed" else "Protected",
                                            unit = "",
                                            icon = Icons.Default.Hub,
                                            iconColor = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = { viewModel.resetBenchmark() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("RESET BENCHMARK ENGINE", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // EDUCATIONAL / PRIVATE DNS GUIDES (FOR UNLOCKING AT SYSTEM ENGINE)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("private_dns_setup_guidelines_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "System-Wide Private DNS (Hardware Link)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can permanently unlock 4K options directly inside Android settings without needing background background processes running. Copy a hostname below and apply as instructed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DnsHostnameCopyRow(
                        title = "Cloudflare Warp Secure DNS",
                        hostname = "1dot1dot1dot1.cloudflare-dns.com",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("1dot1dot1dot1.cloudflare-dns.com"))
                            Toast.makeText(context, "Cloudflare Hostname copied!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DnsHostnameCopyRow(
                        title = "Google Secure DNS Bypass",
                        hostname = "dns.google",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("dns.google"))
                            Toast.makeText(context, "Google Hostname copied!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "How to Apply Direct System-Wide Unlock:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "1. Open your Android device Settings app.\n" +
                                        "2. Go to Network & Internet → Private DNS (or search Custom DNS).\n" +
                                        "3. Select Private DNS provider hostname.\n" +
                                        "4. Paste the copied hostname above and press Save.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // HISTORICAL AUDIT LOG HISTORY OF RUNNING TESTS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Speed & Bypass Audio Logs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (logs.isNotEmpty()) {
                        Text(
                            text = "CLEAR ALL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.clearLogHistory() }
                                .padding(paddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp))
                                .testTag("clear_logs_button")
                        )
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty Logo",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Diagnostic Reports Recorded Yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Run the Bitrate Benchmark tool above to verify capabilities and write details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .testTag("log_item_${log.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = (if (log.isTunnelActive) MaterialTheme.colorScheme.primary else Color.Gray).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (log.isTunnelActive) Icons.Default.NetworkCheck else Icons.Default.SignalCellularAlt,
                                    contentDescription = "Connection Status Logo",
                                    tint = if (log.isTunnelActive) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.maxResolutionUnlocked,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (log.isTunnelActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Score: ${log.testScore}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Bandwidth: ${log.speedMbps} Mbps • Latency: ${log.latencyMs} ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteLog(log.id) },
                            modifier = Modifier.testTag("delete_log_button_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarpCenterToggle(
    connectionState: TunnelState,
    onClick: () -> Unit
) {
    // Beautiful dynamic breathing/scale effects
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (connectionState == TunnelState.CONNECTED) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .testTag("warp_toggle_button_holder")
    ) {
        // Subtle background blurry circle (Geometric Balance Style)
        Box(
            modifier = Modifier
                .size(200.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GeoPrimary.copy(alpha = 0.08f * animatedPulseScale),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.45f
                    )
                }
        )

        // Outer defensive ring representing "border-4 border-[#E7E0EC]" in style
        Box(
            modifier = Modifier
                .size(192.dp)
                .border(4.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )

        // Custom drawn rotating dash indicators for tech visualization
        Canvas(
            modifier = Modifier.size(192.dp)
        ) {
            val strokeColor = when (connectionState) {
                TunnelState.DISCONNECTED -> GeoBorderMedium.copy(alpha = 0.3f)
                TunnelState.CONNECTING -> GeoPrimary.copy(alpha = 0.5f)
                TunnelState.CONNECTED -> GeoPrimary
            }
            drawArc(
                color = strokeColor,
                startAngle = animatedRotation,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 4f)
            )
            drawArc(
                color = strokeColor.copy(alpha = 0.4f),
                startAngle = animatedRotation + 180f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 4f)
            )
        }

        // Inner solid core circle button: "rounded-full bg-white shadow-xl border border-[#CAC4D0]"
        Surface(
            modifier = Modifier
                .size(160.dp)
                .shadow(16.dp, CircleShape)
                .testTag("warp_toggle_button_click"),
            shape = CircleShape,
            color = Color.White, // pure white inner base as specified in Design HTML
            border = BorderStroke(
                1.dp,
                if (connectionState == TunnelState.CONNECTED) GeoPrimary else GeoBorderMedium
            ),
            onClick = onClick
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = when (connectionState) {
                        TunnelState.DISCONNECTED -> Icons.Default.CloudQueue
                        TunnelState.CONNECTING -> Icons.Default.Sync
                        TunnelState.CONNECTED -> Icons.Default.CloudDone
                    },
                    contentDescription = "Toggle Button Icon",
                    tint = if (connectionState == TunnelState.DISCONNECTED) Color.Gray else GeoPrimary,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (connectionState) {
                        TunnelState.DISCONNECTED -> "DISCONNECTED"
                        TunnelState.CONNECTING -> "CONNECTING..."
                        TunnelState.CONNECTED -> "ACTIVE"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = if (connectionState == TunnelState.DISCONNECTED) Color.Gray else GeoPrimary,
                )
            }
        }
    }
}

@Composable
fun DiagnosticRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = valueColor
        )
    }
}

@Composable
fun BenchmarkMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp), // Elegant geometric rounded 24dp shapes
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DnsHostnameCopyRow(
    title: String,
    hostname: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hostname,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onCopy,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
        }
    }
}
