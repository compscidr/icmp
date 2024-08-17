package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.PacketHeaderException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * https://datatracker.ietf.org/doc/html/rfc4443
 */
class ICMPv6EchoPacket(
    checksum: UShort = 0u,
    val id: UShort,
    val sequence: UShort,
    val isReply: Boolean = false,
    val data: ByteArray = ByteArray(0),
) : ICMPv6Header(if (isReply) ICMPv6Type.ECHO_REPLY_V6 else ICMPv6Type.ECHO_REQUEST_V6, 0u, checksum) {
    companion object {
        const val ICMP_ECHO_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromStream(
            buffer: ByteBuffer,
            icmpV6Type: ICMPv6Type,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN
        ): ICMPv6EchoPacket {
            buffer.order(order)
            val isReply: Boolean = icmpV6Type == ICMPv6Type.ECHO_REPLY_V6
            if (buffer.remaining() < ICMP_ECHO_LENGTH) throw PacketHeaderException("Buffer too small")
            val id = buffer.short.toUShort()
            val sequence = buffer.short.toUShort()
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6EchoPacket(checksum, id, sequence, isReply, remainingBuffer)
        }
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_LENGTH + data.size).apply {
            order(order)
            put(super.toByteArray(order))
            putShort(id.toShort())
            putShort(sequence.toShort())
            put(data)
            return array()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv6EchoPacket

        if (sequence != other.sequence) return false
        if (id != other.id) return false
        if (isReply != other.isReply) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + isReply.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ICMPv6EchoPacket(id=$id, sequence=$sequence, isReply=$isReply, data=${data.contentToString()})"
    }
}