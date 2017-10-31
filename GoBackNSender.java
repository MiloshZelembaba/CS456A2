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

    public void sendData() throws Exception {
        int sequenceCounter = 0;
        int base = 0;
        int currentSendingPos = 0;
        long startTime = 0;
        long endTime;
        DatagramPacket ackPacket;
        ArrayList<DataPacket> packets = new ArrayList<>(createAllPackets()); // not sure if i need to do it like this (with the copying)


        while(true){
            if (base == packets.size()){
                System.out.println("all packets have been sent, sending EOT");
                break;
            }

            if (currentSendingPos < base + WINDOW_SIZE && currentSendingPos < packets.size()){
                System.out.println("SENT... seq=" + packets.get(currentSendingPos).getSequenceNumber());
                sendPacket(packets.get(currentSendingPos));

                if (base == currentSendingPos){
                    System.out.println("started timer on base="+base);
                    startTime = System.nanoTime();
                }

                currentSendingPos++;
            }

            byte[] receiveData = new byte[12];
            ackPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
            try {
                senderSocket.receive(ackPacket);
                Packet packet = Packet.toPacket(ackPacket); // converts it to one of my packets i've defined
                int ackNum = packet.getSequenceNumber();
                System.out.println("JUST SAW... seq=" + ackNum);
                // TODO: i think the below works, need to check
                int[] windowNums = generateWindow(base);
                if (isIn(windowNums, ackNum)) { // else ignore duplicate acks
                    base += getPos(windowNums,ackNum) + 1;
//                    if (ackNum >= base % 256) {
//                        base += ((ackNum - base % 256) + 1);
//                    } else {
//                        base += ((256 - (base % 256 - ackNum)) + 1);
//                    }
                    System.out.println("started timer on(tryblock) base=" + base);
                    startTime = System.nanoTime();
                }

            } catch (SocketTimeoutException e){}

            endTime = System.nanoTime();
            if ((endTime - startTime)/1000000 >= millisecondTimeout){
                System.out.println("TIMEOUT... base="+base);
                startTime = System.nanoTime();
                for (int i=base; i<currentSendingPos; i++){
                    sendPacket(packets.get(i));
                }
            }

        }

        // do this at the end ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Packet packet = createEOTPacket();
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        senderSocket.send(sendPacket);

        byte[] receiveData = new byte[12];
        DatagramPacket eotPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
        senderSocket.setSoTimeout(0); // set unlimited timeout value, since we're garunteed for EOT to arrive
        senderSocket.receive(eotPacket);


        senderSocket.close();
    }
}
