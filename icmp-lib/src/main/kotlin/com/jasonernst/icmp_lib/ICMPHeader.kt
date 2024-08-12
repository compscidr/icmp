package com.jasonernst.icmp_lib

import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ICMPHeader(val type: ICMPType, val code: UByte, val checksum: UShort) {

    companion object {
        // uByte + uByte + uShort
        const val ICMP_HEADER_MIN_LENGTH: UShort = 4u
        const val ICMP_CHECKSUM_OFFSET: UShort = 2u

        fun fromBuffer(byteBuffer: ByteBuffer, isIcmpV4: Boolean = true, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPHeader{
            byteBuffer.order(order)
            if (byteBuffer.remaining() < ICMP_HEADER_MIN_LENGTH.toInt()) {
                throw PacketHeaderException("Buffer too small")
            }
            val newType = if (isIcmpV4) ICMPv4Type.fromValue(byteBuffer.get().toUByte()) else ICMPv6Type.fromValue(byteBuffer.get().toUByte())
            val newCode = byteBuffer.get().toUByte()
            val newChecksum = byteBuffer.short.toUShort()
            return when (newType) {
                is ICMPv4Type -> {
                    ICMPv4Header.fromBuffer(byteBuffer, newType, newCode, newChecksum, order)
                }
                is ICMPv6Type -> {
                    ICMPv6Header.fromBuffer(byteBuffer, newType, newCode, newChecksum, order)
                }
                else -> {
                    throw PacketHeaderException("Unsupported ICMP type")
                }
            }
        }
    }

    open fun toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val buffer = ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt())
        buffer.order(order)
        buffer.put(type.value.toByte())
        buffer.put(code.toByte())
        // the checksum doesn't matter, the kernel will recompute - although we might want to
        // for testing / verification purposes
        buffer.putShort(checksum.toShort())
        return buffer.array()
    }
}

/**
 * Implementation of ICMP header:
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
open class ICMPv4Header(
    icmpV4Type: ICMPv4Type,
    code: UByte,
    checksum: UShort,
) : ICMPHeader(type = icmpV4Type, code = code, checksum = checksum) {
    companion object {
        fun fromBuffer(buffer: ByteBuffer, icmpV4Type: ICMPv4Type, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv4Header {
            if (icmpV4Type == ICMPv4Type.ECHO_REPLY || icmpV4Type == ICMPv4Type.ECHO_REQUEST) {
                return ICMPv4EchoPacket.fromBuffer(buffer, icmpV4Type, code, checksum, order)
            } else {
                throw PacketHeaderException("Unsupported ICMPv4 type")
            }
        }
    }
}

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
        fun fromBuffer(buffer: ByteBuffer, icmpV6Type: ICMPv6Type, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN): ICMPv6Header {
            if (icmpV6Type == ICMPv6Type.ECHO_REPLY_V6 || icmpV6Type == ICMPv6Type.ECHO_REQUEST_V6) {
                return ICMPv6EchoPacket.fromBuffer(buffer, icmpV6Type, code, checksum, order)
            } else {
                throw PacketHeaderException("Unsupported ICMPv6 type")
            }
        }
    }
}

/**
 * Minimal implementation of an ICMPv4 Echo Request/Reply packet.
 *
 * https://www.rfc-editor.org/rfc/rfc792.html page 14
 * https://www.rfc-editor.org/rfc/rfc2780.html
 *
 */
class ICMPv4EchoPacket(
    code: UByte,
    checksum: UShort,
    val sequence: UShort,
    val id: UShort,
    val isReply: Boolean = false,
    val data: ByteArray = ByteArray(0),
) : ICMPv4Header(if (isReply) ICMPv4Type.ECHO_REPLY else ICMPv4Type.ECHO_REQUEST, code, checksum) {
    companion object {
        private const val ICMP_ECHO_MIN_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromBuffer(
            buffer: ByteBuffer, icmpV4Type: ICMPv4Type, code: UByte, checksum: UShort, order: ByteOrder = ByteOrder.BIG_ENDIAN
        ): ICMPv4EchoPacket {
            buffer.order(order)
            val isReply: Boolean = icmpV4Type == ICMPv4Type.ECHO_REPLY
            if (buffer.remaining() < ICMP_ECHO_MIN_LENGTH) throw PacketHeaderException("Buffer too small")
            val sequence = buffer.short.toUShort()
            val id = buffer.short.toUShort()
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            return ICMPv4EchoPacket(code, checksum, sequence, id, isReply, data)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ICMPv4EchoPacket

        if (sequence != other.sequence) return false
        if (id != other.id) return false
        if (isReply != other.isReply) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + isReply.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_MIN_LENGTH + data.size).apply {
            order(order)
            put(super.toByteArray(order))
            putShort(sequence.toShort())
            putShort(id.toShort())
            put(data)
            return array()
        }
    }
}

class ICMPv6EchoPacket(
    code: UByte,
    checksum: UShort,
    val sequence: UShort,
    val id: UShort,
    val isReply: Boolean = false,
    val payload: ByteArray = ByteArray(0),
) : ICMPv6Header(if (isReply) ICMPv6Type.ECHO_REPLY_V6 else ICMPv6Type.ECHO_REQUEST_V6, code, checksum) {
    companion object {
        const val ICMP_ECHO_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromBuffer(
            buffer: ByteBuffer,
            icmpV6Type: ICMPv6Type,
            code: UByte,
            checksum: UShort,
            order: ByteOrder = ByteOrder.BIG_ENDIAN
        ): ICMPv6EchoPacket {
            buffer.order(order)
            val isReply: Boolean = icmpV6Type == ICMPv6Type.ECHO_REPLY_V6
            if (buffer.remaining() < ICMP_ECHO_LENGTH) throw PacketHeaderException("Buffer too small")
            val sequence = buffer.short.toUShort()
            val id = buffer.short.toUShort()
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6EchoPacket(code, checksum, sequence, id, isReply, remainingBuffer)
        }
    }

    override fun toByteArray(order: ByteOrder): ByteArray {
        ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + ICMP_ECHO_LENGTH + payload.size).apply {
            order(order)
            put(super.toByteArray(order))
            putShort(sequence.toShort())
            putShort(id.toShort())
            put(payload)
            return array()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ICMPv6EchoPacket

        if (sequence != other.sequence) return false
        if (id != other.id) return false
        if (isReply != other.isReply) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + isReply.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}