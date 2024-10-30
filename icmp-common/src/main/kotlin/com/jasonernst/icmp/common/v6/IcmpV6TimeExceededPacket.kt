package com.jasonernst.icmp.common.v6

import java.net.Inet6Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class IcmpV6TimeExceededPacket(
    sourceAddress: Inet6Address,
    destinationAddress: Inet6Address,
    code: IcmpV6TimeExceededCodes,
    checksum: UShort = 0u,
    val data: ByteArray = ByteArray(0),
) : IcmpV6Header(sourceAddress, destinationAddress, IcmpV6Type.TIME_EXCEEDED, code.value, checksum) {
    companion object {
        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            code: UByte,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6TimeExceededPacket {
            buffer.order(order)
            val remainingBuffer = ByteArray(min(buffer.remaining(), limit - ICMP_HEADER_MIN_LENGTH.toInt()))
            buffer.get(remainingBuffer)
            return IcmpV6TimeExceededPacket(
                sourceAddress,
                destinationAddress,
                IcmpV6TimeExceededCodes.fromValue(code),
                checksum,
                data = remainingBuffer,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IcmpV6TimeExceededPacket

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

    override fun toString(): String = "ICMPv6TimeExceededPacket(code=$code, checksum=$checksum, data=${data.contentToString()})"
}
