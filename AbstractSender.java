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

    abstract public void sendData() throws Exception;

    protected ArrayList<DataPacket> createAllPackets() throws Exception{
        int sequenceCounter = 0;
        ArrayList<DataPacket> packets = new ArrayList<>();
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

    protected void sendPacket(Packet packet) throws Exception{
        int packetLength = packet.getPacketLength();
        byte[] bytes = packet.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, IPAddress, port);
        senderSocket.send(sendPacket);
    }

    protected DataPacket createDataPacket(byte[] curData, int sequnceNumber) throws Exception{
        DataPacket packet = new DataPacket(sequnceNumber);
        packet.setData(curData);

        return packet;
    }

    protected EndOfTransferPacket createEOTPacket(){
        EndOfTransferPacket packet = new EndOfTransferPacket();

        return packet;
    }

    protected int[] generateWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base + i)%256;
        }

        return result;
    }

    protected int getPos(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return i;
            }
        }

        return -1;
    }

    protected boolean isIn(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return true;
            }
        }

        return false;
    }
}
