import java.nio.ByteBuffer;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class DataPacket extends Packet {
    private byte[] data;
    private boolean acked;

    public DataPacket(int seqn){
        acked = false;
        packetType = toArray(Packet.DATA); // number for a data packet
        sequenceNumber = toArray(seqn);
    }

    public byte[] getBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(fromArray(packetLength));
        buffer.put(packetType);
        buffer.put(packetLength);
        buffer.put(sequenceNumber);
        buffer.put(data);
        return buffer.array();
    }

    public void ack(){
        acked = true;
    }

    public boolean wasAcked(){
        return acked;
    }

    public void setData(byte[] data) throws Exception{
        int dataLength = data.length;
        if (dataLength > 500){
            throw new Exception("Data exceeds 500 bytes, cannot add to packet");
        }

        packetLength = toArray(fromArray(packetLength) + dataLength);
        this.data = data;
    }

    public byte[] getData(){
        return data;
    }

}
