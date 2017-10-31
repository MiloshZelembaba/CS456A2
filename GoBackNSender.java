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
    DatagramSocket senderSocket;


    public GoBackNSender(int timeout, byte[] data, String serverAddress, int port) throws Exception{
        millisecondTimeout = timeout;
        IPAddress= InetAddress.getByName(serverAddress);
        this.port = port;
        this.data = data;
        senderSocket = new DatagramSocket();
        senderSocket.setSoTimeout(1);
    }




    public void sendData() throws Exception{
        int sequenceCounter = 0;
        int base = 0;
        int currentSendingPos = 0;
        long startTime = 0;
        long endTime = 0;
        int justSaw = -1;
        DatagramPacket ackPacket;
        ArrayList<Packet> packets = new ArrayList<>(createAllPackets()); // not sure if i need to do it like this (with the copying)


        while(true){
            if (base == packets.size()){
                System.out.println("all packets have been sent, sending EOT");
                break;
            }

            if (sequenceCounter < base + WINDOW_SIZE && currentSendingPos < packets.size()){
                System.out.println("SENT... seq=" + packets.get(currentSendingPos).getSequenceNumber());
                sendPacket(packets.get(currentSendingPos));

                if (base == currentSendingPos){
                    System.out.println("started timer on base="+base);
                    startTime = System.nanoTime();
                }

                currentSendingPos++;
            }

            byte[] receiveData = new byte[12];
            ackPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
            try {
                senderSocket.receive(ackPacket);
                Packet packet = Packet.toPacket(ackPacket); // converts it to one of my packets i've defined
                int ackNum = packet.getSequenceNumber();
                System.out.println("JUST SAW... seq=" + ackNum);
                if (ackNum > justSaw) {
                    justSaw = ackNum;
                    // TODO: i think the below works, need to check
                    if (ackNum >= base % 256) {
                        base += ((ackNum - base % 256) + 1);
                    } else {
                        base += ((256 - (base % 256 - ackNum)) + 1);
                    }
                    System.out.println("started timer on(tryblock) base=" + base);
                    startTime = System.nanoTime();
                }
            } catch (SocketTimeoutException e){}

            endTime = System.nanoTime();
            if ((endTime - startTime)/1000000 >= millisecondTimeout){
                System.out.println("TIMEOUT... base="+base);
                startTime = System.nanoTime();
                for (int i=base; i<currentSendingPos; i++){
                    sendPacket(packets.get(i));
                }
            }

        }







        // do this at the end ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Packet packet = createEOTPacket();
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        senderSocket.send(sendPacket);
        senderSocket.close();
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
        senderSocket.send(sendPacket);
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
