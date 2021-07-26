package com.github.sebyplays.jsimpleudp;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client implements ICommunicator{

    @Getter private DatagramSocket datagramSocket;
    private boolean running;
    @Getter @Setter private byte[] buffer = new byte[1024];
    @Getter @Setter private PacketReceiver packetReceiver;
    @Getter @Setter private InetAddress host;
    private Type type = Type.CLIENT;
    @Getter private int port;
    @Getter private int listenerPort;
    @Getter private DatagramPacket lastReceivedPacket;
    @Getter private DatagramPacket lastSentPacket;
    private long lastReceivedTime = 0;
    private long lastSentTime = 0;

    @SneakyThrows
    public Client(String host, int remotePort, int listenerPort, PacketReceiver packetReceiver) {
        this.packetReceiver = packetReceiver;
        this.port = remotePort;
        this.host = InetAddress.getByName(host);
        this.datagramSocket = new DatagramSocket();
        this.listenerPort = listenerPort;
    }

    @Override
    public void start() {
        running = true;
        Thread thread = new Thread("udpclient"){
            @SneakyThrows
            @Override
            public void run() {
                while (running){
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, host, listenerPort);
                    datagramSocket.receive(datagramPacket);
                    lastReceivedPacket = datagramPacket;
                    lastReceivedTime = System.currentTimeMillis();
                    callReceiver(datagramPacket);
                }
            }
        };
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
        if(!datagramSocket.isClosed()){
            datagramSocket.close();
        }
    }

    public void sendPacket(DatagramPacket datagramPacket){
        try {
            datagramSocket.send(datagramPacket);
            lastSentPacket = datagramPacket;
            lastSentTime = System.currentTimeMillis();
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
    public void sendMessage(InetAddress inetAddress, int port, String message){
        byte[] messageBytes = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, port);
        sendPacket(datagramPacket);
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(getHost(), getPort(), message);
    }

    @Override
    public long getLastReceivedTime() {
        return System.currentTimeMillis() - lastReceivedTime;
    }

    @Override
    public long getLastSentTime() {
        return System.currentTimeMillis() - lastSentTime;
    }

}
