package com.jasonernst.icmp.common

import com.jasonernst.icmp.common.v4.IcmpV4DestinationUnreachableCodes
import com.jasonernst.icmp.common.v4.IcmpV4DestinationUnreachablePacket
import com.jasonernst.icmp.common.v4.IcmpV4EchoPacket
import com.jasonernst.icmp.common.v6.IcmpV6EchoPacket
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.FileDescriptor
import java.net.Inet6Address
import java.net.InetAddress
import java.nio.ByteBuffer

class IcmpTest : Icmp() {
    var responsePacket: ByteBuffer? = null

    override fun obtainSocketIpv4Socket(): FileDescriptor = mockk(relaxed = true)

    override fun obtainSocketIpv6Socket(): FileDescriptor = mockk(relaxed = true)

    override fun setsockoptInt(
        fd: FileDescriptor,
        level: Int,
        optname: Int,
        optval: Int,
    ): Int = 0

    override fun setsocketRecvTimeout(
        fd: FileDescriptor,
        timeoutMS: Long,
    ) {
        return
    }

    override fun sendto(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int,
    ): Int = buffer.limit()

    override fun recvfrom(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int,
    ): Int {
        if (responsePacket != null) {
            buffer.put(responsePacket)
            buffer.rewind()
            return responsePacket!!.limit()
        }
        return 0
    }

    @AfterEach fun cleanup() {
        responsePacket = null
    }

    @Test fun testOneoffPingWithResolution() {
        runBlocking {
            ping("localhost")
            ping("::1")
        }
    }

    @Test fun testOneoffPingWithoutResolution() {
        runBlocking {
            ping(InetAddress.getLoopbackAddress())
        }
    }

    @Test fun setSockOptException() {
        runBlocking {
            val spy = spyk(IcmpTest())
            every { spy.setsockoptInt(any(), any(), any(), any()) } throws Exception("setsockoptInt")
            spy.ping("localhost")
        }
    }

    @Test fun pingFlowHost() {
        runBlocking {
            val flow = ping("localhost", count = 2)
            var count = 0
            flow.take(2).cancellable().collect {
                count++
            }
            assertEquals(2, count)
        }
    }

    @Test fun pingFlowMockedResponse() {
        runBlocking {
            responsePacket = ByteBuffer.wrap(IcmpV4EchoPacket(0u, 0u, 0u, true, ByteArray(0)).toByteArray())
            val flow = ping("localhost", count = 2)
            var count = 0
            flow.take(2).cancellable().collect {
                count++
            }
            assertEquals(2, count)
        }
    }

    @Test fun pingWithMockedPacket() {
        runBlocking {
            // everything matches, ICMPv4EchoPacket
            responsePacket = ByteBuffer.wrap(IcmpV4EchoPacket(0u, 0u, 0u, true, ByteArray(0)).toByteArray())
            var result = ping("localhost")
            assertTrue(result is PingResult.Success)

            // mismatched sequence number, ICMPv4EchoPacket
            responsePacket = ByteBuffer.wrap(IcmpV4EchoPacket(0u, 0u, 5u, true, ByteArray(0)).toByteArray())
            result = ping("localhost")
            assertTrue(result is PingResult.Failed)

            // everything matches, ICMPv6EchoPacket
            val sourceAddress = InetAddress.getByName("::1") as Inet6Address
            responsePacket = ByteBuffer.wrap(IcmpV6EchoPacket(sourceAddress, sourceAddress, 0u, 0u, 0u, true, ByteArray(0)).toByteArray())
            result = ping("::1")
            assertTrue(result is PingResult.Success)

            // mismatched sequence number, ICMPv6EchoPacket
            responsePacket = ByteBuffer.wrap(IcmpV6EchoPacket(sourceAddress, sourceAddress, 0u, 0u, 5u, true, ByteArray(0)).toByteArray())
            result = ping("::1")
            assertTrue(result is PingResult.Failed)

            // ICMPv4DestinationUnreachablePacket
            responsePacket =
                ByteBuffer.wrap(
                    IcmpV4DestinationUnreachablePacket(
                        IcmpV4DestinationUnreachableCodes.DESTINATION_HOST_UNKNOWN,
                        0u,
                        ByteArray(0),
                    ).toByteArray(),
                )
            result = ping("localhost")
            assertTrue(result is PingResult.Failed)
        }
    }

    @Test fun recvThrowsException() {
        runBlocking {
            val spy = spyk(IcmpTest())
            every { spy.recvfrom(any(), any(), any(), any(), any()) } throws Exception("recvfrom")
            var result = spy.ping("localhost")
            assertTrue(result is PingResult.Failed)

            // no exception message
            every { spy.recvfrom(any(), any(), any(), any(), any()) } throws Exception()
            result = spy.ping("localhost")
            assertTrue(result is PingResult.Failed)
        }
    }

    @Test fun pingFlowResolveException() {
        runBlocking {
            val spy = spyk(IcmpTest())
            every { spy.resolveInetAddressWithTimeout(any(), any()) } throws Exception("resolveInetAddressWithTimeout")
            val flow = spy.ping("localhost", count = 1)

            flow
                .runCatching {
                    collect {
                        val result = it
                        assertTrue(result is PingResult.Failed)
                    }
                }.onFailure {
                    // do nothing, this just catches the cancellation exception
                }
        }
    }
}
