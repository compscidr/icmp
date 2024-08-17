package com.jasonernst.icmp_common.v4

import com.jasonernst.icmp_common.ICMPHeader
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
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
        val parsedPacket = ICMPHeader.fromStream(buffer, true)
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
        val parsedPacket = ICMPHeader.fromStream(buffer, true)
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
        val parsedPacket = ICMPHeader.fromStream(buffer, true)
        assertEquals(icmpV4DestinationUnreachablePacket, parsedPacket)
    }
}