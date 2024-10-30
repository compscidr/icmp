package com.jasonernst.icmp.common.v4

import com.jasonernst.icmp.common.Checksum.calculateChecksum
import com.jasonernst.icmp.common.IcmpHeader
import com.jasonernst.icmp.common.PacketHeaderException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation of ICMP header:
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
abstract class IcmpV4Header(
    icmpV4Type: IcmpV4Type,
    code: UByte,
    checksum: UShort,
) : IcmpHeader(type = icmpV4Type, code = code, checksum = checksum) {
    companion object {
        fun fromStream(
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV4Header {
            if (buffer.remaining() < ICMP_HEADER_MIN_LENGTH.toInt()) {
                throw PacketHeaderException("Require at least $ICMP_HEADER_MIN_LENGTH bytes to parse an IcmpV4 header")
            }
            val newType = IcmpV4Type.fromValue(buffer.get().toUByte())
            val newCode = buffer.get().toUByte()
            val newChecksum = buffer.short.toUShort()
            return when (newType) {
                IcmpV4Type.ECHO_REPLY, IcmpV4Type.ECHO_REQUEST -> {
                    IcmpV4EchoPacket.fromStream(buffer, limit, newType, newChecksum, order)
                }
                IcmpV4Type.DESTINATION_UNREACHABLE -> {
                    IcmpV4DestinationUnreachablePacket.fromStream(buffer, limit, newCode, newChecksum, order)
                }
                IcmpV4Type.TIME_EXCEEDED -> {
                    IcmpV4TimeExceededPacket.fromStream(buffer, limit, newCode, newChecksum, order)
                }
                else -> {
                    throw PacketHeaderException("Unsupported ICMPv4 type")
                }
            }
        }
    }

    fun computeChecksum(order: ByteOrder = ByteOrder.BIG_ENDIAN): UShort {
        val buffer = ByteBuffer.allocate(size())
        buffer.order(order)
        buffer.put(toByteArray(order))
        buffer.putShort(ICMP_CHECKSUM_OFFSET, 0)
        buffer.rewind()
        return calculateChecksum(buffer)
    }
}
