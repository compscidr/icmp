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
        const val UNREACHABLE_IPV4 = "169.254.0.10"

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
            // Test with localhost and reasonable timeout - should succeed
            var result = icmp.ping("localhost", pingTimeoutMS = 5000)
            assertTrue(result is PingResult.Success)
            logger.debug("Localhost ping result: $result")

            // Test with non-routable IP and very short timeout - should fail due to timeout
            // Using 192.168.1.1 which is a common private IP that won't be routable in GitHub Actions
            // GitHub Actions networking causes this to timeout after the specified duration
            result = icmp.ping("192.168.1.1", pingTimeoutMS = 1)
            assertTrue(result is PingResult.Failed)
            logger.debug("Timeout test result: $result")
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
