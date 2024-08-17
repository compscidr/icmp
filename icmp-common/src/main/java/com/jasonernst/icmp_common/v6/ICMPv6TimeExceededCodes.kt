package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.ICMPType

enum class ICMPv6TimeExceededCodes(override val value: UByte) : ICMPType {
    HOP_LIMIT_EXCEEDED(0U),
    FRAGMENT_REASSEMBLY_TIME_EXCEEDED(1U)
    ;

    companion object {
        fun fromValue(value: UByte) = ICMPv6TimeExceededCodes.entries.first { it.value == value }
    }
}