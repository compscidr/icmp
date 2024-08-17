package com.jasonernst.icmp_common

import com.jasonernst.icmp_common.v4.ICMPv4Header
import com.jasonernst.icmp_common.v4.ICMPv4Type
import com.jasonernst.icmp_common.v6.ICMPv6Header
import com.jasonernst.icmp_common.v6.ICMPv6Type
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ICMPHeader(val type: ICMPType, val code: UByte, val checksum: UShort) {

    companion object {
        // uByte + uByte + uShort
        const val ICMP_HEADER_MIN_LENGTH: UShort = 4u
        const val ICMP_CHECKSUM_OFFSET: UShort = 2u

        fun fromStream(byteBuffer: ByteBuffer, isIcmpV4: Boolean = true, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPHeader {
            byteBuffer.order(order)
            if (byteBuffer.remaining() < ICMP_HEADER_MIN_LENGTH.toInt()) {
                throw PacketHeaderException("Buffer too small, expected at least ${ICMP_HEADER_MIN_LENGTH.toInt()} bytes, got ${byteBuffer.remaining()}")
            }
            val newType = if (isIcmpV4) ICMPv4Type.fromValue(byteBuffer.get().toUByte()) else ICMPv6Type.fromValue(byteBuffer.get().toUByte())
            val newCode = byteBuffer.get().toUByte()
            val newChecksum = byteBuffer.short.toUShort()
            return when (newType) {
                is ICMPv4Type -> {
                    ICMPv4Header.fromStream(byteBuffer, newType, newCode, newChecksum, order)
                }
                is ICMPv6Type -> {
                    ICMPv6Header.fromStream(byteBuffer, newType, newCode, newChecksum, order)
                }
                else -> {
                    throw PacketHeaderException("Unsupported ICMP type")
                }
            }
        }
    }

    open fun toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val buffer = ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt())
        buffer.order(order)
        buffer.put(type.value.toByte())
        buffer.put(code.toByte())
        // the checksum doesn't matter, the kernel will recompute - although we might want to
        // for testing / verification purposes
        buffer.putShort(checksum.toShort())
        return buffer.array()
    }
}