import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class GoBackNReceiver extends AbstractReceiver {

    public GoBackNReceiver(String filePath) throws Exception{
        senderSocket = createUDPSocket(); // creates a UDP socket
        this.filePath = filePath;

        // write recvInfo for the channel
        FileWriter fileWriter = new FileWriter("recvInfo");
        String serverAddress = senderSocket.getLocalAddress().toString();
        fileWriter.write(serverAddress.replace("/","") + " " + senderSocket.getLocalPort());
        fileWriter.close();
    }

    /**
     * This is where all of the logic behind GBN recieve is coded.
     * Receives data according to the GBN protocol
     * @throws Exception
     */
    public void receive() throws Exception{
        byte[] receiveData = new byte[512];
        int expectedSequenceNumber = -1;

        DatagramPacket receivePacket;
        FileOutputStream fos = new FileOutputStream(filePath,true);
        while (true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            senderSocket.receive(receivePacket);
            Packet packet = Packet.toPacket(receivePacket); // converts it to one of my packets i've defined
            senderPort = receivePacket.getPort();
            senderIPAddress = receivePacket.getAddress();


            if (packet instanceof DataPacket) {
                System.out.println("PKT RECV DATA " + packet.getPacketLength() + " " + packet.getSequenceNumber());
                if (packet.getSequenceNumber() == (expectedSequenceNumber + 1)%256) { // the seq number we're expecting to see
                    expectedSequenceNumber = (expectedSequenceNumber + 1)%256; // we saw it so increase
//                    System.out.println("EXPECTED... seq=" + expectedSequenceNumber);
                    // write recvInfo for the channel
                    fos.write(((DataPacket) packet).getData()); // Deliver the data (a.k.a write the bytes to file)
                    sendAck(expectedSequenceNumber); // send ack to sender
//                    System.out.println("sending... seq=" + expectedSequenceNumber);
                } else {
//                    System.out.println("UNEXPECTED... seq=" + packet.getSequenceNumber());
                    // this is for the case the FIRST package doesn't arrive
                    // according to a piazza post we don't want to send any sort of ack, before i was sending
                    // and ack of -1
                    if (expectedSequenceNumber != -1) {
                        sendAck(expectedSequenceNumber);
                    }
//                    System.out.println("sending... seq=" + expectedSequenceNumber);
                }
            } else { // EOT packet received
                System.out.println("PKT RECV EOT " + packet.getPacketLength() + " " + packet.getSequenceNumber());

                // send back EOT to the sender
                Packet eotPacket = createEOTPacket((expectedSequenceNumber + 1)%256);
                DatagramPacket sendPacket =
                        new DatagramPacket(eotPacket.getBytes(), eotPacket.getPacketLength(), senderIPAddress, senderPort);
                senderSocket.send(sendPacket);
                senderSocket.close();
                System.out.println("PKT SEND EOT " + eotPacket.getPacketLength() + " " + eotPacket.getSequenceNumber());



                fos.close();
                break;
            }
        }
    }
}
