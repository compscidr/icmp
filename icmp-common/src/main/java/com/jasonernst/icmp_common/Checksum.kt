package com.jasonernst.icmp_common

import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

object Checksum {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Given the byte buffer, performs a 16-bit one's of the one's complement sum of the bytes.
     *
     * Puts the buffer back to the position it started in before returning.
     *
     * https://www.rfc-editor.org/rfc/rfc1071
     */
    fun calculateChecksum(buffer: ByteBuffer): UShort {
        val length = buffer.limit()
        val data: ByteBuffer
        if (length % 2 != 0) {
            data = ByteBuffer.allocate(length + 1)
            data.clear()
            data.put(buffer.array())
            data.position(data.position() + 1)
            data.flip()
        } else {
            data = buffer
        }
        if (data.limit() % 2 != 0) {
            throw Exception("Buffer length must be even, got ${data.limit()}")
        }

        var sum = 0u
        while (data.position() < length) {
            val value = data.short.toUShort() and 0xFFFFu
            sum += value
        }

        // carry over one's complement
        while (sum shr 16 > 0u) {
            val overflow = sum shr 16
            sum = (sum and 0xffffu) + (sum shr 16)
        }
        // flip the bit to get one' complement
        sum = sum.inv()

        return (sum and 0xFFFFu).toUShort()
    }
}