package com.jasonernst.icmp_common.v4

import com.jasonernst.icmp_common.ICMPHeader
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.nio.ByteBuffer

class ICMPv4Test {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val stringDumper = StringPacketDumper(logger)

    @Test
    fun echoPacketTest() {
        val icmPv4EchoPacket = ICMPv4EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv4EchoPacket.toByteArray())
        val stringDumper = StringPacketDumper(logger)
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, isIcmpV4 = true)
        assertEquals(icmPv4EchoPacket, parsedPacket)
    }

    @Test
    fun echoReplyTest() {
        val icmPv4EchoPacket = ICMPv4EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = true,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv4EchoPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, isIcmpV4 = true)
        assertEquals(icmPv4EchoPacket, parsedPacket)
    }

    @Test
    fun destinationUnreachableTest() {
        val icmpV4DestinationUnreachablePacket = ICMPv4DestinationUnreachablePacket(
            checksum = 0u,
            code = ICMPv4DestinationUnreachableCodes.DESTINATION_HOST_UNKNOWN,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmpV4DestinationUnreachablePacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, isIcmpV4 = true)
        assertEquals(icmpV4DestinationUnreachablePacket, parsedPacket)
    }

    @Test
    fun timeExceededTest() {
        val icmPv4TimeExceededPacket = ICMPv4TimeExceededPacket(
            checksum = 0u,
            code = ICMPv4DestinationUnreachableCodes.DESTINATION_HOST_UNKNOWN,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv4TimeExceededPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, isIcmpV4 = true)
        assertEquals(icmPv4TimeExceededPacket, parsedPacket)
    }

    @Test
    fun checksumTest() {
        val icmpV4EchoPacket = ICMPv4EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val source = InetAddress.getLocalHost()
        val checksum = icmpV4EchoPacket.computeChecksum(source, source)

        // when we have the checksum set to the correct value, computing the checksum again without
        // zero'ing it out should result in a zero checksum.
        val icmPv4EchoPacket2 = ICMPv4EchoPacket(
            checksum = checksum,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        assertEquals(0u.toUShort(), icmPv4EchoPacket2.computeChecksum(source, source, true))
    }
}