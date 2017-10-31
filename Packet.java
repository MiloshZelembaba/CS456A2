import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by miloshzelembaba on 2017-10-30.
 *
 * Abstract packet class. This is whats sent over the network between the sender & reciever
 */
abstract public class Packet {
    public static final int DATA = 0;
    public static final int ACK = 1;
    public static final int EOT = 2;
    protected byte[] packetType;
    protected byte[] packetLength = toArray(12);
    protected byte[] sequenceNumber;


    /**
     * Converts a DatagramPacket to a Packet
     */
    public static Packet toPacket(DatagramPacket packet){
        byte[] allBytes = packet.getData();
        int pt;
        int pl;
        int seqn;

        ByteBuffer buffer = ByteBuffer.allocate(4);
        for (int i=0; i<4; i++){ // extract the packetType
            buffer.put(allBytes[i]);
        }
        pt = fromArray(buffer.array());
        buffer.clear();
//        System.out.println("packetType: " + pt);

        buffer = ByteBuffer.allocate(4);
        for (int i=4; i<8; i++){ // extract the packetLength
            buffer.put(allBytes[i]);
        }
        pl = fromArray(buffer.array());
        buffer.clear();
//        System.out.println("packetLength: " + pl);

        buffer = ByteBuffer.allocate(4);
        for (int i=8; i<12; i++){ // extract the seqN
            buffer.put(allBytes[i]);
        }
        seqn = fromArray(buffer.array());
        buffer.clear();
//        System.out.println("packetSequence: " + seqn);

        Packet receivedPacket;
        // create the packet
        if (pt == Packet.DATA){
            receivedPacket = new DataPacket(seqn);
            buffer = ByteBuffer.allocate(pl - 12);
            for (int i=12; i<pl; i++){
                buffer.put(allBytes[i]);
            }
            try {
                ((DataPacket) receivedPacket).setData(buffer.array());
            } catch (Exception e){
                System.out.println("couldn't do it");
            }
        } else if (pt == Packet.ACK){
            receivedPacket = new AcknowledgementPacket(seqn);
        } else { // treat anything else as an EOT, better for debugging?
            receivedPacket = new EndOfTransferPacket(seqn);
        }


        return receivedPacket;
    }

    /**
     * converts the packet into a byte array
     */
    public byte[] getBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(fromArray(packetLength));
        buffer.put(packetType);
        buffer.put(packetLength);
        buffer.put(sequenceNumber);
        return buffer.array();
    }

    /**
     * converts an integer value into 32-bit unsigned Big Endian
     */
    protected static byte[] toArray(int value){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        buffer.flip();
        return buffer.array();
    }

    /**
     * converts a 32-bit unsigned Big Endian to an integer
     */
    protected static int fromArray(byte[] payload){
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    public int getSequenceNumber(){
        return fromArray(sequenceNumber);
    }
    public int getPacketLength() { return fromArray(packetLength);}
}
