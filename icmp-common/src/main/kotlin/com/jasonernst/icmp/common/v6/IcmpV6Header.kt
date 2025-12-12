package com.jasonernst.icmp.common.v6

import com.jasonernst.icmp.common.IcmpHeader
import com.jasonernst.icmp.common.PacketHeaderException
import java.net.Inet6Address
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation of ICMPv6 header.
 *
 * Only difference is the protocol number it returns, and the ICMPTypes it uses.
 *
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
abstract class IcmpV6Header(
    private val sourceAddress: Inet6Address,
    private val destinationAddress: Inet6Address,
    icmPv6Type: IcmpV6Type,
    code: UByte,
    checksum: UShort,
) : IcmpHeader(type = icmPv6Type, code = code, checksum = checksum) {
    companion object {
        // source address (16 bytes) + dest address (16 bytes) + 32-bit length (4 bytes) + 3 bytes
        // of zeros + next header (1 byte)
        const val PSEUDOHEADER_LENGTH = 40
        const val ICMPV6_IP_TYPE: Byte = 58

        fun fromStream(
            sourceAddress: Inet6Address,
            destinationAddress: Inet6Address,
            buffer: ByteBuffer,
            limit: Int = buffer.remaining(),
            order: ByteOrder = ByteOrder.BIG_ENDIAN,
        ): IcmpV6Header {
            if (buffer.remaining() < ICMP_HEADER_MIN_LENGTH.toInt()) {
                throw PacketHeaderException("Require at least $ICMP_HEADER_MIN_LENGTH bytes to parse an IcmpV6 header")
            }
            val newType = IcmpV6Type.fromValue(buffer.get().toUByte())
            val newCode = buffer.get().toUByte()
            val newChecksum = buffer.short.toUShort()
            return when (newType) {
                IcmpV6Type.ECHO_REPLY_V6, IcmpV6Type.ECHO_REQUEST_V6 -> {
                    IcmpV6EchoPacket.fromStream(sourceAddress, destinationAddress, buffer, limit, newType, newChecksum, order)
                }

                IcmpV6Type.DESTINATION_UNREACHABLE -> {
                    IcmpV6DestinationUnreachablePacket.fromStream(
                        sourceAddress,
                        destinationAddress,
                        buffer,
                        limit,
                        newCode,
                        newChecksum,
                        order,
                    )
                }

                IcmpV6Type.TIME_EXCEEDED -> {
                    IcmpV6TimeExceededPacket.fromStream(sourceAddress, destinationAddress, buffer, limit, newCode, newChecksum, order)
                }

                IcmpV6Type.MULTICAST_LISTENER_DISCOVERY_V2 -> {
                    IcmpV6MulticastListenerDiscoveryV2.fromStream(sourceAddress, destinationAddress, buffer, limit, newChecksum, order)
                }

                IcmpV6Type.ROUTER_SOLICITATION_V6 -> {
                    IcmpV6RouterSolicitationPacket.fromStream(sourceAddress, destinationAddress, buffer, limit, newChecksum, order)
                }

                IcmpV6Type.ROUTER_ADVERTISEMENT_V6 -> {
                    IcmpV6RouterAdvertisementPacket.fromStream(sourceAddress, destinationAddress, buffer, limit, newChecksum, order)
                }

                else -> {
                    throw PacketHeaderException("Unsupported ICMPv6 type: $newType")
                }
            }
        }
    }

    fun getPseudoHeader(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val buffer = ByteBuffer.allocate(PSEUDOHEADER_LENGTH)
        buffer.order(order)
        buffer.put(sourceAddress.address)
        buffer.put(destinationAddress.address)
        buffer.putInt(size())
        buffer.put(0)
        buffer.put(0)
        buffer.put(0)
        buffer.put(ICMPV6_IP_TYPE)
        return buffer.array()
    }

    fun computeChecksum(order: ByteOrder = ByteOrder.BIG_ENDIAN): UShort {
        val buffer = ByteBuffer.wrap(toByteArray(order))
        return buffer.getShort(ICMP_CHECKSUM_OFFSET).toUShort()
    }
}
