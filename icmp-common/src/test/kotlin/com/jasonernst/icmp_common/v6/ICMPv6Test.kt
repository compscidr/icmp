package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.ICMPHeader
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.InetAddress
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

    @Test
    fun timeExceededTest() {
        val icmPv6TimeExceededPacket = ICMPv6TimeExceededPacket(
            checksum = 0u,
            code = ICMPv6TimeExceededCodes.HOP_LIMIT_EXCEEDED,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmPv6TimeExceededPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmPv6TimeExceededPacket, parsedPacket)
    }

    @Test
    fun multicastListenerDiscoveryV2Test() {
        val icmpV6MulticastListenerDiscoveryV2 = ICMPv6MulticastListenerDiscoveryV2(data = byteArrayOf(0x01, 0x02, 0x03, 0x04))
        val buffer = ByteBuffer.wrap(icmpV6MulticastListenerDiscoveryV2.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmpV6MulticastListenerDiscoveryV2, parsedPacket)
    }

    @Test
    fun routerAdvertisementTest() {
        val icmpV6RouterAdvertisement = ICMPv6RouterAdvertisementPacket(
            curHopLimit = 64u,
            M = true,
            O = false,
            routerLifetime = 1800u,
            reachableTime = 0u,
            retransTimer = 9u,
            options = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmpV6RouterAdvertisement.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmpV6RouterAdvertisement, parsedPacket)
    }

    @Test
    fun routerSolicitationTest() {
        val icmpV6RouterSolicitation = ICMPv6RouterSolicitationPacket(
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val buffer = ByteBuffer.wrap(icmpV6RouterSolicitation.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = ICMPHeader.fromStream(buffer, false)
        assertEquals(icmpV6RouterSolicitation, parsedPacket)
    }

    @Test
    fun checksumTest() {
        val icmpV6EchoPacket = ICMPv6EchoPacket(
            checksum = 0u,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        val source = InetAddress.getLocalHost()
        val checksum = icmpV6EchoPacket.computeChecksum(source, source)\
        val icmPv6EchoPacket2 = ICMPv6EchoPacket(
            checksum = checksum,
            sequence = 5u,
            id = 3u,
            isReply = false,
            data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
        assertEquals(0u.toUShort(), icmPv6EchoPacket2.computeChecksum(source, source, true))
    }
}