/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class EndOfTransferPacket extends Packet{

    public EndOfTransferPacket(int seqn){
        packetType = toArray(Packet.EOT); // number for an acknowledgement packet
        sequenceNumber = toArray(seqn);
    }
}
