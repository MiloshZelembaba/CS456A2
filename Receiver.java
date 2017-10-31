

/**
 * Created by miloshzelembaba on 2017-10-30.
 */
public class Receiver {
    private static final int GO_BACK_N = 0;
    private static final int SELECTIVE_REPEAT = 1;

    public static void main(String[] args) throws Exception{
        int protocol = Integer.parseInt(args[0]);
        String filePath = args[1];

        if (protocol == GO_BACK_N){
            GoBackNReceiver receiver = new GoBackNReceiver(filePath);
            receiver.receive();
        } else if (protocol == SELECTIVE_REPEAT){
            SelectiveRepeatReceiver receiver = new SelectiveRepeatReceiver(filePath);
            receiver.receive();
        } else {
            throw new Exception("Incorrect protocol selected");
        }

    }
}
