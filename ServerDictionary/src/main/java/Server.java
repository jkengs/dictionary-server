import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    // Server Information
    private int portNo;
    private ServerSocket serverSocket;
    private Dictionary dictionary;
    private String dictionaryFilePath;

    // Server Status
    private boolean hasValidPort;
    private boolean isRunning;
    private int requestNo;

    // Argument Indexes
    private final int PORT_NO_INDEX = 0;
    private final int DICT_INDEX = 1;

    // Validity Constraints
    private final int PORT_NO_LOWER_LIMIT = 1024;
    private final int PORT_NO_UPPER_LIMIT = 65335;
    private final int REQUIRED_ARGS = 2;

    // Default Filepath
    private final String DEFAULT_DICTIONARY_FILE_PATH = "dictionary.json";

    // Error Messages
    private final String INVALID_PORT_NO = "Invalid port number entered. " +
            "Please input a port number between 1024 and 65335.";
    private final String INVALID_ARG_NO = "Insufficient arguments entered.\n" +
            "Usage: java -jar ServerDictionary.jar <port> <dictionary-file-path>";
    private final String ERROR_BIND = "Server port number is in use, please try another.";
    private final String ERROR_CREATE_SOCKET = "Unable to create a server socket.";
    private final String ERROR_CLOSE_SOCKET = "Unable to close the server socket.";
    private final String ERROR_LOCATE_FILE = "Unable to locate the file: ";

    // Status Messages
    private final String PROGRAM_RUNNING = "Server application is running...";
    private final String ARG_VERIFY = "Verifying arguments...";
    private final String SERVER_TERMINATING = "Server application is terminating...";
    private final String SOCKET_CLOSING = "Server socket is closing...";
    private final String SERVER_LISTENING = "Server listening for connections on port ";
    private final String DEFAULT_FILE_CREATED = "Default file created at: ";

    public static void main(String[] args) {
        Server server = new Server();

        server.runProgram(args);
    }

    private void runProgram(String[] args) {

        isRunning = true;
        serverSocket = null;
        scanCommandArguments(args);
        if (hasValidPort) {
            System.out.println(PROGRAM_RUNNING);
            listen();
        } else {
            exitProgram();
        }
    }

    private void scanCommandArguments(String[] args) {
        System.out.println(ARG_VERIFY);
        if (args.length == REQUIRED_ARGS) {
            validatePort(Integer.parseInt(args[PORT_NO_INDEX]));
            validateDictionary(args[DICT_INDEX]);
        } else {
            System.out.println(INVALID_ARG_NO);
            exitProgram();
        }
    }

    private void validatePort(int portNo) {
        if (portNo >= PORT_NO_LOWER_LIMIT && portNo <= PORT_NO_UPPER_LIMIT) {
            hasValidPort = true;
            this.portNo = portNo;

        } else {
            // If provided port number is a well-known port and thus invalid
            System.out.println(INVALID_PORT_NO);
            exitProgram();
        }
    }

    private void validateDictionary(String dictionaryFilePath) {
        IOHandler ioHandler = new IOHandler();
        if (ioHandler.fileExists(dictionaryFilePath)) {
            this.dictionaryFilePath = dictionaryFilePath;
            this.dictionary = new Dictionary(dictionaryFilePath);
        } else {
            System.out.println(ERROR_LOCATE_FILE + dictionaryFilePath);
            // If provided filepath does not exist, create a default one
            try {
                ioHandler.createFile(DEFAULT_DICTIONARY_FILE_PATH);
                this.dictionary = new Dictionary(DEFAULT_DICTIONARY_FILE_PATH);
                this.dictionaryFilePath = dictionaryFilePath;
                System.out.println(DEFAULT_FILE_CREATED + DEFAULT_DICTIONARY_FILE_PATH);
            } catch (IOHandlerException e) {
                System.out.println(e.getMessage());
                exitProgram();
            }
        }
    }

    private void listen() {
        try {
            serverSocket = new ServerSocket(portNo);
            System.out.println(SERVER_LISTENING + portNo + "...");
            Thread serverController = new Thread(new ServerController(this));
            serverController.start();
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                // Creates a thread for each request received
                Thread t = new Thread((new ServerRequestHandler(clientSocket,dictionary)));
                t.start();
                requestNo++;
            }
        } catch (BindException e) {
            System.out.println(ERROR_BIND);
            exitProgram();
        } catch (IOException e) {
            System.out.println(ERROR_CREATE_SOCKET);
            exitProgram();
        }
    }

    public int getRequestNo() {
        return requestNo;
    }

    public String getDictionaryFilePath() {return dictionaryFilePath;}

    public void exitProgram() {
        isRunning = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println(SOCKET_CLOSING);
            } catch (IOException e) {
                System.out.println(ERROR_CLOSE_SOCKET);
            }
        }
        System.out.println(SERVER_TERMINATING);
        System.exit(0);
    }
}

