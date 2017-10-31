import java.net.*;
import java.util.ArrayList;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class GoBackNSender extends AbstractSender{

    public GoBackNSender(int timeout, byte[] data, String serverAddress, int port) throws Exception{
        millisecondTimeout = timeout;
        IPAddress= InetAddress.getByName(serverAddress);
        this.port = port;
        this.data = data;
        senderSocket = new DatagramSocket();
        senderSocket.setSoTimeout(1);
    }

    /**
     * This is where the logic behind GBN is coded.
     * Sends data to receiver via the GBN protocol.
     */
    public void sendData() throws Exception {
        int base = 0;
        int currentSendingPos = 0; // position of DataPacket we currently want to send
        long startTime = 0;
        long endTime;
        DatagramPacket ackPacket;
        ArrayList<DataPacket> packets = new ArrayList<>(createAllPackets()); // not sure if i need to do it like this (with the copying)


        while(true){
            if (base == packets.size()){ // we reach this when all the packets have completed sending and been ack'ed
//                System.out.println("all packets have been sent, sending EOT");
                break;
            }

            if (currentSendingPos < base + WINDOW_SIZE && currentSendingPos < packets.size()){ // send a packet if we can
//                System.out.println("SENT... seq=" + packets.get(currentSendingPos).getSequenceNumber());
                sendPacket(packets.get(currentSendingPos));

                if (base == currentSendingPos){
//                    System.out.println("started timer on base="+base);
                    startTime = System.nanoTime();
                }

                currentSendingPos++;
            }

            byte[] receiveData = new byte[12];
            ackPacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                senderSocket.receive(ackPacket); // will timeout after 1ms
                Packet packet = Packet.toPacket(ackPacket); // converts it to one of my packets i've defined
                int ackNum = packet.getSequenceNumber();
                System.out.println("PKT RECV ACK " + packet.getPacketLength() + " " + packet.getSequenceNumber());
//                System.out.println("JUST SAW... seq=" + ackNum);
                int[] windowNums = generateWindow(base);
                if (isIn(windowNums, ackNum)) { // else ignore duplicate acks
                    base += getPos(windowNums,ackNum) + 1; // shift the base
//                    System.out.println("started timer on(tryblock) base=" + base);
                    startTime = System.nanoTime();
                }

            } catch (SocketTimeoutException e){
                // It's okay the receive times out, just ignore it
            }

            endTime = System.nanoTime();
            if ((endTime - startTime)/1000000 >= millisecondTimeout){ // check if the timer has timedout
//                System.out.println("TIMEOUT... base="+base);
                startTime = System.nanoTime();
                for (int i=base; i<currentSendingPos; i++){ // resend packets due to timeout
                    sendPacket(packets.get(i));
                }
            }

        }

        // at this point we have sent all of the DataPackets and they have been successfully received/acknowledged
        // so we send the EOT packet that we can assume will not be dropped
        Packet packet = createEOTPacket(base%256);
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        senderSocket.send(sendPacket);
        System.out.println("PKT SEND EOT " + packet.getPacketLength() + " " + packet.getSequenceNumber());

        // receive the EOT from the receiver
        byte[] receiveData = new byte[12];
        DatagramPacket eotPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
        senderSocket.setSoTimeout(0); // set unlimited timeout value, since we're garunteed for EOT to arrive
        senderSocket.receive(eotPacket);
        packet = Packet.toPacket(eotPacket);
        System.out.println("PKT RECV EOT " + packet.getPacketLength() + " " + packet.getSequenceNumber());

        // terminate
        senderSocket.close();
    }
}
