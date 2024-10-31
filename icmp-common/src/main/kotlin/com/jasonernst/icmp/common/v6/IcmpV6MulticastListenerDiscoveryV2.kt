package com.jasonernst.icmp.common.v6

import java.net.Inet6Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

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
class IcmpV6MulticastListenerDiscoveryV2(
    sourceAddress: Inet6Address,
    destinationAddress: Inet6Address,
    val data: ByteArray = ByteArray(0),
) : IcmpV6Header(sourceAddress, destinationAddress, icmPv6Type = IcmpV6Type.MULTICAST_LISTENER_DISCOVERY_V2, code = 0u, checksum = 0u) {
    companion object {
        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6MulticastListenerDiscoveryV2 {
            buffer.order(order)
            val remainingBuffer = ByteArray(min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt()))
            buffer.get(remainingBuffer)
            return IcmpV6MulticastListenerDiscoveryV2(sourceAddress, destinationAddress, data = remainingBuffer)
        }
    }

    override fun size(): Int = ICMP_HEADER_MIN_LENGTH.toInt() + data.size

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

        other as IcmpV6MulticastListenerDiscoveryV2

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String = "ICMPv6MulticastListenerDiscoveryV2(data=${data.contentToString()})"
}
