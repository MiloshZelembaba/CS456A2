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

    abstract public void receive() throws Exception;

    protected EndOfTransferPacket createEOTPacket(int seqn){
        EndOfTransferPacket packet = new EndOfTransferPacket(seqn);

        return packet;
    }

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

    protected static DatagramSocket tryUDPOnPort(int port){
        DatagramSocket ds;
        try {
            ds = new DatagramSocket(port, InetAddress.getLocalHost());
            return ds;
        } catch (IOException e) {}


        return null;
    }

    protected static int generatePortNumber(){
        Random rand = new Random();
        return rand.nextInt(MAX_PORT - 1023) + 1024;
    }

    protected int[] generateWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base + i)%256;
        }

        return result;
    }

    protected int[] generateRecvWindow(int base){
        int[] result = new int[10];

        for (int i=0; i<WINDOW_SIZE; i++){
            result[i] = (base - WINDOW_SIZE + i)%256;
        }

        return result;
    }

    protected int getPos(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return i;
            }
        }

        return -1;
    }

    protected boolean isIn(int[] windowSlider, int num){
        for (int i=0; i<WINDOW_SIZE; i++){
            if (windowSlider[i] == num){
                return true;
            }
        }

        return false;
    }

}
