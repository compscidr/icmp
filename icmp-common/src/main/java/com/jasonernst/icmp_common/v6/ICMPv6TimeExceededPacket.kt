package com.jasonernst.icmp_common.v6

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ICMPv6TimeExceededPacket(code: ICMPv6TimeExceededCodes, checksum: UShort = 0u, val data: ByteArray = ByteArray(0)): ICMPv6Header(
    ICMPv6Type.TIME_EXCEEDED, code.value, checksum) {
    companion object {
        fun fromStream(buffer: ByteBuffer, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv6TimeExceededPacket {
            buffer.order(order)
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6TimeExceededPacket(ICMPv6TimeExceededCodes.fromValue(code), checksum, data = remainingBuffer)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv6TimeExceededPacket

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
        return "ICMPv6TimeExceededPacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
    }
}