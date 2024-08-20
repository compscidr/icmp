package com.jasonernst.icmp_common.v6

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * https://datatracker.ietf.org/doc/html/rfc3810
 * https://datatracker.ietf.org/doc/html/rfc4604
 *
 * The first reserved field is where the code would be in other ICMPv6 packets. We will treat the
 * remaining packet as data and not parse it.
 *
 *      0                   1                   2                   3
 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |  Type = 143   |    Reserved   |           Checksum            |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |           Reserved            |Nr of Mcast Address Records (M)|
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     .                                                               .
 *     .                  Multicast Address Record [1]                 .
 *     .                                                               .
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     .                                                               .
 *     .                  Multicast Address Record [2]                 .
 *     .                                                               .
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                               .                               |
 *     .                               .                               .
 *     |                               .                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     .                                                               .
 *     .                  Multicast Address Record [M]                 .
 *     .                                                               .
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
class ICMPv6MulticastListenerDiscoveryV2(val data: ByteArray = ByteArray(0)): ICMPv6Header(icmPv6Type = ICMPv6Type.MULTICAST_LISTENER_DISCOVERY_V2, code = 0u, checksum = 0u) {

    companion object {
        fun fromStream(buffer: ByteBuffer, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv6MulticastListenerDiscoveryV2 {
            buffer.order(order)
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6MulticastListenerDiscoveryV2(data = remainingBuffer)
        }
    }

    override fun size(): Int {
        return ICMP_HEADER_MIN_LENGTH.toInt() + data.size
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + data.size).apply {
            order(order)
            put(super.toByteArray(order))
            put(data)
            return array()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv6MulticastListenerDiscoveryV2

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ICMPv6MulticastListenerDiscoveryV2(data=${data.contentToString()})"
    }
}