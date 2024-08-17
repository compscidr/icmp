package com.jasonernst.icmp_common.v6

import com.jasonernst.icmp_common.ICMPHeader
import com.jasonernst.icmp_common.PacketHeaderException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation of ICMPv6 header.
 *
 * Only difference is the protocol number it returns, and the ICMPTypes it uses.
 *
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
open class ICMPv6Header(
    icmPv6Type: ICMPv6Type,
    code: UByte,
    checksum: UShort
) : ICMPHeader(type = icmPv6Type, code = code, checksum = checksum) {
    companion object {
        fun fromStream(buffer: ByteBuffer, icmpV6Type: ICMPv6Type, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv6Header {
            return when (icmpV6Type) {
                ICMPv6Type.ECHO_REPLY_V6, ICMPv6Type.ECHO_REQUEST_V6 -> {
                    ICMPv6EchoPacket.fromStream(buffer, icmpV6Type, checksum, order)
                }
                ICMPv6Type.DESTINATION_UNREACHABLE -> {
                    ICMPv6DestinationUnreachablePacket.fromStream(buffer, code, checksum, order)
                }
                ICMPv6Type.TIME_EXCEEDED -> {
                    ICMPv6TimeExceededPacket.fromStream(buffer, code, checksum, order)
                }
                else -> {
                    throw PacketHeaderException("Unsupported ICMPv6 type")
                }
            }
        }
    }
}