package com.jasonernst.icmp.common.v4

import com.jasonernst.icmp.common.IcmpType

enum class IcmpV4TimeExceededCodes(
    override val value: UByte,
) : IcmpType {
    TTL_EXCEEDED(0U),
    FRAGMENT_REASSEMBLY_TIME_EXCEEDED(1U),
    ;

    companion object {
        fun fromValue(value: UByte) =
            com.jasonernst.icmp.common.v4.IcmpV4DestinationUnreachableCodes.entries
                .first { it.value == value }
    }
}
