package com.jasonernst.icmp.ui

import androidx.lifecycle.ViewModel
import com.jasonernst.icmp_android.ICMPAndroid
import com.jasonernst.icmp_common.PingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

private const val RESULT_CACHE_CAPACITY = 20

class MainScreenViewModel: ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val icmp = ICMPAndroid
    private val _hostField = MutableStateFlow("8.8.8.8")

    private var _minPingMS = Long.MAX_VALUE
    private var _maxPingMS = Long.MIN_VALUE
    private var _ip = ""
    private var _packetsSent = 0
    private var _packetsReceived = 0
    private var _totalLatencyMs = 0L
    private var _packetsLost = 0

    data class Stats(
        val host: String,
        val ip: String? = null,
        val pingMs: Long? = null,
        val minPingMS: Long? = null,
        val maxPingMS: Long? = null,
        val avgPingMS: Long? = null,
        val packetsSent: Int = 0,
        val packetsReceived: Int = 0,
        val packetsLost: Int = 0,
        val error: String? = null,
        val results: List<ResultItem>
    )

    data class ResultItem(
        val num: Int? = null,
        val message: String,
        val ms: Long? = null
    )

    val hostField: StateFlow<String>
        get() = _hostField

    fun onHostFieldChanged(value: String) {
        logger.info("onHostFieldChanged: $value")
        _hostField.value = value
    }

    private fun resetStats(host: String, error: String? = null): Stats {
        _minPingMS = Long.MAX_VALUE
        _maxPingMS = Long.MIN_VALUE
        _ip = ""
        _packetsSent = 0
        _packetsReceived = 0
        _totalLatencyMs = 0
        _packetsLost = 0
        if (host.isEmpty()) {
            return Stats(host, "", 0, error = "No host to ping", results = emptyList())
        }
        return Stats(host, "", 0, error = error, results = emptyList())
    }

    private fun processPingResult(host: String, pingResult: PingResult, results: List<ResultItem>): Stats {
        _packetsSent++

        return if (pingResult is PingResult.Success) {
            _ip = pingResult.inetAddress.hostAddress?.toString() ?: ""
            _packetsReceived++
            if (pingResult.ms < _minPingMS) {
                _minPingMS = pingResult.ms
            }
            if (pingResult.ms > _maxPingMS) {
                _maxPingMS = pingResult.ms
            }
            _totalLatencyMs += pingResult.ms
            Stats(
                host = host,
                ip = _ip,
                pingMs = pingResult.ms,
                minPingMS = _minPingMS,
                maxPingMS = _maxPingMS,
                avgPingMS = _totalLatencyMs / _packetsReceived,
                packetsSent = _packetsSent,
                packetsReceived = _packetsReceived,
                packetsLost = _packetsLost,
                results = results,
            )
        } else {
            _packetsLost++
            val avg = if (_packetsReceived > 0) _totalLatencyMs / _packetsReceived else 0
            Stats(
                host = host,
                ip = _ip,
                minPingMS = _minPingMS,
                maxPingMS = _maxPingMS,
                avgPingMS = avg,
                packetsSent = _packetsSent,
                packetsReceived = _packetsReceived,
                packetsLost = _packetsLost,
                results = results,
            )
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val stats = _hostField.flatMapLatest { host ->
        logger.debug("ABOUT TO CALL PING")
        val stats = resetStats(host)
        if (host.isEmpty()) {
            return@flatMapLatest flowOf(stats)
        }
        icmp.ping(host, 1000, 1000, 1000, 0u)
            .cacheLatest(RESULT_CACHE_CAPACITY)
            .map { cachedResults ->
                val latestResult = cachedResults.last()
                logger.debug("latestResult: $latestResult")
                processPingResult(host, latestResult, results = cachedResults
                    .asReversed()
                    .map { result ->
                        when (result) {
                            is PingResult.Success -> ResultItem(
                                num = result.sequenceNumber,
                                message = "Success",
                                ms = result.ms
                            )
                            is PingResult.Failed -> ResultItem(
                                message = "Failed",
                                ms = null,
                            )
                            else -> {
                                throw IllegalStateException("Unknown PingResult type")
                            }
                        }
                    }
                )
            }.catch { e->
                logger.error("Error: $e")
                val errorStats = resetStats(host, e.message)
                emit(errorStats)
            }
    }

    private fun <T> Flow<T>.cacheLatest(capacity: Int): Flow<List<T>> {
        return scan<T, LinkedBlockingQueue<T>>(LinkedBlockingQueue(capacity)) { cache, item ->
            while (cache.remainingCapacity() < 1) {
                cache.poll()
            }
            cache.add(item)
            cache
        }
            .map { it.toList() }
            .drop(1)
    }
}