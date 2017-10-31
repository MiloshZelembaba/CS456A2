import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class Sender {
    private static final int GO_BACK_N = 0;
    private static final int SELECTIVE_REPEAT = 1;

    public static void main(String[] args) throws Exception{
        int protocol = Integer.parseInt(args[0]);
        int millisecondTimeout = Integer.parseInt(args[1]);
        String filePath = args[2];

        // read in the channel info
        Path channelInfoPath = Paths.get("channelInfo");
        String channelInfo = Files.readAllLines(channelInfoPath).get(0);
        String serverAddress = channelInfo.split(" ")[0];
        int port = Integer.parseInt(channelInfo.split(" ")[1]);

        if (protocol == GO_BACK_N){
            Path path = Paths.get(filePath);
            byte[] dataToSend = Files.readAllBytes(path);
            System.out.println("data is this many bytes: " + dataToSend.length);

            GoBackNSender sender = new GoBackNSender(millisecondTimeout, dataToSend, serverAddress, port);
            sender.sendData();
        } else if (protocol == SELECTIVE_REPEAT){

        } else {
            throw new Exception("Incorrect protocol selected");
        }
    }
}
