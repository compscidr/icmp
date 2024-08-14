package com.jasonernst.icmp_android

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
import com.jasonernst.icmp_common.ICMP
import java.io.FileDescriptor
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

object ICMPAndroid: ICMP() {
    override fun obtainSocketIpv4Socket(): FileDescriptor {
        return socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP)
    }

    override fun obtainSocketIpv6Socket(): FileDescriptor {
        return socket(AF_INET6, SOCK_DGRAM, IPPROTO_ICMPV6)
    }


    override fun setsockoptInt(fd: FileDescriptor, level: Int, optname: Int, optval: Int): Int {
        return setsockoptInt(fd, level, optname, optval)
    }

    override fun setsocketRecvTimeout(fd: FileDescriptor, timeoutMS: Long) {
        val timeout = StructTimeval.fromMillis(timeoutMS)
        setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeout)
    }

    override fun sendto(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int
    ): Int {
        return Os.sendto(fd, buffer, flags, address, port)
    }

    override fun recvfrom(
        fd: FileDescriptor,
        buffer: ByteBuffer,
        flags: Int,
        address: InetAddress,
        port: Int
    ): Int {
        val inetSocketAddress = InetSocketAddress(address, port)
        return Os.recvfrom(fd, buffer, flags, inetSocketAddress)
    }
}