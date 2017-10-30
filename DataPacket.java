import java.nio.ByteBuffer;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class DataPacket extends Packet {
    private byte[] data;

    public DataPacket(int seqn){
        packetType = toArray(0); // number for a data packet
        sequenceNumber = toArray(seqn);
    }

    public byte[] getBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(fromArray(packetLength));
        buffer.put(packetType);
        buffer.put(packetLength);
        buffer.put(sequenceNumber);
        buffer.put(data);
        System.out.println("THE DATAPACKET VERSION OF GETBYTES CALLED!");
        return buffer.array();
    }

    public void setdata(byte[] data) throws Exception{
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
