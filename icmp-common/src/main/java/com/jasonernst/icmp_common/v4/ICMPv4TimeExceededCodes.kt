package com.jasonernst.icmp_common.v4

import com.jasonernst.icmp_common.ICMPType

enum class ICMPv4TimeExceededCodes(override val value: UByte) : ICMPType {
    TTL_EXCEEDED(0U),
    FRAGMENT_REASSEMBLY_TIME_EXCEEDED(1U)
    ;

    companion object {
        fun fromValue(value: UByte) = ICMPv4DestinationUnreachableCodes.entries.first { it.value == value }
    }
}