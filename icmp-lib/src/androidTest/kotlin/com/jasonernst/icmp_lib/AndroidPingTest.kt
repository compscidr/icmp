package com.jasonernst.icmp_lib

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AndroidPingTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val icmp = ICMPAndroid

    @Test
    fun pingIpv4Localhost() {
        icmp.ping("localhost")
    }

    @Test fun pingIpv6Localhost() {
        icmp.ping("::1")
    }
}