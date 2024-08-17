package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.ICMPType

// https://www.iana.org/assignments/icmpv6-parameters/icmpv6-parameters.xhtml#icmpv6-parameters-codes-2
enum class ICMPv6DestinationUnreachableCodes(override val value: UByte) : ICMPType {
    NO_ROUTE_TO_DESTINATION(0U),
    COMMUNICATION_WITH_DESTINATION_ADMINISTRATIVELY_PROHIBITED(1U),
    BEYOND_SCOPE_OF_SOURCE_ADDRESS(2U),
    ADDRESS_UNREACHABLE(3U),
    PORT_UNREACHABLE(4U),
    SOURCE_ADDRESS_FAILED_INGRESS_EGRESS_POLICY(5U),
    REJECT_ROUTE_TO_DESTINATION(6U),
    ERROR_IN_SOURCE_ROUTING_HEADER(7U),
    HEADERS_TOO_LONG(8U),
    ;

    companion object {
        fun fromValue(value: UByte) = entries.first { it.value == value }
    }
}