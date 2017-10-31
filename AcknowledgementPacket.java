/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class AcknowledgementPacket extends Packet{

    public AcknowledgementPacket(int seqn){
        packetType = toArray(Packet.ACK); // number for an acknowledgement packet
        sequenceNumber = toArray(seqn);
    }
}
