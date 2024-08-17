package com.jasonernst.icmp_linux

import kotlinx.coroutines.TimeoutCancellationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class JVMPingTest {
    private val icmp = ICMPLinux

    companion object {
        // assumes this is run from within docker, or a machine with docker running
        const val UNREACHABLE_IPv4 = "180.171.171.171"

        // this should be the host machine if docker is running
        const val REACHABLE_IPv4 = "172.17.0.1"
    }

    @Test fun pingIpv4Localhost() {
        icmp.ping("localhost")
    }

    @Test
    fun pingIpv6Localhost() {
        icmp.ping("::1")
    }

    @Test
    fun pingReachableIpv4() {
        icmp.ping(REACHABLE_IPv4)
    }

    @Test fun pingUnreachableIpv4() {
        assertThrows<IOException> {
            icmp.ping(UNREACHABLE_IPv4)
        }
    }

    @Test fun pingUnknownHost() {
        assertThrows<UnknownHostException> {
            icmp.ping("dfadfasdf.com")
        }
    }

    @Test fun pingTimeout() {
        // first do a ping with a normal timeout to make sure the host works
        icmp.ping("www.gov.za", pingTimeoutMS = 2000)
        // then do one with an aggressive timeout
        assertThrows<IOException> {
            icmp.ping("www.gov.za", pingTimeoutMS = 1)
        }
    }

    @Test fun pingDnsTimeout() {
        assertThrows<TimeoutCancellationException> {
            // do a request to a host we haven't resolved before so we get a cache miss
            icmp.ping("www.yahoo.com", resolveTimeoutMS = 1)
        }
    }

    @Test fun pingWithIdAndSequence() {
        icmp.ping(InetAddress.getLoopbackAddress(), id = 0x1234u, sequence = 0x5678u)
    }
}