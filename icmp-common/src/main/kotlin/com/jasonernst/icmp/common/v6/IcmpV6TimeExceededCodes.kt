package com.jasonernst.icmp.common.v6

import com.jasonernst.icmp.common.IcmpType

enum class IcmpV6TimeExceededCodes(
    override val value: UByte,
) : IcmpType {
    HOP_LIMIT_EXCEEDED(0U),
    FRAGMENT_REASSEMBLY_TIME_EXCEEDED(1U),
    ;

    companion object {
        fun fromValue(value: UByte) = IcmpV6TimeExceededCodes.entries.first { it.value == value }
    }
}
