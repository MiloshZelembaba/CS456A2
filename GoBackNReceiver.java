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
    private DatagramSocket serverSocket;
    private String filePath;
    private int senderPort;
    private InetAddress senderIPAddress;

    public GoBackNReceiver(String filePath) throws Exception{
        serverSocket = createUDPSocket(); // creates a UDP socket
        this.filePath = filePath;

        // write recvInfo for the channel
        FileWriter fileWriter = new FileWriter("recvInfo");
        String serverAddress = serverSocket.getLocalAddress().toString();
        fileWriter.write(serverAddress.replace("/","") + " " +serverSocket.getLocalPort());
        fileWriter.close();
    }

    public void receive() throws Exception{
        byte[] receiveData = new byte[512];
        int expectedSequenceNumber = -1;

        DatagramPacket receivePacket;
        FileOutputStream fos = new FileOutputStream(filePath,true);
        while (true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            Packet packet = Packet.toPacket(receivePacket); // converts it to one of my packets i've defined
            senderPort = receivePacket.getPort();
            senderIPAddress = receivePacket.getAddress();


            if (packet instanceof DataPacket) {
                if (packet.getSequenceNumber() == expectedSequenceNumber+1) {
                    System.out.println("EXPECTED... seq=" + expectedSequenceNumber+1);
                    // write recvInfo for the channel
                    fos.write(((DataPacket) packet).getData());
                    sendAck(expectedSequenceNumber);
                    expectedSequenceNumber = (expectedSequenceNumber + 1)%256;
                    System.out.println("sending... seq=" + expectedSequenceNumber);
                } else {
                    System.out.println("UNEXPECTED... seq=" + packet.getSequenceNumber());
                    sendAck(expectedSequenceNumber);
                    System.out.println("sending... seq=" + expectedSequenceNumber);
                }
            } else {
                fos.close();
                System.out.println("finished");
                break;
            }
        }
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
            serverSocket.send(sendPacket);
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
