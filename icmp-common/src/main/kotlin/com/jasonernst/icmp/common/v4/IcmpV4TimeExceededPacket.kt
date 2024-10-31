package com.jasonernst.icmp.common.v4

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class IcmpV4TimeExceededPacket(
    code: IcmpV4DestinationUnreachableCodes,
    checksum: UShort = 0u,
    val data: ByteArray = ByteArray(0),
) : IcmpV4Header(IcmpV4Type.TIME_EXCEEDED, code.value, checksum) {
    companion object {
        fun fromStream(
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            code: UByte,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV4TimeExceededPacket {
            buffer.order(order)
            val remainingBuffer = ByteArray(min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt()))
            buffer.get(remainingBuffer)
            return IcmpV4TimeExceededPacket(
                com.jasonernst.icmp.common.v4.IcmpV4TimeExceededCodes
                    .fromValue(code),
                checksum,
                data = remainingBuffer,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV4TimeExceededPacket

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

    override fun size(): Int = ICMP_HEADER_MIN_LENGTH.toInt() + data.size

    override fun toString(): String = "ICMPv4TimeExceededPacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
}
