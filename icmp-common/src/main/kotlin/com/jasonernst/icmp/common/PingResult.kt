package com.jasonernst.icmp.common

import java.net.InetAddress

/**
 * Represents the result of a ping operation.
 *
 * Note: using a sealed class gives a dokka error: "PermittedSubclasses requires ASM9"
 */
open class PingResult {
    data class Success(
        val sequenceNumber: Int,
        val packetSize: Int,
        val ms: Long,
        val inetAddress: InetAddress,
    ) : com.jasonernst.icmp.common.PingResult()

    data class Failed(
        val errorMessage: String,
        val inetAddress: InetAddress? = null,
    ) : com.jasonernst.icmp.common.PingResult()
}
