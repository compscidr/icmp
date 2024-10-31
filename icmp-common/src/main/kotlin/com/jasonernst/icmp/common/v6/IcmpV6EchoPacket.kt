package com.jasonernst.icmp.common.v6

import com.jasonernst.icmp.common.Checksum
import com.jasonernst.icmp.common.PacketHeaderException
import java.net.Inet6Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * https://datatracker.ietf.org/doc/html/rfc4443
 */
class IcmpV6EchoPacket(
    sourceAddress: Inet6Address,
    destinationAddress: Inet6Address,
    checksum: UShort = 0u,
    val id: UShort,
    val sequence: UShort,
    val isReply: Boolean = false,
    val data: ByteArray = ByteArray(0),
) : IcmpV6Header(sourceAddress, destinationAddress, if (isReply) IcmpV6Type.ECHO_REPLY_V6 else IcmpV6Type.ECHO_REQUEST_V6, 0u, checksum) {
    companion object {
        const val ICMP_ECHO_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            icmpV6Type: IcmpV6Type,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6EchoPacket {
            buffer.order(order)
            val isReply: Boolean = icmpV6Type == IcmpV6Type.ECHO_REPLY_V6
            if (buffer.remaining() < ICMP_ECHO_LENGTH) throw PacketHeaderException("Buffer too small")
            val id = buffer.short.toUShort()
            val sequence = buffer.short.toUShort()
            val remainingBuffer = ByteArray(min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_LENGTH))
            buffer.get(remainingBuffer)
            return IcmpV6EchoPacket(sourceAddress, destinationAddress, checksum, id, sequence, isReply, remainingBuffer)
        }
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        val buffer = ByteBuffer.allocate(PSEUDOHEADER_LENGTH + size())
        buffer.order(order)
        buffer.put(super.getPseudoHeader(order))
        buffer.put(super.toByteArray(order))
        buffer.putShort(id.toShort())
        buffer.putShort(sequence.toShort())
        buffer.put(data)
        // zero out the checksum before computing it
        buffer.putShort(PSEUDOHEADER_LENGTH + ICMP_CHECKSUM_OFFSET, 0)
        buffer.rewind()
        val checksum = Checksum.calculateChecksum(buffer)
        buffer.putShort(PSEUDOHEADER_LENGTH + ICMP_CHECKSUM_OFFSET, checksum.toShort())
        // return the array without the psuedoheader
        return buffer.array().copyOfRange(PSEUDOHEADER_LENGTH, buffer.array().size)
    }

    override fun size(): Int = ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_LENGTH + data.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV6EchoPacket

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

    override fun toString(): String = "ICMPv6EchoPacket(id=$id, sequence=$sequence, isReply=$isReply, data=${data.contentToString()})"
}
