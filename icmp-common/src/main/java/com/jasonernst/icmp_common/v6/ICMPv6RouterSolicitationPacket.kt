package com.jasonernst.icmp_common.v6

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * https://www.rfc-editor.org/rfc/rfc4861.html#section-4.1 page 18
 *
 * Payload supports:
 * Source link-layer address The link-layer address of the sender, if
 * known.  MUST NOT be included if the Source Address
 * is the unspecified address.  Otherwise, it SHOULD
 * be included on link layers that have addresses.
 *
 * Since it's optional we'll just leave it out for now.
 *
 *      0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |     Type      |     Code      |          Checksum             |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                            Reserved                           |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |   Options ...
 *      +-+-+-+-+-+-+-+-+-+-+-+-
 *
 *  TODO: proper option processing: https://www.rfc-editor.org/rfc/rfc4861.html#section-9
 *  Valid Options:
 *
 *       Source link-layer address The link-layer address of the sender, if
 *                      known.  MUST NOT be included if the Source Address
 *                      is the unspecified address.  Otherwise, it SHOULD
 *                      be included on link layers that have addresses.
 *
 *       Future versions of this protocol may define new option types.
 *       Receivers MUST silently ignore any options they do not recognize
 *       and continue processing the message.
 *
 */
class ICMPv6RouterSolicitationPacket(val data: ByteArray = ByteArray(0)): ICMPv6Header(icmPv6Type = ICMPv6Type.ROUTER_SOLICITATION_V6, code = 0u, checksum = 0u) {

    companion object {
        fun fromStream(buffer: ByteBuffer, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv6RouterSolicitationPacket {
            buffer.order(order)
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6RouterSolicitationPacket(data = remainingBuffer)
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

        other as ICMPv6RouterSolicitationPacket

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ICMPv6RouterSolicitationPacket(data=${data.contentToString()})"
    }

}