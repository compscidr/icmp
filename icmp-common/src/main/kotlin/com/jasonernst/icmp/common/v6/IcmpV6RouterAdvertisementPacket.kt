package com.jasonernst.icmp.common.v6

import java.net.Inet6Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * https://www.rfc-editor.org/rfc/rfc4861.html#section-4.2 page 19
 *
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |     Type      |     Code      |          Checksum             |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      | Cur Hop Limit |M|O|  Reserved |       Router Lifetime         |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                         Reachable Time                        |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                          Retrans Timer                        |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |   Options ...
 *      +-+-+-+-+-+-+-+-+-+-+-+-
 *
 *      TODO: proper option processing: https://www.rfc-editor.org/rfc/rfc4861.html#section-9
 */
class IcmpV6RouterAdvertisementPacket(
    sourceAddress: Inet6Address,
    destinationAddress: Inet6Address,
    val curHopLimit: UByte,
    val M: Boolean,
    val O: Boolean,
    val routerLifetime: UShort,
    val reachableTime: UInt,
    val retransTimer: UInt,
    val options: ByteArray = ByteArray(0),
) : IcmpV6Header(sourceAddress, destinationAddress, IcmpV6Type.ROUTER_ADVERTISEMENT_V6, 0u, 0u) {
    companion object {
        // cur hop limit (1 byte) + flags (1 byte) + router lifetime (2 bytes) + reachable time (4 bytes) + retrans timer (4 bytes)
        const val ICMP_ADVERTISEMENT_PACKET_MIN_LENGTH: UShort = 12u

        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6RouterAdvertisementPacket {
            buffer.order(order)
            val curHopLimit = buffer.get().toUByte()
            val flags = buffer.get().toUByte()
            val m = flags.toInt() and 0b10000000 == 0b10000000
            val o = flags.toInt() and 0b01000000 == 0b01000000
            val routerLifetime = buffer.short.toUShort()
            val reachableTime = buffer.int.toUInt()
            val retransTimer = buffer.int.toUInt()
            val options =
                ByteArray(
                    min(
                        buffer.remaining(),
                        limit - ICMP_HEADER_MIN_LENGTH.toInt() - ICMP_ADVERTISEMENT_PACKET_MIN_LENGTH.toInt(),
                    ),
                )
            buffer.get(options)
            return IcmpV6RouterAdvertisementPacket(
                sourceAddress,
                destinationAddress,
                curHopLimit,
                m,
                o,
                routerLifetime,
                reachableTime,
                retransTimer,
                options,
            )
        }
    }

    override fun size(): Int = ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ADVERTISEMENT_PACKET_MIN_LENGTH.toInt() + options.size

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ADVERTISEMENT_PACKET_MIN_LENGTH.toInt() + options.size).apply {
            order(order)
            put(super.toByteArray(order))
            put(curHopLimit.toByte())
            put(((if (M) 0b10000000 else 0) or (if (O) 0b01000000 else 0)).toByte())
            putShort(routerLifetime.toShort())
            putInt(reachableTime.toInt())
            putInt(retransTimer.toInt())
            put(options)
            return array()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV6RouterAdvertisementPacket

        if (curHopLimit != other.curHopLimit) return false
        if (M != other.M) return false
        if (O != other.O) return false

        if (routerLifetime != other.routerLifetime) return false
        if (reachableTime != other.reachableTime) return false
        if (retransTimer != other.retransTimer) return false
        if (!options.contentEquals(other.options)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = curHopLimit.toInt()
        result = 31 * result + M.hashCode()
        result = 31 * result + O.hashCode()
        result = 31 * result + routerLifetime.toInt()
        result = 31 * result + reachableTime.hashCode()
        result = 31 * result + retransTimer.hashCode()
        result = 31 * result + options.contentHashCode()
        return result
    }

    override fun toString(): String = "ICMPv6RouterAdvertisementPacket()"
}
