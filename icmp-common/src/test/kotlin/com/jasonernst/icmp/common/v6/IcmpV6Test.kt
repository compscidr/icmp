package com.jasonernst.icmp.common.v6

import com.jasonernst.packetdumper.filedumper.TextFilePacketDumper
import com.jasonernst.packetdumper.stringdumper.StringPacketDumper
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.Inet6Address
import java.net.InetAddress
import java.nio.ByteBuffer

class IcmpV6Test {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val stringDumper = StringPacketDumper(logger)
    private val sourceAddress = InetAddress.getByName("::1") as Inet6Address

    @Test
    fun echoPacketTest() {
        val icmPv6EchoPacket =
            IcmpV6EchoPacket(
                sourceAddress,
                sourceAddress,
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = false,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv6EchoPacket.toByteArray())
        val stringDumper = StringPacketDumper(logger)
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmPv6EchoPacket, parsedPacket)
    }

    @Test
    fun echoReplyTest() {
        val icmPv6EchoPacket =
            IcmpV6EchoPacket(
                sourceAddress,
                sourceAddress,
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = false,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv6EchoPacket.toByteArray())

        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmPv6EchoPacket, parsedPacket)
    }

    @Test
    fun destinationUnreachableTest() {
        val icmpV6DestinationUnreachablePacket =
            IcmpV6DestinationUnreachablePacket(
                sourceAddress,
                sourceAddress,
                checksum = 0u,
                code = IcmpV6DestinationUnreachableCodes.ADDRESS_UNREACHABLE,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmpV6DestinationUnreachablePacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmpV6DestinationUnreachablePacket, parsedPacket)
    }

    @Test
    fun timeExceededTest() {
        val icmPv6TimeExceededPacket =
            IcmpV6TimeExceededPacket(
                sourceAddress,
                sourceAddress,
                checksum = 0u,
                code = IcmpV6TimeExceededCodes.HOP_LIMIT_EXCEEDED,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmPv6TimeExceededPacket.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmPv6TimeExceededPacket, parsedPacket)
    }

    @Test
    fun multicastListenerDiscoveryV2Test() {
        val icmpV6MulticastListenerDiscoveryV2 =
            IcmpV6MulticastListenerDiscoveryV2(
                sourceAddress,
                sourceAddress,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmpV6MulticastListenerDiscoveryV2.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmpV6MulticastListenerDiscoveryV2, parsedPacket)
    }

    @Test
    fun routerAdvertisementTest() {
        val icmpV6RouterAdvertisement =
            IcmpV6RouterAdvertisementPacket(
                sourceAddress,
                sourceAddress,
                curHopLimit = 64u,
                M = true,
                O = false,
                routerLifetime = 1800u,
                reachableTime = 0u,
                retransTimer = 9u,
                options = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmpV6RouterAdvertisement.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmpV6RouterAdvertisement, parsedPacket)
    }

    @Test
    fun routerSolicitationTest() {
        val icmpV6RouterSolicitation =
            IcmpV6RouterSolicitationPacket(
                sourceAddress,
                sourceAddress,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        val buffer = ByteBuffer.wrap(icmpV6RouterSolicitation.toByteArray())
        stringDumper.dumpBuffer(buffer, 0, buffer.limit())
        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, buffer)
        assertEquals(icmpV6RouterSolicitation, parsedPacket)
    }

    @Test
    fun checksumTest() {
        val icmpV6EchoPacket =
            IcmpV6EchoPacket(
                sourceAddress,
                sourceAddress,
                checksum = 0u,
                sequence = 5u,
                id = 3u,
                isReply = false,
                data = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            )
        icmpV6EchoPacket.computeChecksum()
    }

    @Test
    fun echoRequestParsingAndChecksum() {
        val filename = "/test_packets/IcmpV6EchoRequest.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, stream)
        assertTrue(parsedPacket is IcmpV6EchoPacket)
        logger.debug("Parsed packet: {}", parsedPacket)
        val echoPacket = parsedPacket as IcmpV6EchoPacket
        val checksum = echoPacket.checksum
        val computedChecksum = echoPacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(echoPacket.toByteArray())

        val stringPacketDumper = StringPacketDumper(logger)
        stringPacketDumper.dumpBuffer(buffer, 0, buffer.limit())

        assertArrayEquals(stream.array(), buffer.array())
    }

    @Test fun echoResponseParsingAndChecksum() {
        val filename = "/test_packets/IcmpV6EchoResponse.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val parsedPacket = IcmpV6Header.fromStream(sourceAddress, sourceAddress, stream)
        assertTrue(parsedPacket is IcmpV6EchoPacket)
        logger.debug("Parsed packet: {}", parsedPacket)
        val echoPacket = parsedPacket as IcmpV6EchoPacket
        val checksum = echoPacket.checksum
        val computedChecksum = echoPacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(echoPacket.toByteArray())

        val stringPacketDumper = StringPacketDumper(logger)
        stringPacketDumper.dumpBuffer(buffer, 0, buffer.limit())

        assertArrayEquals(stream.array(), buffer.array())
    }

    @Test fun destinationUnreachableParsingAndChecksum() {
        val filename = "/test_packets/IcmpV6DestinationUnreachable.dump"
        val resource =
            javaClass.getResource(filename)
                ?: throw FileNotFoundException("Could not find test dump: $filename")
        val stream = TextFilePacketDumper.parseFile(resource.file, true)
        logger.debug("Read buffer length: {}", stream.limit())

        val source = InetAddress.getByName("2400:cb00:465:1024::ac45:85d5") as Inet6Address
        val destination = InetAddress.getByName("2601:646:ca82:b540:e3d3:e933:582b:8b4c") as Inet6Address
        val parsedPacket = IcmpV6Header.fromStream(source, destination, stream)
        assertTrue(parsedPacket is IcmpV6DestinationUnreachablePacket)
        val destinationUnreachablePacket = parsedPacket as IcmpV6DestinationUnreachablePacket
        val checksum = destinationUnreachablePacket.checksum
        val computedChecksum = destinationUnreachablePacket.computeChecksum()
        assertEquals(checksum, computedChecksum)

        // make sure that when we convert the packet back to a byte array, it matches the original
        val buffer = ByteBuffer.wrap(destinationUnreachablePacket.toByteArray())
        assertArrayEquals(stream.array(), buffer.array())
    }
}
