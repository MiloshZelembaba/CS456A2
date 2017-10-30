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

    public GoBackNReceiver() throws Exception{
        serverSocket = createUDPSocket(); // creates a UDP socket

        // write recvInfo for the channel
        FileWriter fileWriter = new FileWriter("recvInfo");
        String serverAddress = serverSocket.getLocalAddress().toString();
        fileWriter.write(serverAddress.replace("/","") + " " +serverSocket.getLocalPort());
        fileWriter.close();
    }

    public void receive() throws Exception{
        byte[] receiveData = new byte[512];
        DatagramPacket receivePacket;
        while (true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            System.out.println(receiveData);
            break;
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
