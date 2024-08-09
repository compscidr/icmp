package com.jasonernst.icmp_lib

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val icmp = ICMP()

    @Test fun pingIpv4Localhost() {
        icmp.ping("localhost")
    }

    @Test fun pingIpv6Localhost() {
        icmp.ping("::1")
    }
}