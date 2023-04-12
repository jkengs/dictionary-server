
public class Client {

    private int portNo;
    private String hostAddress;
    private ClientMessageHandler clientMessageHandler;
    private boolean hasValidPort;

    // Validity Constraints
    private final int PORT_NO_INDEX = 1;
    private final int ADDRESS_INDEX = 0;
    private final int PORT_NO_LOWER_LIMIT = 1024;
    private final int PORT_NO_UPPER_LIMIT = 65335;
    private final int REQUIRED_ARGS = 2;

    // Error Messages
    private final String REQUIRED_ARGS_ERROR = "Warning: Insufficient arguments entered. \n" +
            "Usage: java -jar ClientDictionary.jar <server-address> <server-port>";
    private final String INVALID_PORT_NO = "Warning: Invalid port number entered. Please input a port number between " +
            "1024 and 65335.";

    // Status Messages
    private final String APP_RUNNING = "Application is launching...";
    private final String ARG_VERIFY = "Verifying arguments...";
    private final String APP_TERMINATING = "Application is terminating...";

    public static void main(String[] args) {
        Client client = new Client();
        client.runProgram(args);
    }

    private void runProgram(String[] args) {
        hasValidPort  = false;
        scanCommandArguments(args);
        if (hasValidPort) {
            System.out.println(APP_RUNNING);
            startMessageHandler();
            initializeGUI();
        }
    }

    private void scanCommandArguments(String[] args) {
        System.out.println(ARG_VERIFY);
        if (args.length == REQUIRED_ARGS) {
            validatePort(Integer.parseInt(args[PORT_NO_INDEX]));
            hostAddress = args[ADDRESS_INDEX];
        } else {
            System.out.println(REQUIRED_ARGS_ERROR);
            exitProgram();
        }
    }

    private void startMessageHandler() {
        this.clientMessageHandler = new ClientMessageHandler(hostAddress, portNo);
    }

    private void initializeGUI() {
        ClientGUI clientGUI = new ClientGUI(this, clientMessageHandler);
        clientGUI.initialize();
    }

    private void validatePort(int portNo) {
        if (portNo >= PORT_NO_LOWER_LIMIT && portNo <= PORT_NO_UPPER_LIMIT) {
            hasValidPort = true;
            this.portNo = portNo;
        } else {
            // If provided port number is ia well-known port and thus invalid
            System.out.println(INVALID_PORT_NO);
            exitProgram();
        }
    }

    public void exitProgram() {
        System.out.println(APP_TERMINATING);
        System.exit(0);
    }
}
