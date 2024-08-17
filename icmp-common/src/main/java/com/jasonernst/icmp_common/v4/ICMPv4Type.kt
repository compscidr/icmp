package com.jasonernst.icmp_common.v4

import com.jasonernst.icmp_common.ICMPType

// https://www.iana.org/assignments/icmp-parameters/icmp-parameters.xhtml
enum class ICMPv4Type(override val value: UByte) : ICMPType {
    ECHO_REPLY(0U),
    DESTINATION_UNREACHABLE(3U),
    SOURCE_QUENCH(4U),
    REDIRECT(5U),
    ALTERNATE_HOST_ADDRESS(6U),
    ECHO_REQUEST(8U),
    ROUTER_ADVERTISEMENT(9U),
    ROUTER_SOLICITATION(10U),
    TIME_EXCEEDED(11U),
    PARAMETER_PROBLEM(12U),
    TIMESTAMP_REQUEST(13U),
    TIMESTAMP_REPLY(14U),
    INFORMATION_REQUEST(15U),
    INFORMATION_REPLY(16U),
    ADDRESS_MASK_REQUEST(17U),
    ADDRESS_MASK_REPLY(18U),
    TRACEROUTE(30U),
    DATAGRAM_CONVERSION_ERROR(31U),
    MOBILE_HOST_REDIRECT(32U),
    WHERE_ARE_YOU(33U),
    I_AM_HERE(34U),
    MOBILE_REGISTRATION_REQUEST(35U),
    MOBILE_REGISTRATION_REPLY(36U),
    DOMAIN_NAME_REQUEST(37U),
    DOMAIN_NAME_REPLY(38U),
    SKIP(39U),
    PHOTURIS(40U),
    EXTENDED_ECHO_REQUEST(42U),
    EXTENDED_ECHO_REPLY(43U),
    ;

    companion object {
        fun fromValue(value: UByte) = entries.first { it.value == value }
    }
}