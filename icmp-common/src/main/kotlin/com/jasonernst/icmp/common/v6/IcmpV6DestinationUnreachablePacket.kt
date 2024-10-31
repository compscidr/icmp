package com.jasonernst.icmp.common.v6

import com.jasonernst.icmp.common.Checksum
import com.jasonernst.icmp.common.PacketHeaderException
import com.jasonernst.icmp.common.v4.IcmpV4DestinationUnreachablePacket.Companion.DESTINATION_UNREACHABLE_HEADER_MIN_LENGTH
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.slf4j.LoggerFactory
import java.net.Inet6Address
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
class IcmpV6DestinationUnreachablePacket(
    sourceAddress: Inet6Address,
    destinationAddress: Inet6Address,
    code: IcmpV6DestinationUnreachableCodes,
    checksum: UShort = 0u,
    val data: ByteArray = ByteArray(0),
) : IcmpV6Header(sourceAddress, destinationAddress, IcmpV6Type.DESTINATION_UNREACHABLE, code.value, checksum) {
    companion object {
        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            code: UByte,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6DestinationUnreachablePacket {
            buffer.order(order)
            val remainingBytes = min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt())
            if (remainingBytes < 4) {
                throw PacketHeaderException("Buffer too small, expected at least 4 bytes to get unused zero padding, got $remainingBytes")
            }
            buffer.getInt() // 4 bytes "unused"
            val remainingBuffer = ByteArray(remainingBytes - 4)
            buffer.get(remainingBuffer)
            return IcmpV6DestinationUnreachablePacket(
                sourceAddress,
                destinationAddress,
                IcmpV6DestinationUnreachableCodes.fromValue(code),
                checksum,
                data = remainingBuffer,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV6DestinationUnreachablePacket

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        val buffer = ByteBuffer.allocate(PSEUDOHEADER_LENGTH + size())
        buffer.order(order)
        buffer.put(super.getPseudoHeader(order))
        buffer.put(super.toByteArray(order))
        buffer.putInt(0) // 4 bytes "unused"
        buffer.put(data)
        // zero out the checksum before computing it
        buffer.putShort(PSEUDOHEADER_LENGTH + ICMP_CHECKSUM_OFFSET, 0)
        buffer.rewind()

        val logger = LoggerFactory.getLogger(javaClass)
        val stringPacketDumper = StringPacketDumper(logger)
        stringPacketDumper.dumpBuffer(buffer, 0, buffer.limit())

        val checksum = Checksum.calculateChecksum(buffer)
        buffer.putShort(PSEUDOHEADER_LENGTH + ICMP_CHECKSUM_OFFSET, checksum.toShort())
        // return the array without the psuedoheader
        return buffer.array().copyOfRange(PSEUDOHEADER_LENGTH, buffer.array().size)
    }

    override fun size(): Int = DESTINATION_UNREACHABLE_HEADER_MIN_LENGTH.toInt() + data.size

    override fun toString(): String = "ICMPv6DestinationUnreachablePacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
}
