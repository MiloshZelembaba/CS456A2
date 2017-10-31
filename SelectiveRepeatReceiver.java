import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class SelectiveRepeatReceiver extends AbstractReceiver{

    public SelectiveRepeatReceiver(String filePath) throws Exception{
        senderSocket = createUDPSocket(); // creates a UDP socket
        this.filePath = filePath;

        // write recvInfo for the channel
        FileWriter fileWriter = new FileWriter("recvInfo");
        String serverAddress = senderSocket.getLocalAddress().toString();
        fileWriter.write(serverAddress.replace("/","") + " " + senderSocket.getLocalPort());
        fileWriter.close();
    }

    public void receive() throws Exception{
        Map<Integer, DataPacket> windowBuffer = new HashMap<>();
        int base = 0;
        byte[] receiveData = new byte[512];

        DatagramPacket receivePacket;
        FileOutputStream fos = new FileOutputStream(filePath,true);
        while (true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            senderSocket.receive(receivePacket);
            Packet packet = Packet.toPacket(receivePacket); // converts it to one of my packets i've defined
            senderPort = receivePacket.getPort();
            senderIPAddress = receivePacket.getAddress();
            int[] window = generateWindow(base);
            int[] recvWindow = generateRecvWindow(base);

            if (packet instanceof DataPacket) {
                if (isIn(window, packet.getSequenceNumber())){
                    windowBuffer.put(packet.getSequenceNumber(), (DataPacket)packet);
                    sendAck(packet.getSequenceNumber());

                    if (getPos(window, packet.getSequenceNumber()) == 0){ // packet received was at the base of window
                        for (int i=0; i<WINDOW_SIZE; i++) {
                            if (windowBuffer.containsKey(window[i])) {
                                DataPacket tmp = windowBuffer.get(window[i]);
                                fos.write(tmp.getData()); // deliver data
                                base++;
                                windowBuffer.remove(window[i]);
                            } else {
                                break;
                            }
                        }
                    }
                } else if (isIn(recvWindow ,packet.getSequenceNumber())){
                    sendAck(packet.getSequenceNumber());
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
