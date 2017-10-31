import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Created by miloshzelembaba on 2017-10-31.
 */
abstract public class AbstractReceiver {
    protected final int WINDOW_SIZE = 10;
    final static int MAX_PORT = 65535;
    protected DatagramSocket senderSocket;
    protected String filePath;
    protected int senderPort;
    protected InetAddress senderIPAddress;

    // implemented by the respective protocol
    abstract public void receive() throws Exception;

    /**
     * Creates an EOTPacket
     */
    protected EndOfTransferPacket createEOTPacket(int seqn){
        EndOfTransferPacket packet = new EndOfTransferPacket(seqn);

        return packet;
    }

    /**
     * Opens up a UDPSocket
     */
    protected static DatagramSocket createUDPSocket(){
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

    /**
     * Sends an AckPacket with ackNum to the sender
     */
    protected void sendAck(int ackNum){
        Packet ackPack = new AcknowledgementPacket(ackNum);
        int packetLength = ackPack.getPacketLength();
        byte[] bytes = ackPack.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, senderIPAddress, senderPort);
        try {
            senderSocket.send(sendPacket);
            System.out.println("PKT SEND ACK " + packetLength + " " + ackNum);
        } catch (Exception e){}
    }

    /**
     * Tries opening a UDPSocket on port
     */
    protected static DatagramSocket tryUDPOnPort(int port){
        DatagramSocket ds;
        try {
            ds = new DatagramSocket(port, InetAddress.getLocalHost());
            return ds;
        } catch (IOException e) {}


        return null;
    }

    /**
     * Generates a random port number
     */
    protected static int generatePortNumber(){
        Random rand = new Random();
        return rand.nextInt(MAX_PORT - 1023) + 1024;
    }

    /**
     * Generates a window
     */
    protected int[] generateWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base + i)%256;
        }

        return result;
    }

    /**
     * Generates a reciever window
     */
    protected int[] generateRecvWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base - WINDOW_SIZE + i)%256;
        }

        return result;
    }

    /**
     * returns the position of num in windowSlider
     */
    protected int getPos(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return i;
            }
        }

        return -1;
    }

    /**
     * returns true if num in windowSlider
     */
    protected boolean isIn(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return true;
            }
        }

        return false;
    }

}
