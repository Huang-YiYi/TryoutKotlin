package com.example.app

import com.example.app.model.DiscoveryQueryResponse
import com.fasterxml.jackson.databind.ObjectMapper
import de.siegmar.fastcsv.reader.CsvReader
import java.io.File
import java.io.IOException
import java.net.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and

fun macString(mac: ByteArray): String {
        val locallyAssigned = (mac[0] and 2).toInt() == 2
        if (locallyAssigned) {
                mac[0] = (mac[0] and 2.inv())
        }
        val sb = StringBuilder()
        for (i in mac.indices) {
                sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) "-" else ""))
        }
        return sb.toString()
}

private val VM_ORG_OUI = arrayOf(
        "00-50-56", // VMware ESX 3, Server, Workstation, Player
        "00-0C-29", // """
        "00-05-69", // """
        "00-03-FF", // Microsoft Hyper-V, Virtual Server, Virtual PC
        "00-1C-42", // Parallells Desktop, Workstation, Server, Virtuozzo
        "00-0F-4B", // Red Hat Xen, Oracle VM, XenSource
        "00-16-3E", // Novell Xen
        "08-00-27" // Virtualbox
)

fun isVmAdapter(macString: String): Boolean {
        // check the first 3 octets
        return VM_ORG_OUI.contains(macString.substring(0, 8))
}

var SCRIPT_PATH = "/Users/eleven/Desktop/Actor-Forty_2017_min-_170127_ .csv"
private const val MULTICAST_ADDRESS = "239.255.100.100" // Chosen at random from local network block at http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xhtml
private const val MULTICAST_PORT = 4446
private const val QUERY_COMMAND = "CLOSEDCAPTIONSERVICE_QUERY"

const val SERVICE_PORT = 1337

class ServerThread(private val addresses: List<InetAddress>): Thread() {
        private var listening = false
        private val objectMapper = ObjectMapper()

        override fun start() {
                super.start()
                listening = true
        }

        override fun run() {
                super.run()
                val s = MulticastSocket(MULTICAST_PORT)
                val interfaceAddress = addresses[0]
                s.`interface` = interfaceAddress
                s.soTimeout = 10000 // 10s

                try {
                        val group = InetAddress.getByName(MULTICAST_ADDRESS)
                        s.joinGroup(group)
                        println("Joined multicast group interface address: $interfaceAddress")
                } catch(e: IOException) {
                        println("Failed to join multicast group interface address: $interfaceAddress")
                        return
                }
                while (listening) {
                        val recData = ByteArray(100)
                        val receivePacket = DatagramPacket(recData, recData.size)
                        try {
                                s.receive(receivePacket)
                        } catch (e: SocketTimeoutException) {
                                if (!listening) {
                                        s.close()
                                        break
                                }
                                continue
                        }

                        if (receivePacket.length > recData.size) {
                                print("receive size too big skipping")
                                continue
                        }

                        val strrec = String(recData, 0, receivePacket.length)
                        print("server received: $strrec")
                        print("from: " + receivePacket.getAddress().toString())
                        if (strrec == QUERY_COMMAND) {
                                val hostAddresses = addresses.map {address ->
                                        println(address.hostAddress)
                                        if (address is Inet6Address) {
                                                return@map address.hostAddress.replace(Regex("%\\w+\\d+$"), "")
                                        }
                                        return@map address.hostAddress
                                }
                                val msgData = objectMapper.writeValueAsBytes(DiscoveryQueryResponse(hostAddresses, SERVICE_PORT))
                                val msgPacket = DatagramPacket(
                                        msgData,
                                        msgData.size,
                                        receivePacket.getAddress(),
                                        receivePacket.getPort()
                                )
                                s.send(msgPacket)
                                print("server sent: ${msgData}\n")
                        } else {
                                print("Didn't send; unrecognized message.")
                        }
                }
        }

        fun stopListening() {
                listening = false
        }
}

fun main() {
        val bindings = mutableListOf<ServerThread>()
        val interfaces = NetworkInterface.getNetworkInterfaces()

        MyPanel.open()


        while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                // drop inactive
                if (!networkInterface.isUp || networkInterface.isLoopback) continue

                // check vm adapter
                if (networkInterface.hardwareAddress != null) {
                        val macString = macString(networkInterface.hardwareAddress)
                        println("mac string: " + macString)
                        if (isVmAdapter(macString)) continue
                }

                for (interfaceAddress in networkInterface.interfaceAddresses) {
                        println(
                                String.format(
                                        "NetInterface: name [%s], ip [%s]",
                                        networkInterface.displayName, interfaceAddress.address.hostAddress
                                )
                        )
                }

                val server = ServerThread(networkInterface.inetAddresses.toList())
                server.start()
                bindings.add(server)
        }

        val lines: List<String> by lazy {
                val file = File(SCRIPT_PATH)
                val csvReader = CsvReader()

                val csv = csvReader.read(file, StandardCharsets.UTF_8)
                csv.rows.map {
                        it.getField(0)
                }
        }
        var currentLine = 0

        val service = ClosedCaptionService(SERVICE_PORT, object: ClosedCaptionService.ClientRequestListener {
                override fun onRequestScript(): List<String> {
                        return lines
                }

                override fun onRequestCurrentLineNumber(): Int {
                        return currentLine
                }
        })
        service.start()

        // Wait a bit
        val showCommands = {
                println("Type in commands")
                println("q - quit")
                println("n - next line")
                println("p - prev line")
        }
        showCommands()

        val scanner = Scanner(System.`in`)
        loop@ while (scanner.hasNext()) {
                when (scanner.next()) {
                        "q" -> {
                                break@loop
                        }
                        "n" -> {
                                service.setCurrentLineNumber(++currentLine)
                        }
                        "p" -> {
                                service.setCurrentLineNumber(--currentLine)
                        }
                        else -> {
                                println("invalid command")
                        }
                }
                showCommands()
        }
        println("Exiting")

        // Unregister all services
        for (server in bindings) {
                server.stopListening()
                server.join()
        }
        service.close()
}
