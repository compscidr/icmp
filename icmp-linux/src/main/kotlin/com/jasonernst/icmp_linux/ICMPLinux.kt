package com.jasonernst.icmp_linux

import com.jasonernst.icmp_common.ICMP
import java.io.FileDescriptor
import java.net.InetAddress
import java.nio.ByteBuffer

object ICMPLinux: ICMP() {

    // see: netinet/in.h and sys/socket.h
    // note: these values are different on android.system.Os, and only make sense on linux
    const val AF_INET = 2
    const val AF_INET6 = 10
    const val SOCK_DGRAM = 2
    const val IPPROTO_ICMP = 1
    const val IPPROTO_ICMPV6 = 58

    /**
     * Matches the signature of the android.system.Os.socket function.
     *
     * Important: Do not use the android.system.OS constants for domain, type, and protocol, use
     * the ones defined above taken from netinet/in.h and sys/socket.h or there will be problems.
     *
     * @param domain the address family (e.g. AF_INET)
     * @param type the socket type (e.g. SOCK_DGRAM)
     * @param protocol the protocol (e.g. IPPROTO_ICMP)
     */
    external fun socket(domain: Int, type: Int, protocol: Int): FileDescriptor

    override fun obtainSocketIpv4Socket(): FileDescriptor {
        return socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP)
    }

    override fun obtainSocketIpv6Socket(): FileDescriptor {
        return socket(AF_INET6, SOCK_DGRAM, IPPROTO_ICMPV6)
    }

    /**
     * Matches the signature of the android.system.Os.setsockoptInt function.
     */
    external override fun setsockoptInt(fd: FileDescriptor, level: Int, optname: Int, optval: Int): Int

    override fun setsocketRecvTimeout(fd: FileDescriptor, timeoutMS: Long) {
        // see: sys/time.h
        val sec = timeoutMS / 1000
        val usec = (timeoutMS % 1000) * 1000
        setsocketRecvTimeout(fd, sec, usec)
    }

    override fun sendto(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int
    ): Int {
        return sendTo(fd, buffer.array(), flags, address.address, port)
    }

    override fun recvfrom(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int
    ): Int {
        return recvFrom(fd, buffer.array(), flags, address.address, port)
    }

    private external fun setsocketRecvTimeout(fd: FileDescriptor, sec: Long, usec: Long): Int
    external fun sendTo(fd: FileDescriptor, buffer: ByteArray, flags: Int, address: ByteArray, port: Int): Int
    external fun recvFrom(fd: FileDescriptor, buffer: ByteArray, flags: Int, address: ByteArray, port: Int): Int
}