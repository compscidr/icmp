package com.jasonernst.icmp_common

import com.jasonernst.icmp_common.v4.ICMPv4Header
import com.jasonernst.icmp_common.v4.ICMPv4Type
import com.jasonernst.icmp_common.v6.ICMPv6Header
import com.jasonernst.icmp_common.v6.ICMPv6Type
import java.net.Inet6Address
import java.net.InetAddress
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

    abstract fun size(): Int

    /**
     * Compute the checksum for the ICMP packet. If verify is true, the checksum field will not be
     * zero'd out before calculating the checksum. This means the result should be zero if the
     * checksum is valid.
     */
    fun computeChecksum(source: InetAddress, dest: InetAddress, verify: Boolean = false): UShort {
        val pseudoHeader: ByteBuffer
        if (source is Inet6Address) {
            // icmpv6 uses the ip header in the checksum
            // see page 4: https://www.rfc-editor.org/rfc/rfc2463
            // https://networkengineering.stackexchange.com/a/22934
            // https://en.wikipedia.org/wiki/ICMPv6#Checksum
            // source address (16 bytes) + dest address (16 bytes) + 32-bit length (4 bytes) + 3 bytes of 0 + next header (1 byte)
            pseudoHeader = ByteBuffer.allocate(40 + size())
            pseudoHeader.put(source.address)
            pseudoHeader.put(dest.address)
            pseudoHeader.putInt(size())
            pseudoHeader.put(0)
            pseudoHeader.put(0)
            pseudoHeader.put(0)
            pseudoHeader.put(58)
        } else {
            // icmpv4 does not use the ip header in the checksum
            pseudoHeader = ByteBuffer.allocate(size())
        }
        val pseudoHeaderICMPStart = pseudoHeader.position()
        pseudoHeader.put(toByteArray())
        pseudoHeader.rewind()

        if (verify) {
            return Checksum.calculateChecksum(pseudoHeader)
        }
        // zero out the existing checksum
        pseudoHeader.putShort(pseudoHeaderICMPStart + ICMP_CHECKSUM_OFFSET.toInt(), 0)
        return Checksum.calculateChecksum(pseudoHeader)
    }
}