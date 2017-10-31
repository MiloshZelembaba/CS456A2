import java.net.*;
import java.nio.ByteBuffer;

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

        for (int i=0; i<data.length; i+=500){
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(500, data.length - i));
            for (int j=i; j<Math.min(500, data.length - i); j++){
                System.out.println(j);
                buffer.put(data[j-i]);
            }
            Packet packet = createDataPacket(buffer.array());
            int packetLength = packet.getPacketLength();
            byte[] bytes = packet.getBytes();
            String text = new String(buffer.array(), "UTF-8");
            char[] chars = text.toCharArray();
            System.out.println(chars);

            DatagramPacket sendPacket = new DatagramPacket(bytes, packetLength, IPAddress, port);
            clientSocket.send(sendPacket);

            buffer.clear();
        }

        Packet packet = createEOTPacket();
        DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getPacketLength(), IPAddress, port);
        clientSocket.send(sendPacket);

        clientSocket.close();
    }

    private DataPacket createDataPacket(byte[] curData) throws Exception{
        DataPacket packet = new DataPacket(100);
        packet.setData(curData);

        return packet;
    }

    private EndOfTransferPacket createEOTPacket(){
        EndOfTransferPacket packet = new EndOfTransferPacket();

        return packet;
    }
}
