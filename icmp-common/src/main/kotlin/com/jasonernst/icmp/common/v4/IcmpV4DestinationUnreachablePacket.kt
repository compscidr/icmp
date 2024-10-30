package com.jasonernst.icmp.common.v4

import com.jasonernst.icmp.common.Checksum
import com.jasonernst.icmp.common.IcmpHeader.Companion.ICMP_CHECKSUM_OFFSET
import com.jasonernst.icmp.common.IcmpHeader.Companion.ICMP_HEADER_MIN_LENGTH
import com.jasonernst.icmp.common.PacketHeaderException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * https://www.rfc-editor.org/rfc/rfc792.html pg 4
 *
 * Note, that the data should contain the InternetHeader + 64 bits of the original datagram
 * causing the error. We are not going to the trouble of parsing the InternetHeader, so we'll just
 * include it as a byte array.
 */
class IcmpV4DestinationUnreachablePacket(
    code: IcmpV4DestinationUnreachableCodes,
    checksum: UShort = 0u,
    val data: ByteArray = ByteArray(0),
) : IcmpV4Header(IcmpV4Type.DESTINATION_UNREACHABLE, code.value, checksum) {
    companion object {
        // type (1) + code (1) + checksum (2) + unused (4)
        const val DESTINATION_UNREACHABLE_HEADER_MIN_LENGTH: UShort = 8u

        fun fromStream(
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            code: UByte,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV4DestinationUnreachablePacket {
            buffer.order(order)
            val remainingBytes = min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt())
            if (remainingBytes < 4) {
                throw PacketHeaderException("Buffer too small, expected at least 4 bytes to get unused zero padding, got $remainingBytes")
            }
            buffer.getInt() // 4 bytes "unused"
            val remainingBuffer = ByteArray(remainingBytes - 4)
            buffer.get(remainingBuffer)
            return IcmpV4DestinationUnreachablePacket(IcmpV4DestinationUnreachableCodes.fromValue(code), checksum, data = remainingBuffer)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV4DestinationUnreachablePacket

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        val buffer = ByteBuffer.allocate(DESTINATION_UNREACHABLE_HEADER_MIN_LENGTH.toInt() + data.size)
        buffer.order(order)
        buffer.put(super.toByteArray(order))
        buffer.putInt(0) // 4 bytes "unused"
        buffer.put(data)
        buffer.rewind()
        val checksum = Checksum.calculateChecksum(buffer)
        buffer.putShort(ICMP_CHECKSUM_OFFSET, checksum.toShort())
        return buffer.array()
    }

    override fun size(): Int = DESTINATION_UNREACHABLE_HEADER_MIN_LENGTH.toInt() + data.size

    override fun toString(): String = "ICMPv4DestinationUnreachablePacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
}
