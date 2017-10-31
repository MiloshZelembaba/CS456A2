import java.net.*;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class GoBackNSender {
    private int millisecondTimeout;
    private int port;
    private InetAddress IPAddress;
    private byte[] data;


    public GoBackNSender(int timeout, byte[] data, String serverAddress, int port) throws UnknownHostException{
        millisecondTimeout = timeout;
        IPAddress= InetAddress.getByName(serverAddress);
        this.port = port;
        this.data = data;
    }




    public void sendData() throws Exception{
        DatagramSocket clientSocket = new DatagramSocket();

        byte[] sendData;


        Packet packet = createPacket(data);
        int packetLength = packet.getPacketLength();
        byte[] bytes = packet.getBytes();
        System.out.println("PacketLength=" + packetLength + "   actualLength=" + bytes.length);

        DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, IPAddress, port);
        clientSocket.send(sendPacket);

    }

    public DataPacket createPacket(byte[] curData) throws Exception{
        DataPacket packet = new DataPacket(100);
        packet.setData(curData);

        return packet;
    }
}
