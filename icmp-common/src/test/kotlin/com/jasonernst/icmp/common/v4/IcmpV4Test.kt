package com.jasonernst.icmp.common.v4

import com.jasonernst.packetdumper.filedumper.TextFilePacketDumper
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.ByteBuffer

class IcmpV4Test {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val stringDumper = StringPacketDumper(logger)

    @Test
    fun echoPacketTest() {
        val icmPv4EchoPacket =
            IcmpV4EchoPacket(
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = false,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv4EchoPacket.toByteArray())
        val stringDumper = StringPacketDumper(logger)
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV4Header.fromStream(buffer)
        assertEquals(icmPv4EchoPacket, parsedPacket)
    }

    @Test
    fun echoReplyTest() {
        val icmPv4EchoPacket =
            IcmpV4EchoPacket(
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = true,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv4EchoPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV4Header.fromStream(buffer)
        assertEquals(icmPv4EchoPacket, parsedPacket)
    }

    @Test
    fun destinationUnreachableTest() {
        val icmpV4DestinationUnreachablePacket =
            IcmpV4DestinationUnreachablePacket(
                checksum = 0u,
                code = IcmpV4DestinationUnreachableCodes.DESTINATION_HOST_UNKNOWN,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmpV4DestinationUnreachablePacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV4Header.fromStream(buffer)
        assertEquals(icmpV4DestinationUnreachablePacket, parsedPacket)
    }

    @Test
    fun timeExceededTest() {
        val icmPv4TimeExceededPacket =
            IcmpV4TimeExceededPacket(
                checksum = 0u,
                code = IcmpV4DestinationUnreachableCodes.DESTINATION_HOST_UNKNOWN,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv4TimeExceededPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV4Header.fromStream(buffer)
        assertEquals(icmPv4TimeExceededPacket, parsedPacket)
    }

    @Test
    fun checksumTest() {
        val icmpV4EchoPacket =
            IcmpV4EchoPacket(
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = false,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        icmpV4EchoPacket.computeChecksum()
    }

    @Test
    fun echoRequestParsingAndChecksum() {
        val filename = "/test_packets/IcmpV4EchoRequest.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val parsedPacket = IcmpV4Header.fromStream(stream)
        assertTrue(parsedPacket is IcmpV4EchoPacket)
        logger.debug("Parsed packet: {}", parsedPacket)
        val echoPacket = parsedPacket as IcmpV4EchoPacket
        val checksum = echoPacket.checksum
        val computedChecksum = echoPacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(echoPacket.toByteArray())
        assertArrayEquals(stream.array(), buffer.array())
    }

    @Test fun echoResponseParsingAndChecksum() {
        val filename = "/test_packets/IcmpV4EchoResponse.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val parsedPacket = IcmpV4Header.fromStream(stream)
        assertTrue(parsedPacket is IcmpV4EchoPacket)
        val echoPacket = parsedPacket as IcmpV4EchoPacket
        val checksum = echoPacket.checksum
        val computedChecksum = echoPacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(echoPacket.toByteArray())
        assertArrayEquals(stream.array(), buffer.array())
    }

    @Test fun destinationUnreachableParsingAndChecksum() {
        val filename = "/test_packets/IcmpV4DestinationUnreachable.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val parsedPacket = IcmpV4Header.fromStream(stream)
        assertTrue(parsedPacket is IcmpV4DestinationUnreachablePacket)
        val destinationUnreachablePacket = parsedPacket as IcmpV4DestinationUnreachablePacket
        val checksum = destinationUnreachablePacket.checksum
        val computedChecksum = destinationUnreachablePacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(destinationUnreachablePacket.toByteArray())
        assertArrayEquals(stream.array(), buffer.array())
    }
}
