package consistent.hashing

import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.stream.Stream

typealias Hash = Int
typealias ServerIP = String

object App {

    @JvmStatic
    fun main(args: Array<String>) {
        val ring = sortedMapOf<Hash, ServerIP>()
        val serverIPs = listOf("192.168.0.0", "192.168.0.1", "192.168.0.2")
        val hashFunction = Hashing.murmur3_32()
        serverIPs.forEach { serverIP ->
            val hash = hashFunction.hashString(serverIP, UTF_8).asInt()
            ring[hash] = serverIP
            println("server $serverIP has joined the ring with hash $hash")
        }
        val serverReceived = mutableMapOf<ServerIP, Int>()
        serverIPs.forEach { serverIP -> serverReceived[serverIP] = 0 }
        Stream.generate(UUID::randomUUID)
            .map { hashFunction.hashString(it.toString(), UTF_8).asInt() }
            .limit(1000000)
            .forEach { hash ->
                val tail = ring.tailMap(hash)
                if (tail.isEmpty()) {
                    val firstKey = ring.firstKey()
                    val serverIP = ring[firstKey]!!
                    serverReceived[serverIP] = serverReceived[serverIP]!! + 1
                } else {
                    val firstKey = tail.firstKey()
                    val serverIP = ring[firstKey]!!
                    serverReceived[serverIP] = serverReceived[serverIP]!! + 1
                }
            }
        println(serverReceived)
    }
}