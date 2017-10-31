import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class SelectiveRepeatSender extends AbstractSender{

    public SelectiveRepeatSender(int timeout, byte[] data, String serverAddress, int port) throws Exception{
        millisecondTimeout = timeout;
        IPAddress= InetAddress.getByName(serverAddress);
        this.port = port;
        this.data = data;
        senderSocket = new DatagramSocket();
        senderSocket.setSoTimeout(1);
    }

    public void sendData() throws Exception {
        int base = 0;
        int currentSendingPos = 0;
        long startTime = 0;
        long endTime;
        DatagramPacket ackPacket;
        Map<Integer, Long> timers = new HashMap<>(); // this will keep track of all of the timers for each unacked packet in the window
        ArrayList<DataPacket> packets = new ArrayList<>(createAllPackets()); // not sure if i need to do it like this (with the copying)


        while(true){
            if (base == packets.size()){ // we reach this when all the packets have completed sending and been ack'ed
//                System.out.println("all packets have been sent, sending EOT");
                break;
            }

            if (currentSendingPos < base + WINDOW_SIZE && currentSendingPos < packets.size()){ // send packets when we can
//                System.out.println("SENT... seq=" + packets.get(currentSendingPos).getSequenceNumber());
                sendPacket(packets.get(currentSendingPos));

                timers.put(packets.get(currentSendingPos).getSequenceNumber(),
                            System.nanoTime()); // start the timer for packet

                currentSendingPos++;
            }

            byte[] receiveData = new byte[12];
            ackPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
            try {
                senderSocket.receive(ackPacket);
                Packet packet = Packet.toPacket(ackPacket); // converts it to one of my packets i've defined
                int ackNum = packet.getSequenceNumber();
//                System.out.println("JUST SAW... seq=" + ackNum);
                System.out.println("PKT RECV ACK " + packet.getPacketLength() + " " + packet.getSequenceNumber());
                int[] windowNums = generateWindow(base);
                if (isIn(windowNums, ackNum)) { // else ignore duplicate acks
                    packets.get(base + getPos(windowNums,ackNum)).ack();
                    timers.remove(ackNum); // stop timer for the packet that received

                    if (getPos(windowNums,ackNum) == 0){ // we just acked the base, move slider
                        while (true){
                            base++;
                            if (base < packets.size() && packets.get(base).wasAcked()){
                                continue;
                            }
                            break;
                        }
                    }
                }

            } catch (SocketTimeoutException e){}

            endTime = System.nanoTime();
            ArrayList<Integer> activeTimers= new ArrayList<>(timers.keySet());
            for (Integer t: activeTimers) {
                if ((endTime - timers.get(t)) / 1000000 >= millisecondTimeout) {
//                System.out.println("TIMEOUT... base="+base);
                    timers.remove(t); // stop timer
                    timers.put(t,System.nanoTime()); // start timer again

                    sendPacket(packets.get(base + getPos(generateWindow(base), t)));
                }
            }

        }

        // at this point we have sent all of the DataPackets and they have been successfully received/acknowledged
        // so we send the EOT packet that we can assume will not be dropped
        Packet packet = createEOTPacket( base%256);
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        senderSocket.send(sendPacket);
        System.out.println("PKT SEND EOT " + packet.getPacketLength() + " " + packet.getSequenceNumber());

        byte[] receiveData = new byte[12];
        DatagramPacket eotPacket = new DatagramPacket(receiveData, receiveData.length); // will timeout after 10ms
        senderSocket.setSoTimeout(0); // set unlimited timeout value, since we're garunteed for EOT to arrive
        senderSocket.receive(eotPacket);
        Packet tmp = Packet.toPacket(eotPacket);

        System.out.println("PKT RECV EOT " + tmp.getPacketLength() + " " + tmp.getSequenceNumber());


        senderSocket.close();


    }
}
