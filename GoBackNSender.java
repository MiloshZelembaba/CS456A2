import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class GoBackNSender {
    private final int WINDOW_SIZE = 10;
    private int millisecondTimeout;
    private int port;
    private InetAddress IPAddress;
    private byte[] data;
    DatagramSocket clientSocket;


    public GoBackNSender(int timeout, byte[] data, String serverAddress, int port) throws Exception{
        millisecondTimeout = timeout;
        IPAddress= InetAddress.getByName(serverAddress);
        this.port = port;
        this.data = data;
        clientSocket = new DatagramSocket();
    }




    public void sendData() throws Exception{
        // TODO some sort of timer
        int sequenceCounter = 0;
        int base = 0;
        int currentSendingPos = 0;
        ArrayList<Packet> packets = new ArrayList<>(createAllPackets()); // not sure if i need to do it like this (with the copying)

        while(true){
//            if (sequenceCounter < base + WINDOW_SIZE && currentSendingPos < packets.size()){
//                sendPacket(packets.get(currentSendingPos));
//
//                if (base == currentSendingPos){
//                    // TODO start timer
//                }
//
//                currentSendingPos++;
//            }

            if (currentSendingPos < packets.size()){
                sendPacket(packets.get(currentSendingPos));
                currentSendingPos++;
            } else {
                break;
            }



        }







        // do this at the end ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Packet packet = createEOTPacket();
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }

    private ArrayList<Packet> createAllPackets() throws Exception{
        int sequenceCounter = 0;
        ArrayList<Packet> packets = new ArrayList<>();
        for (int i = 0; i < data.length; i += 500) {
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(500, data.length - i));
            for (int j = 0; j < Math.min(500, data.length - i); j++) {
                buffer.put(data[i + j]);
            }
            Packet packet = createDataPacket(buffer.array(), sequenceCounter);
            packets.add(packet);

            sequenceCounter = (sequenceCounter + 1) % 256;
            buffer.clear();
        }

        return packets;
    }

    private void sendPacket(Packet packet) throws Exception{
        int packetLength = packet.getPacketLength();
        byte[] bytes = packet.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, IPAddress, port);
        clientSocket.send(sendPacket);
    }

    private DataPacket createDataPacket(byte[] curData, int sequnceNumber) throws Exception{
        DataPacket packet = new DataPacket(sequnceNumber);
        packet.setData(curData);

        return packet;
    }

    private EndOfTransferPacket createEOTPacket(){
        EndOfTransferPacket packet = new EndOfTransferPacket();

        return packet;
    }
}
