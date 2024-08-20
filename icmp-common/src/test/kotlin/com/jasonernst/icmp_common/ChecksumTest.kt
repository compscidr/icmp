package com.jasonernst.icmp_common

import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * An alternate checksum implementation to test the main checksum implementation against.
 *
 * Good explanation of the algorithm
 * https://lateblt.tripod.com/bit33.txt
 */
object ChecksumTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val stringPacketDumper = StringPacketDumper()

    /**
     * Computes a 16-bit one's complement sum of the bytes in the buffer.
     */
    private fun checksum(buffer: ByteBuffer): UShort {
        logger.debug("Calculating checksum for ${buffer.array().size} bytes")
        val dump = stringPacketDumper.dumpBufferToString(buffer, 0, buffer.limit(), true)
        logger.debug("\n$dump")
        var checksum = 0u
        val position = buffer.position()
        while (buffer.position() + 2 <= buffer.limit()) {
            val value = buffer.short.toUShort()
            logger.debug("CS: ${checksum.toString(16)}, value: ${value.toString(16)}")
            checksum += value
        }
        // handle case with an odd number of bytes
        if (buffer.hasRemaining()) {
            // shift left is required because on big endian systems, the first byte is the high byte
            // https://stackoverflow.com/questions/27264078/internet-checksum-function-move-8bits-or-not
            val value = (buffer.get().toULong() shl 8).toUShort()
            logger.debug("CS: ${checksum.toString(16)}, value: ${value.toString(16)}")
            checksum += value
        }

        // handle overflow of short (take the overflow high bits and add them to the low bits)
        val overflow = checksum shr 16
        logger.debug("CS: ${checksum.toString(16)}, overflow: ${overflow.toString(16)}")
        checksum = (checksum and 0xFFFFu) + overflow
        logger.debug("CS: ${checksum.toString(16)}")

        // flip the bits to get the one's complement, and clear the high bits
        checksum = checksum.inv() and 0xFFFFu
        logger.debug("CS: ${checksum.toString(16)}")
        buffer.position(position)
        return checksum.toUShort()
    }

    @Test
    fun testChecksum() {
        val buffer = ByteBuffer.allocate(4)
        buffer.put(0x00)
        buffer.put(0x01)
        buffer.put(0x02)
        buffer.put(0x03)
        buffer.rewind()
        val checksum = checksum(buffer)
        val checksum2 = Checksum.calculateChecksum(buffer)

        assertEquals(checksum, checksum2)
    }
}