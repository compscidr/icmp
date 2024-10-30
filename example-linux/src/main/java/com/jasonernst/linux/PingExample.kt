package com.jasonernst.linux

import com.jasonernst.icmp.linux.IcmpLinux
import kotlinx.coroutines.runBlocking

class PingExample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                IcmpLinux.ping("localhost")
            }
        }
    }
}
