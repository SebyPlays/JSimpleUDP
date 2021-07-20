package com.github.sebyplays.jsimpleudp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public interface ICommunicator {

    void start();

    void stop();

    void sendPacket(DatagramPacket datagramPacket);

    void callReceiver(DatagramPacket datagramPacket);

    Type getType();

    void sendMessage(InetAddress inetAddress, String message);

}
