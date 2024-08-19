package com.jasonernst.example_linux

import com.jasonernst.icmp_linux.ICMPLinux
import kotlinx.coroutines.runBlocking

class PingExample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                ICMPLinux.ping("localhost")
            }
        }
    }
}