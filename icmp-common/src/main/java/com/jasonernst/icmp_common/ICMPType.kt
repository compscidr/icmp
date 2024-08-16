package com.jasonernst.icmp_common

interface ICMPType {
    val value: UByte
}

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

// https://www.iana.org/assignments/icmpv6-parameters/icmpv6-parameters.xhtml
enum class ICMPv6Type(override val value: UByte) : ICMPType {
    RESERVED(0U),
    DESTINATION_UNREACHABLE(1U),
    PACKET_TOO_BIG(2U),
    TIME_EXCEEDED(3U),
    PARAMETER_PROBLEM(4U),
    ECHO_REQUEST_V6(128U),
    ECHO_REPLY_V6(129U),
    MULTICAST_LISTENER_QUERY(130U),
    MULTICAST_LISTENER_REPORT(131U),
    MULTICAST_LISTENER_DONE(132U),
    ROUTER_SOLICITATION_V6(133U),
    ROUTER_ADVERTISEMENT_V6(134U),
    NEIGHBOR_SOLICITATION(135U),
    REDIRECT_V6(137U),
    ROUTER_RENUMBERING(138U),
    INFORMATION_REQUEST(139U),
    INFORMATION_REPLY(140U),
    INVERSE_NEIGHBOR_DISCOVERY_SOLICITATION(141U),
    INVERSE_NEIGHBOR_DISCOVERY_ADVERTISEMENT(142U),
    MULTICAST_LISTENER_DISCOVERY_V2(143U),
    HOME_AGENT_ADDRESS_DISCOVERY_REQUEST(144U),
    HOME_AGENT_ADDRESS_DISCOVERY_REPLY(145U),
    MOBILE_PREFIX_SOLICITATION(146U),
    MOBILE_PREFIX_ADVERTISEMENT(147U),
    CERTIFICATION_PATH_SOLICITATION(148U),
    CERTIFICATION_PATH_ADVERTISEMENT(149U),
    EXPERIMENTAL_MOBILITY_PROTOCOLS(150U),
    MULTICAST_ROUTER_ADVERTISEMENT(151U),
    MULTICAST_ROUTER_SOLICITATION(152U),
    MULTICAST_ROUTER_TERMINATION(153U),
    FMIPV6_MESSAGES(154U),
    RPL_CONTROL_MESSAGE(155U),
    ILNPV6_LOCATOR_UPDATE_MESSAGE(156U),
    DUPLICATE_ADDRESS_REQUEST(157U),
    DUPLICATE_ADDRESS_CONFIRMATION(158U),
    MPL_CONTROL_MESSAGE(159U),
    EXTENDED_ECHO_REQUEST(160U),
    EXTENDED_ECHO_REPLY(161U),
    ;

    companion object {
        fun fromValue(value: UByte) = entries.first { it.value == value }
    }
}

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