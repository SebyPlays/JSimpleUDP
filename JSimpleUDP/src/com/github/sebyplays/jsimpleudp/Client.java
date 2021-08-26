package com.github.sebyplays.jsimpleudp;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client implements ICommunicator{

    @Getter private DatagramSocket datagramSocket;
    private boolean running;
    @Getter @Setter private byte[] buffer = new byte[1024];
    @Getter @Setter private PacketReceiver primaryReceiver;
    @Getter @Setter private InetAddress host;
    private Type type = Type.CLIENT;
    @Getter private int port;
    @Getter private int listenerPort;
    @Getter private DatagramPacket lastReceivedPacket;
    @Getter private DatagramPacket lastSentPacket;
    private long lastReceivedTime = System.currentTimeMillis();
    private long lastSentTime = System.currentTimeMillis();
    @Getter private ArrayList<PacketReceiver> receivers = new ArrayList<>();

    @Getter private DatagramPacket tempPacket;

    @SneakyThrows
    public Client(String host, int remotePort, int listenerPort, PacketReceiver packetReceiver) {
        this.primaryReceiver = packetReceiver;
        this.receivers.add(primaryReceiver);
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
                    callReceiver(datagramPacket);
                    lastReceivedTime = System.currentTimeMillis();
                    tempPacket = datagramPacket;
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
        receivers.forEach(receiver -> receiver.onPacket(this, datagramPacket, datagramPacket.getAddress(),
                new String(datagramPacket.getData(), 0, datagramPacket.getLength()), this.getType()));
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

    @SneakyThrows
    public DatagramPacket sendMessageCallBack(String message){
        sendMessage(message);
        int timeOut = 0;
        while (tempPacket == null){
            if(timeOut <= 20){
                timeOut++;
                TimeUnit.SECONDS.sleep(1);
            } else {
                return null;
            }
        }
        DatagramPacket response = tempPacket;
        tempPacket = null;
        return response;
    }

    @Override
    public long getLastReceivedTime() {
        return System.currentTimeMillis() - lastReceivedTime;
    }

    @Override
    public long getLastSentTime() {
        return System.currentTimeMillis() - lastSentTime;
    }

    public void registerReceiver(PacketReceiver packetReceiver){
        if(!receivers.contains(packetReceiver)){
            this.receivers.remove(packetReceiver);
        }
    }

    public void unregisterReceiver(PacketReceiver packetReceiver){
        if(receivers.contains(packetReceiver)){
            this.receivers.remove(packetReceiver);
        }
    }

}
