package com.jasonernst.icmp_lib

import android.os.Build
import android.system.Os
import android.system.Os.recvfrom
import android.system.Os.sendto
import android.system.OsConstants
import android.system.Os.setsockoptInt
import android.system.Os.setsockoptTimeval
import android.system.Os.socket
import android.system.OsConstants.AF_INET
import android.system.OsConstants.AF_INET6
import android.system.OsConstants.IPPROTO_ICMP
import android.system.OsConstants.IPPROTO_ICMPV6
import android.system.OsConstants.IPPROTO_IP
import android.system.OsConstants.SOCK_DGRAM
import android.system.StructTimeval
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.io.FileDescriptor
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketOptions.IP_TOS
import java.nio.ByteBuffer

class ICMP {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ICMP_PORT = 7
        const val ICMP_TYPE: UByte = 1u
        const val ICMPV6_TYPE: UByte = 58u
    }

    /**
     * Pings a hostname - which could be an IP address or a domain name. There are two separate
     * timeouts here - one for the name resolution and one for the ping itself. Note that this
     * function will run at worst resolveTimeoutMS + pingTimeoutMS.
     */
    fun ping(host: String, resolveTimeoutMS: Long = 1000, pingTimeoutMS: Long = 1000) {
        runBlocking {
            val inetAddress: InetAddress = withTimeout(resolveTimeoutMS) {
                InetAddress.getByName(host)
            }
            logger.debug("Resolved $host to ${inetAddress.hostAddress}")
            ping(inetAddress, pingTimeoutMS)
        }
    }

    /**
     * Pings an InetAddress (Ipv4 or Ipv6) with a timeout.
     */
    fun ping(inetAddress: InetAddress, timeoutMS: Long = 1000) {
        val fd: FileDescriptor = if (inetAddress is Inet4Address) {
            logger.debug("ping4 to ${inetAddress.hostAddress}")
            socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP)
        } else {
            logger.debug("ping6 to ${inetAddress.hostAddress}")
            socket(AF_INET6, SOCK_DGRAM, IPPROTO_ICMPV6)
        }

        // can only call this function on android O and higher
        // see: https://datatracker.ietf.org/doc/html/rfc791 page 12, setting bit3 to 1 means low delay
        // setsockoptInt(fd, IPPROTO_IP, IP_TOS, 0b10000)

        // can only call on android Q and higher
        val timeout = StructTimeval.fromMillis(timeoutMS)
        setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeout)

        // todo: construct an ICMP packet
        // https://man7.org/linux/man-pages/man2/sendto.2.html
        // this will return -1 on error and errno set
        val identifier: UShort = 1u
        val sequence: UShort = 1u
        val icmpHeader = if (inetAddress is Inet4Address) {
            ICMPv4EchoPacket(
                0u,
                0u,
                sequence,
                identifier,
                false,
                ByteArray(0),
            )
        } else {
            ICMPv6EchoPacket(
                0u,
                0u,
                sequence,
                identifier,
                false,
                ByteArray(0),
            )
        }
        val bytesToSend = ByteBuffer.wrap(icmpHeader.toByteArray())
        logger.debug("bytesToSend: ${icmpHeader.toByteArray().size}")
        val bytesSent = sendto(fd, bytesToSend, 0, inetAddress, ICMP_PORT)
        logger.debug("bytesSent: $bytesSent")

        val recvArray = ByteArray(1024)

        // may also be able to use null. We want to filter to only the address we sent it to so
        // someone else can't send us a response and confuse the result
        // https://man7.org/linux/man-pages/man2/recvfrom.2.html
        // this will return 0 when the socket is EOF and -1 on error with errno set
        val bytesRecieved = recvfrom(fd, recvArray, 0, recvArray.size, 0, InetSocketAddress(inetAddress, ICMP_PORT))
        logger.debug("bytesRecieved: $bytesRecieved")
    }
}