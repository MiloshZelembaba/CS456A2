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
                if (packet.getSequenceNumber() == (expectedSequenceNumber + 1)%256) {
                    expectedSequenceNumber = (expectedSequenceNumber + 1)%256;
                    System.out.println("EXPECTED... seq=" + expectedSequenceNumber);
                    // write recvInfo for the channel
                    fos.write(((DataPacket) packet).getData());
                    sendAck(expectedSequenceNumber);
                    System.out.println("sending... seq=" + expectedSequenceNumber);
                } else {
                    System.out.println("UNEXPECTED... seq=" + packet.getSequenceNumber());
                    sendAck(expectedSequenceNumber);
                    System.out.println("sending... seq=" + expectedSequenceNumber);
                }
            } else { // EOT packet received
                Packet eotPacket = createEOTPacket();
                DatagramPacket sendPacket =
                        new DatagramPacket(eotPacket.getBytes(), eotPacket.getPacketLength(), senderIPAddress, senderPort);
                senderSocket.send(sendPacket);
                senderSocket.close();

                fos.close();
                System.out.println("finished");
                break;
            }
        }
    }
}
