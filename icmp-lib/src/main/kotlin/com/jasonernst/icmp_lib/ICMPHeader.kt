package com.jasonernst.icmp_lib

import com.jasonernst.icmp_lib.ICMP.Companion.ICMPV6_TYPE
import com.jasonernst.icmp_lib.ICMP.Companion.ICMP_TYPE
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface ICMPHeader {
    var type: ICMPType
    var code: UByte
    var checksum: UShort
    var payload: ByteArray

    companion object {
        // uByte + uByte + uShort
        const val ICMP_HEADER_MIN_LENGTH: UShort = 4u
        const val ICMP_CHECKSUM_OFFSET: UShort = 2u
    }

    fun toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val buffer = ByteBuffer.allocate(ICMP_HEADER_MIN_LENGTH.toInt() + payload.size)
        buffer.order(order)
        buffer.put(type.value.toByte())
        buffer.put(code.toByte())
        // the checksum doesn't matter, the kernel will recompute - although we might want to
        // for testing / verification purposes
        buffer.putShort(checksum.toShort())
        buffer.put(payload)
        return buffer.array()
    }
}

/**
 * Implementation of ICMP header:
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
open class ICMPv4Header(
    var icmPv4Type: ICMPv4Type,
    override var code: UByte,
    override var checksum: UShort,
    override var payload: ByteArray,
) : ICMPHeader {
    override var type: ICMPType = icmPv4Type

    fun copy(): ICMPHeader {
        return ICMPv4Header(icmPv4Type, code, checksum, payload)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv4Header

        if (icmPv4Type != other.icmPv4Type) return false
        if (code != other.code) return false
        if (checksum != other.checksum) return false
        if (!payload.contentEquals(other.payload)) return false
        if (type != other.type) return false
        if (protocol != other.protocol) return false
        if (typeString != other.typeString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icmPv4Type.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + checksum.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + protocol.hashCode()
        result = 31 * result + typeString.hashCode()
        return result
    }

    override fun toString(): String {
        return "ICMPv4Header(icmPv4Type=$icmPv4Type, code=$code, checksum=$checksum, payload=${payload.contentToString()}"
    }

    val protocol: UByte
        get() = ICMP_TYPE
    val typeString: String
        get() = "ICMPv4"
}

/**
 * Minimal implementation of an ICMPv4 Echo Request/Reply packet.
 *
 * https://www.rfc-editor.org/rfc/rfc792.html page 14
 * https://www.rfc-editor.org/rfc/rfc2780.html
 *
 * For now, we do not implement the timestamps found on page 15.
 *
 * We can just use the ICMPv4Header to serialize this since this just builds the payload for it.
 */
data class ICMPv4EchoPacket(
    override var code: UByte,
    override var checksum: UShort,
    val sequence: UShort,
    val id: UShort,
    val isReply: Boolean = false,
    override var payload: ByteArray,
) :
    ICMPv4Header(if (isReply) ICMPv4Type.ECHO_REPLY else ICMPv4Type.ECHO_REQUEST, code, checksum, payload) {
    companion object {
        private const val ICMP_ECHO_MIN_PAYLOAD_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromByteArray(
            code: UByte,
            checksum: UShort,
            isReply: Boolean,
            byteArray: ByteArray,
        ): ICMPv4EchoPacket {
            if (byteArray.size < ICMP_ECHO_MIN_PAYLOAD_LENGTH) throw PacketHeaderException("Buffer too small")
            val buffer = ByteBuffer.wrap(byteArray)
            val sequence = buffer.short.toUShort()
            val id = buffer.short.toUShort()
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv4EchoPacket(code, checksum, sequence, id, isReply, remainingBuffer)
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

/**
 * Implementation of ICMPv6 header.
 *
 * Only difference is the protocol number it returns, and the ICMPTypes it uses.
 *
 * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol
 */
open class ICMPv6Header(
    var icmPv6Type: ICMPv6Type,
    override var code: UByte,
    override var checksum: UShort,
    override var payload: ByteArray = ByteArray(0),
) : ICMPHeader {
    override var type: ICMPType = icmPv6Type

    val protocol: UByte
        get() = ICMPV6_TYPE
    val typeString: String
        get() = "ICMPv6"

    fun copy(): ICMPHeader {
        return ICMPv6Header(icmPv6Type, code, checksum, payload)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICMPv6Header

        if (icmPv6Type != other.icmPv6Type) return false
        if (code != other.code) return false
        if (checksum != other.checksum) return false
        if (!payload.contentEquals(other.payload)) return false
        if (type != other.type) return false
        if (protocol != other.protocol) return false
        if (typeString != other.typeString) return false

        return true
    }

    override fun toString(): String {
        return "ICMPv6Header(icmPv6Type=$icmPv6Type, code=$code, checksum=$checksum, payload=${payload.contentToString()})"
    }

    override fun hashCode(): Int {
        var result = icmPv6Type.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + checksum.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + protocol.hashCode()
        result = 31 * result + typeString.hashCode()
        return result
    }
}

data class ICMPv6EchoPacket(
    override var code: UByte,
    override var checksum: UShort,
    val sequence: UShort,
    val id: UShort,
    val isReply: Boolean = false,
    override var payload: ByteArray,
) :
    ICMPv6Header(
        if (isReply) ICMPv6Type.ECHO_REPLY_V6 else ICMPv6Type.ECHO_REQUEST_V6,
        0u,
        0u,
        payload,
    ) {
    companion object {
        const val ICMP_ECHO_MINPAYLOAD_LENGTH = 4 // 2 bytes for sequence, 2 bytes for id

        fun fromByteArray(
            code: UByte,
            checksum: UShort,
            isReply: Boolean,
            byteArray: ByteArray,
        ): ICMPv6EchoPacket {
            if (byteArray.size < ICMP_ECHO_MINPAYLOAD_LENGTH) throw PacketHeaderException("Payload too small")
            val buffer = ByteBuffer.wrap(byteArray)
            val sequence = buffer.short.toUShort()
            val id = buffer.short.toUShort()
            val remainingBuffer = ByteArray(buffer.remaining())
            buffer.get(remainingBuffer)
            return ICMPv6EchoPacket(code, checksum, sequence, id, isReply, remainingBuffer)
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