package com.github.sebyplays.jsimpleudp;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server implements ICommunicator{

    @Getter private DatagramSocket datagramSocket;
    private boolean running;
    @Getter @Setter private byte[] buffer = new byte[1024];
    private Type type = Type.SERVER;
    private int port;
    @Getter @Setter private PacketReceiver packetReceiver;
    @Getter private DatagramPacket lastReceivedPacket;
    @Getter private DatagramPacket lastSentPacket;
    private long lastReceivedTime = 0;
    private long lastSentTime = 0;


    public Server(int port, PacketReceiver packetReceiver) throws SocketException {
        this.datagramSocket = new DatagramSocket(port);
        this.packetReceiver = packetReceiver;
        this.port = port;
    }

    public void start(){
        this.running = true;
        Thread thread = new Thread("udpserver"){
            @SneakyThrows
            @Override
            public void run() {
                while (running){
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
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
    public void stop(){
        this.running = false;
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
    public void sendMessage(InetAddress inetAddress, int port, String message) {
        byte[] messageBytes = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, port);
        sendPacket(datagramPacket);
    }

    @Override
    public void sendMessage(String message) {
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
