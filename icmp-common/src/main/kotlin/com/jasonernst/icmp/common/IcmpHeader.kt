package com.jasonernst.icmp.common

import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class IcmpHeader(
    val type: IcmpType,
    val code: UByte,
    var checksum: UShort,
) {
    companion object {
        // type (1), code (1), checksum (2)
        const val ICMP_HEADER_MIN_LENGTH: UShort = 4u
        const val ICMP_CHECKSUM_OFFSET = 2
    }

    open fun toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val buffer = ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt())
        buffer.order(order)
        buffer.put(type.value.toByte())
        buffer.put(code.toByte())
        // for ICMP packets sent out over the userspace API, the checksum will be recomputed
        // however, if we're generating these packets and sending them out to a VPN or something
        // we need the checksum to be correct because it won't be sent out over the userspace API.

        // also note, the checksum is different in each ICMP packet that extends this class because
        // it must be computed over the header (and data). According to RFC792, if the total length
        // is odd, the "received data" is padding with one octect of zeros for computing the
        // checksum.

        // The procedure for computing the checksum and putting it into the buffer is as followsL:
        // 1) allocate the ByteBuffer for the ICMP packet + data
        // 2) call super.toByteArray(), which will put the type and code into the buffer, and zero
        //    the checksum.
        // 3) put the rest of the header specific to the particular ICMP packet into the buffer
        // 4) put the data into the buffer
        // 5) compute the checksum over the buffer
        // 6) put the checksum into the buffer at CHECKSUM_OFFSET
        buffer.putShort(0)
        return buffer.array()
    }

    abstract fun size(): Int
}
