package com.jasonernst.example_linux

import com.jasonernst.icmp_linux.ICMPLinux

class PingExample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ICMPLinux.ping("localhost")
        }
    }
}