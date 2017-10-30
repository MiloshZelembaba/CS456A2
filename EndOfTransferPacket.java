/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class EndOfTransferPacket extends Packet{

    public EndOfTransferPacket(){
        packetType = toArray(2); // number for an acknowledgement packet
        sequenceNumber = toArray(-1);
    }
}
