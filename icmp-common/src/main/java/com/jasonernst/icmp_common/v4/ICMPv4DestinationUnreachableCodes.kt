package com.jasonernst.icmp_common.v4

import com.jasonernst.icmp_common.ICMPType

// https://www.iana.org/assignments/icmp-parameters/icmp-parameters.xhtml#icmp-parameters-codes-3
enum class ICMPv4DestinationUnreachableCodes(override val value: UByte) : ICMPType {
    NETWORK_UNREACHABLE(0U),
    HOST_UNREACHABLE(1U),
    PROTOCOL_UNREACHABLE(2U),
    PORT_UNREACHABLE(3U),
    FRAGMENTATION_NEEDED_AND_DF_SET(4U),
    SOURCE_ROUTE_FAILED(5U),
    DESTINATION_NETWORK_UNKNOWN(6U),
    DESTINATION_HOST_UNKNOWN(7U),
    SOURCE_HOST_ISOLATED(8U),
    COMMUNICATION_WITH_DESTINATION_NETWORK_PROHIBITED(9U),
    COMMUNICATION_WITH_DESTINATION_HOST_PROHIBITED(10U),
    DESTINATION_NETWORK_UNREACHABLE_FOR_TYPE_OF_SERVICE(11U),
    DESTINATION_HOST_UNREACHABLE_FOR_TYPE_OF_SERVICE(12U),
    COMMUNICATION_ADMINISTRATIVELY_PROHIBITED(13U),
    HOST_PRECEDENCE_VIOLATION(14U),
    PRECEDENCE_CUTOFF_IN_EFFECT(15U),
    ;

    companion object {
        fun fromValue(value: UByte) = entries.first { it.value == value }
    }
}