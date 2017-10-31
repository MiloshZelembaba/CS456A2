import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class GoBackNReceiver {
    final static int MAX_PORT = 65535;
    private DatagramSocket senderSocket;
    private String filePath;
    private int senderPort;
    private InetAddress senderIPAddress;

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

    private EndOfTransferPacket createEOTPacket(){
        EndOfTransferPacket packet = new EndOfTransferPacket();

        return packet;
    }

    public static DatagramSocket createUDPSocket(){
        int n_port;
        DatagramSocket serverSocket;
        // keep trying random ports until we find an open one to establish a UDP socket on
        while (true) {
            n_port = generatePortNumber();
            serverSocket = tryUDPOnPort(n_port);

            if (serverSocket != null){
                break;
            }
        }

        return serverSocket;
    }

    public void sendAck(int ackNum){
        Packet ackPack = new AcknowledgementPacket(ackNum);
        int packetLength = ackPack.getPacketLength();
        byte[] bytes = ackPack.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, senderIPAddress, senderPort);
        try {
            senderSocket.send(sendPacket);
        } catch (Exception e){}
    }

    public static DatagramSocket tryUDPOnPort(int port){
        DatagramSocket ds;
        try {
            ds = new DatagramSocket(port, InetAddress.getLocalHost());
            return ds;
        } catch (IOException e) {}


        return null;
    }

    public static int generatePortNumber(){
        Random rand = new Random();
        return rand.nextInt(MAX_PORT - 1023) + 1024;
    }
}
