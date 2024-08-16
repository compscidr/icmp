package com.jasonernst.icmp_linux

import org.junit.jupiter.api.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class JVMPingTest {
    private val icmp = ICMPLinux

    @Test fun pingIpv4Localhost() {
        icmp.ping("localhost")
    }

    @Test
    fun pingIpv6Localhost() {
        icmp.ping("::1")
    }
}