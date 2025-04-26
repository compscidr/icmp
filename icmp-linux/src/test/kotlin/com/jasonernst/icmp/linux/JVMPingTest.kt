package com.jasonernst.icmp.linux

import com.jasonernst.icmp.common.PingResult
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class JVMPingTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val icmp = IcmpLinux

    companion object {
        // assumes this is run from within docker, or a machine with docker running
        const val UNREACHABLE_IPV4 = "180.171.171.171"

        // this should be the host machine if docker is running
        const val REACHABLE_IPV4 = "172.17.0.1"
    }

    @Test fun pingIpv4Localhost() {
        runBlocking {
            icmp.ping("localhost")
        }
    }

    @Test
    fun pingIpv6Localhost() {
        runBlocking {
            icmp.ping("::1")
        }
    }

    @Test
    fun pingReachableIpv4() {
        runBlocking {
            icmp.ping(REACHABLE_IPV4)
        }
    }

    @Test fun pingUnreachableIpv4() {
        runBlocking {
            val result = icmp.ping(UNREACHABLE_IPV4)
            assertTrue(result is PingResult.Failed)
        }
    }

    @Test fun pingUnknownHost() {
        assertThrows<UnknownHostException> {
            runBlocking {
                icmp.ping("dfadfasdf.com")
            }
        }
    }

    @Test fun pingTimeout() {
        runBlocking {
            // first do a ping with a normal timeout to make sure the host works
            var result = icmp.ping("www.gov.za", pingTimeoutMS = 5000)
            assertTrue(result is PingResult.Success)
            logger.debug("Long timeout ping result: $result")

            // now do a ping with a 1ms timeout to make sure it fails
            result = icmp.ping("www.gov.za", pingTimeoutMS = 1)
            assertTrue(result is PingResult.Failed)
        }
    }

    @Test fun pingDnsTimeout() {
        runBlocking {
            // do a request to a host we haven't resolved before so we get a cache miss
            val host = icmp.ping("www.yahoo.com", resolveTimeoutMS = 1)
            assertTrue(host is PingResult.Failed)
        }
    }

    @Test fun pingWithIdAndSequence() {
        runBlocking {
            icmp.ping(InetAddress.getLoopbackAddress(), id = 0x1234u, sequence = 0x5678u)
        }
    }
}
