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
                    callReceiver(datagramPacket);
                }
            }
        };
        thread.start();

    }
    @Override
    public void stop(){
        this.running = false;
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
    public void sendMessage(InetAddress inetAddress, String message) {
        byte[] messageBytes = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, port);
        sendPacket(datagramPacket);
    }
}
