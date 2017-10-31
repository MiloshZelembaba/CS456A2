import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by miloshzelembaba on 2017-10-31.
 */
abstract public class AbstractSender {
    protected final int WINDOW_SIZE = 10;
    protected int millisecondTimeout;
    protected DatagramSocket senderSocket;
    protected byte[] data;
    protected int port;
    protected InetAddress IPAddress;

    // implemented by the respective protocol
    abstract public void sendData() throws Exception;

    /**
     * Creates an ArrayList with all of the DataPackets that will be sent to the reciever.
     * All the DataPackets will have the sequence number assigned to them
     */
    protected ArrayList<DataPacket> createAllPackets() throws Exception{
        int sequenceCounter = 0;
        ArrayList<DataPacket> packets = new ArrayList<>();

        // Divide the data into (max)500byte chunks and add it to the array
        for (int i = 0; i < data.length; i += 500) {
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(500, data.length - i));
            for (int j = 0; j < Math.min(500, data.length - i); j++) {
                buffer.put(data[i + j]);
            }
            DataPacket packet = createDataPacket(buffer.array(), sequenceCounter);
            packets.add(packet);

            sequenceCounter = (sequenceCounter + 1) % 256;
            buffer.clear();
        }

        return packets;
    }

    /**
     * Sends a packet to the reciever
     */
    protected void sendPacket(Packet packet) throws Exception{
        int packetLength = packet.getPacketLength();
        byte[] bytes = packet.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, IPAddress, port);
        senderSocket.send(sendPacket);
        System.out.println("PKT SEND DAT " + packetLength + " " + packet.getSequenceNumber());
    }

    /**
     * Creates a DataPacket
     * @param curData ~~ the data to be placed in the packet
     * @param sequnceNumber ~~ the sequence number of the packet
     */
    protected DataPacket createDataPacket(byte[] curData, int sequnceNumber) throws Exception{
        DataPacket packet = new DataPacket(sequnceNumber);
        packet.setData(curData);

        return packet;
    }

    /**
     * Creates an EOTPacket
     */
    protected EndOfTransferPacket createEOTPacket(int seqn){
        EndOfTransferPacket packet = new EndOfTransferPacket(seqn);

        return packet;
    }

    /**
     * generates the sequence numbers of the current sender window (e.g {254,255,0,1,2,3,4,5,6,7})
     * @param base
     * @return
     */
    protected int[] generateWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base + i)%256;
        }

        return result;
    }

    /**
     * Gets the position of num in windowSlider
     */
    protected int getPos(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return i;
            }
        }

        return -1;
    }

    /**
     * returns true if num is in windowSlider
     */
    protected boolean isIn(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return true;
            }
        }

        return false;
    }
}
