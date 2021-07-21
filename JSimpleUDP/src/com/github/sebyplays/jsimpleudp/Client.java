package com.github.sebyplays.jsimpleudp;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client implements ICommunicator{

    @Getter private DatagramSocket datagramSocket;
    private boolean running;
    @Getter @Setter private byte[] buffer = new byte[1024];
    @Getter @Setter private PacketReceiver packetReceiver;
    @Getter @Setter private InetAddress host;
    private Type type = Type.CLIENT;
    @Getter private int port;
    @Getter private DatagramPacket lastReceivedPacket;

    @SneakyThrows
    public Client(String host, int port, PacketReceiver packetReceiver) {
        this.datagramSocket = new DatagramSocket();
        this.packetReceiver = packetReceiver;
        this.port = port;
        this.host = InetAddress.getByName(host);
    }

    @Override
    public void start() {
        running = true;
        Thread thread = new Thread("udpclient"){
            @SneakyThrows
            @Override
            public void run() {
                while (running){
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, host, port);
                    datagramSocket.receive(datagramPacket);
                    lastReceivedPacket = datagramPacket;
                    callReceiver(datagramPacket);
                }
            }
        };
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
        datagramSocket.close();
    }

    public void sendPacket(DatagramPacket datagramPacket){
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callReceiver(DatagramPacket datagramPacket){
        packetReceiver.onPacket(this, datagramPacket, datagramPacket.getAddress(),
                new String(datagramPacket.getData(), 0, datagramPacket.getLength()), this.getType());
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public void sendMessage(InetAddress inetAddress, String message){
        byte[] messageBytes = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, port);
        sendPacket(datagramPacket);
    }
}
