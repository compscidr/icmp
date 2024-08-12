package com.jasonernst.icmp_lib

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val icmp = ICMPLinux

    companion object {

        @JvmStatic @BeforeAll
        fun setup(): Unit {
            System.loadLibrary("icmp-lib")
        }
    }

    @Test fun pingIpv4Localhost() {
        icmp.ping("localhost")
    }

    @Test
    fun pingIpv6Localhost() {
        icmp.ping("::1")
    }
}