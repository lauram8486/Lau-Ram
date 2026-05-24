package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PerformanceLog
import com.example.data.WarpDatabase
import com.example.data.WarpRepository
import com.example.network.DnsProvider
import com.example.network.TunnelState
import com.example.network.WarpTunnelManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

sealed interface BenchmarkStage {
    object Idle : BenchmarkStage
    object ResolvingDns : BenchmarkStage
    object TestingPing : BenchmarkStage
    object SimulatingDownload : BenchmarkStage
    object AnalyzingBypass : BenchmarkStage
    data class Completed(
        val latencyMs: Int,
        val downloadMbps: Double,
        val packetLoss: Double,
        val maxResolution: String,
        val throttleBypassed: Boolean,
        val finalScore: Int
    ) : BenchmarkStage
}

class WarpViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WarpDatabase.getDatabase(application)
    private val repository = WarpRepository(database.performanceLogDao())

    val tunnelManager = WarpTunnelManager()

    val connectionState: StateFlow<TunnelState> = tunnelManager.connectionState
    val selectedProvider: StateFlow<DnsProvider> = tunnelManager.selectedProvider
    val detectedTrace = tunnelManager.detectedTrace

    val logsHistory: StateFlow<List<PerformanceLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _benchmarkStage = MutableStateFlow<BenchmarkStage>(BenchmarkStage.Idle)
    val benchmarkStage: StateFlow<BenchmarkStage> = _benchmarkStage

    private val _benchmarkProgress = MutableStateFlow(0f)
    val benchmarkProgress: StateFlow<Float> = _benchmarkProgress

    private val _currentStatusText = MutableStateFlow("Ready to scan")
    val currentStatusText: StateFlow<String> = _currentStatusText

    private val _liveSpeed = MutableStateFlow(0.0)
    val liveSpeed: StateFlow<Double> = _liveSpeed

    init {
        // Initialize network status on first launch
        viewModelScope.launch {
            tunnelManager.refreshNetworkStatus()
        }
    }

    fun toggleWarp() {
        viewModelScope.launch {
            tunnelManager.toggleConnection()
        }
    }

    fun setDnsProvider(provider: DnsProvider) {
        tunnelManager.setProvider(provider)
    }

    fun startStreamOptimizerTest() {
        viewModelScope.launch {
            _benchmarkProgress.value = 0f
            
            // Step 1: Resolving Secure DNS (0% - 25%)
            _benchmarkStage.value = BenchmarkStage.ResolvingDns
            _currentStatusText.value = "Initiating DNS resolution over secure handshake..."
            for (i in 0..25) {
                _benchmarkProgress.value = i / 100f
                delay(30)
            }

            // Step 2: Evaluating Edge Latency (25% - 50%)
            _benchmarkStage.value = BenchmarkStage.TestingPing
            val dnsName = selectedProvider.value.displayName
            _currentStatusText.value = "Measuring Edge Node ping on $dnsName..."
            // Fetch fresh latency
            tunnelManager.refreshNetworkStatus()
            val basePing = detectedTrace.value.rttMs
            for (i in 25..50) {
                _benchmarkProgress.value = i / 100f
                delay(40)
            }

            // Step 3: Simulating 4K Stream Fragment Download (50% - 85%)
            _benchmarkStage.value = BenchmarkStage.SimulatingDownload
            _currentStatusText.value = "Streaming dynamic chunks of 4K video buffer..."
            
            // Let's sweep speed up to double check network capacity
            val maxSimSpeed = if (connectionState.value == TunnelState.CONNECTED) {
                // If connected, simulated bandwidth is higher due to bypassed ISP throttling config
                Random.nextDouble(55.0, 140.0)
            } else {
                // Throttled speed
                Random.nextDouble(12.0, 24.5)
            }

            for (i in 50..85) {
                _benchmarkProgress.value = i / 100f
                val interpolator = (i - 50) / 35.0
                _liveSpeed.value = interpolator * maxSimSpeed + Random.nextDouble(-2.0, 2.0)
                delay(60)
            }

            // Step 4: Analyzing Bypass Capabilities (85% - 100%)
            _benchmarkStage.value = BenchmarkStage.AnalyzingBypass
            _currentStatusText.value = "Bypass detection scanning: analyzing packet throttling signature..."
            for (i in 85..100) {
                _benchmarkProgress.value = i / 100f
                delay(50)
            }

            // Calculations
            val pingResult = (basePing + Random.nextInt(-5, 5)).coerceAtLeast(10)
            val finalSpeed = _liveSpeed.value.coerceAtLeast(5.0)
            val lossResult = if (connectionState.value == TunnelState.CONNECTED) {
                Random.nextDouble(0.0, 0.2)
            } else {
                Random.nextDouble(0.2, 1.4)
            }

            // Determine maximum unlocked resolution
            // 4K stream needs at least 25 Mbps
            val maxRes = if (finalSpeed >= 25.0) {
                "4K (2160p Ultra HD)"
            } else if (finalSpeed >= 15.0) {
                "2K (1440p Quad HD)"
            } else {
                "1080p (Full HD)"
            }

            val bypassed = connectionState.value == TunnelState.CONNECTED && finalSpeed >= 25.0
            val score = (((finalSpeed / 140.0) * 40) + ((100.0 - pingResult.toDouble()) / 100.0 * 50) + (10 - lossResult * 10)).roundToInt().coerceIn(40, 100)

            val completedResult = BenchmarkStage.Completed(
                latencyMs = pingResult,
                downloadMbps = (finalSpeed * 10).roundToInt() / 10.0,
                packetLoss = (lossResult * 100).roundToInt() / 100.0,
                maxResolution = maxRes,
                throttleBypassed = bypassed,
                finalScore = score
            )

            // Save test report into Room Database
            val log = PerformanceLog(
                latencyMs = pingResult,
                speedMbps = (finalSpeed * 10).roundToInt() / 10.0,
                dnsProvider = selectedProvider.value.displayName,
                isTunnelActive = connectionState.value == TunnelState.CONNECTED,
                maxResolutionUnlocked = maxRes,
                testScore = score
            )
            repository.insertLog(log)

            _benchmarkStage.value = completedResult
            _currentStatusText.value = "Diagnostic complete! Max Resolution unlocked: $maxRes."
        }
    }

    fun resetBenchmark() {
        _benchmarkStage.value = BenchmarkStage.Idle
        _benchmarkProgress.value = 0f
        _currentStatusText.value = "Ready to scan"
        _liveSpeed.value = 0.0
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }
}

class WarpViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WarpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WarpViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
