# JSimpleUDP
A simple java udp socket api
----------------
PacketReceivers are Interfaces, that are being triggered, wen a packet is received. Pretty selfexplanatory.

The ICommunicator interface is applying to both the server and client.

The ICommunicator provides a start as well as a stop, sendMessage and sendPacket method.

To use this api, simply create objects of the desired instance either server or client.

And then just send packets with the send method.
