package com.jasonernst.icmp.android

import android.system.Os
import android.system.Os.setsockoptTimeval
import android.system.Os.socket
import android.system.OsConstants
import android.system.OsConstants.AF_INET
import android.system.OsConstants.AF_INET6
import android.system.OsConstants.IPPROTO_ICMP
import android.system.OsConstants.IPPROTO_ICMPV6
import android.system.OsConstants.SOCK_DGRAM
import android.system.StructTimeval
import com.jasonernst.icmp.common.Icmp
import java.io.FileDescriptor
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

object IcmpAndroid : Icmp() {
    override fun obtainSocketIpv4Socket(): FileDescriptor = socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP)

    override fun obtainSocketIpv6Socket(): FileDescriptor = socket(AF_INET6, SOCK_DGRAM, IPPROTO_ICMPV6)

    override fun setsockoptInt(
        fd: FileDescriptor,
        level: Int,
        optname: Int,
        optval: Int,
    ): Int {
        Os.setsockoptInt(fd, level, optname, optval)
        return 0
    }

    override fun setsocketRecvTimeout(
        fd: FileDescriptor,
        timeoutMS: Long,
    ) {
        val timeout = StructTimeval.fromMillis(timeoutMS)
        setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeout)
    }

    override fun sendto(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int,
    ): Int = Os.sendto(fd, buffer, flags, address, port)

    override fun recvfrom(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int,
    ): Int {
        val inetSocketAddress = InetSocketAddress(address, port)
        return Os.recvfrom(fd, buffer, flags, inetSocketAddress)
    }
}
