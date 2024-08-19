package com.jasonernst.icmp_common.v4

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * https://www.rfc-editor.org/rfc/rfc792.html pg 4
 *
 * Note, that the data should contain the InternetHeader + 64 bits of the original datagram
 * causing the error. We are not going to the trouble of parsing the InternetHeader, so we'll just
 * include it as a byte array.
 */
class ICMPv4DestinationUnreachablePacket(code: ICMPv4DestinationUnreachableCodes,
                                         checksum: UShort = 0u,
                                         val data: ByteArray = ByteArray(0)): ICMPv4Header(ICMPv4Type.DESTINATION_UNREACHABLE, code.value, checksum) {
    companion object {
        fun fromStream(buffer: ByteBuffer, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv4DestinationUnreachablePacket {
            buffer.order(order)
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv4DestinationUnreachablePacket(ICMPv4DestinationUnreachableCodes.fromValue(code), checksum, data = remainingBuffer)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv4DestinationUnreachablePacket

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + data.size).apply {
            order(order)
            put(super.toByteArray(order))
            put(data)
            return array()
        }
    }

    override fun size(): Int {
        return ICMP_HEADER_MIN_LENGTH.toInt() + data.size
    }

    override fun toString(): String {
        return "ICMPv4DestinationUnreachablePacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
    }
}