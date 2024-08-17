package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.ICMPHeader
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class ICMPv6Test {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val stringDumper = StringPacketDumper(logger)

    @Test
    fun echoPacketTest() {
        val icmPv6EchoPacket = ICMPv6EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv6EchoPacket.toByteArray())
        val stringDumper = StringPacketDumper(logger)
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmPv6EchoPacket, parsedPacket)
    }

    @Test
    fun echoReplyTest() {
        val icmPv6EchoPacket = ICMPv6EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv6EchoPacket.toByteArray())

        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmPv6EchoPacket, parsedPacket)
    }

    @Test
    fun destinationUnreachableTest() {
        val icmpV6DestinationUnreachablePacket = ICMPv6DestinationUnreachablePacket(
            checksum = 0u,
            code = ICMPv6DestinationUnreachableCodes.ADDRESS_UNREACHABLE,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmpV6DestinationUnreachablePacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmpV6DestinationUnreachablePacket, parsedPacket)
    }
}