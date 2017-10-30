import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
abstract public class Packet {
    protected byte[] packetType;
    protected byte[] packetLength = toArray(12);
    protected byte[] sequenceNumber;


    public byte[] getBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(fromArray(packetLength));
        buffer.put(packetType);
        buffer.put(packetLength);
        buffer.put(sequenceNumber);
        return buffer.array();
    }

    protected byte[] toArray(int value){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        buffer.flip();
        return buffer.array();
    }

    protected int fromArray(byte[] payload){
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    public int getSequenceNumber(){
        return fromArray(sequenceNumber);
    }
    public int getPacketLength() { return fromArray(packetLength);}
}
