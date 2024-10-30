package com.jasonernst.icmp.common.v4

import com.jasonernst.icmp.common.Checksum
import com.jasonernst.icmp.common.PacketHeaderException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * Minimal implementation of an ICMPv4 Echo Request/Reply packet.
 *
 * https://www.rfc-editor.org/rfc/rfc792.html page 14
 * https://www.rfc-editor.org/rfc/rfc2780.html
 *
 */
class IcmpV4EchoPacket(
    checksum: UShort = 0u,
    val id: UShort,
    val sequence: UShort,
    val isReply: Boolean = false,
    val data: ByteArray = ByteArray(0),
) : IcmpV4Header(if (isReply) IcmpV4Type.ECHO_REPLY else IcmpV4Type.ECHO_REQUEST, 0u, checksum) {
    companion object {
        private const val ICMP_ECHO_MIN_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromStream(
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            icmpV4Type: IcmpV4Type,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV4EchoPacket {
            buffer.order(order)
            val isReply: Boolean = icmpV4Type == IcmpV4Type.ECHO_REPLY
            if (buffer.remaining() < ICMP_ECHO_MIN_LENGTH) throw PacketHeaderException("Buffer too small")
            val id = buffer.short.toUShort()
            val sequence = buffer.short.toUShort()
            val remainingBuffer = ByteArray(min(buffer.remaining(), limit - ICMP_ECHO_MIN_LENGTH))
            buffer.get(remainingBuffer)
            return IcmpV4EchoPacket(checksum, id, sequence, isReply, remainingBuffer)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV4EchoPacket

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

    override fun toByteArray(order: ByteOrder): ByteArray {
        val buffer = ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_MIN_LENGTH + data.size)
        buffer.order(order)
        buffer.put(super.toByteArray(order))
        buffer.putShort(id.toShort())
        buffer.putShort(sequence.toShort())
        buffer.put(data)
        buffer.rewind()
        val checksum =
            com.jasonernst.icmp.common.Checksum
                .calculateChecksum(buffer)
        buffer.putShort(ICMP_CHECKSUM_OFFSET, checksum.toShort())
        return buffer.array()
    }

    override fun size(): Int = ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_MIN_LENGTH + data.size

    override fun toString(): String =
        "ICMPv4EchoPacket(checksum=$checksum, id=$id, sequence=$sequence, isReply=$isReply, data=${data.contentToString()})"
}
