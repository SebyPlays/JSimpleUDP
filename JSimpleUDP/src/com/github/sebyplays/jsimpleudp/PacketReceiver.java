package com.github.sebyplays.jsimpleudp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public interface PacketReceiver {

    void onPacket(ICommunicator iCommunicator, DatagramPacket datagramPacket, InetAddress source, String packet, Type type);

}
