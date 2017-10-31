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

    /**
     * This is where the logic of the SR receiver is coded.
     * Recieves data according the the SR protocol
     */
    public void receive() throws Exception{
        Map<Integer, DataPacket> windowBuffer = new HashMap<>(); // this map is representative of the current buffer
        int base = 0;
        byte[] receiveData = new byte[512];

        DatagramPacket receivePacket;
        FileOutputStream fos = new FileOutputStream(filePath,true);
        while (true) {
            // receive packet
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            senderSocket.receive(receivePacket);
            Packet packet = Packet.toPacket(receivePacket); // converts it to one of my packets i've defined
            senderPort = receivePacket.getPort();
            senderIPAddress = receivePacket.getAddress();

            int[] window = generateWindow(base); // {base -> base + WindowSize}
            int[] recvWindow = generateRecvWindow(base); // {base - windowSize -> base - 1}

            if (packet instanceof DataPacket) {
                System.out.println("PKT RECV DATA " + packet.getPacketLength() + " " + packet.getSequenceNumber());
                if (isIn(window, packet.getSequenceNumber())){ // if its in the bufferable window
//                    System.out.println("IN BUFFER WINDOW... seq=" + packet.getSequenceNumber());
                    windowBuffer.put(packet.getSequenceNumber(), (DataPacket)packet); // put packet into buffer indexed by its seq number
                    sendAck(packet.getSequenceNumber());

                    if (getPos(window, packet.getSequenceNumber()) == 0){ // packet received was at the base of window
                        for (int i=0; i<WINDOW_SIZE; i++) {
                            // loops through the window, sliding it & sending ack'ed packets when we can
                            if (windowBuffer.containsKey(window[i])) {
                                DataPacket tmp = windowBuffer.get(window[i]);
                                fos.write(tmp.getData()); // deliver data
//                                System.out.println("DELIVER... seq=" + window[i]);
                                base++; // slide the window
                                windowBuffer.remove(window[i]); // remove from buffer
                            } else {
                                break;
                            }
                        }
                    }
                } else if (isIn(recvWindow ,packet.getSequenceNumber())){ // for resending acks
//                    System.out.println("IN RECEIVER WINDOW... seq=" + packet.getSequenceNumber());
                    sendAck(packet.getSequenceNumber());
                } else {
//                    System.out.println("IGNORED... seq=" + packet.getSequenceNumber());
                }


            } else { // EOT packet received
                System.out.println("PKT RECV EOT " + packet.getPacketLength() + " " + packet.getSequenceNumber());

                // send back EOT to the sender
                Packet eotPacket = createEOTPacket(base%256);
                DatagramPacket sendPacket =
                        new DatagramPacket(eotPacket.getBytes(), eotPacket.getPacketLength(), senderIPAddress, senderPort);
                senderSocket.send(sendPacket);
                senderSocket.close();
                System.out.println("PKT SEND EOT " + eotPacket.getPacketLength() + " " + eotPacket.getSequenceNumber());

                fos.close();
//                System.out.println("finished");
                break;
            }
        }


    }
}
