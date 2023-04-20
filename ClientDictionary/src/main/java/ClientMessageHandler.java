import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientMessageHandler {

    private Socket socket;
    private String hostAddress;
    private int portNo;
    private ArrayList<String> response; // From server

    // Request Status
    public final String REQUEST_SUCCESS = "Success";
    public final String REQUEST_FAILURE = "Failure";
    private final int REQUEST_STATUS_INDEX = 0;

    // Action Codes
    public final static int QUERY_CODE = 1;
    public final static int ADD_CODE = 2;
    public final static int REMOVE_CODE = 3;
    public final static int UPDATE_CODE = 4;

    // Error Messages
    private final String CONNECT_FAIL = "Failed to connect to server.";
    private final String ERROR_CLOSE_SOCKET = "Unable to close the server socket.";
    private final String ERROR_STREAM = "Unable to maintain input/output streams from/to client socket.";

    private final int STARTING_DEFINITION_COUNTER = 1;
    private final String CONTAINS_DIGITS_REGEX = ".*\\d+.*"; // Check for digits
    private final String VALID_BODY_REGEX = "^[a-zA-Z](?:['\\\\\\-a-zA-Z]*[a-zA-Z])?$"; // Alphabet, Apostrophes, Hyphen
    public final static String STRING_SEPARATOR = ":";

    public ClientMessageHandler(String hostAddress, int portNo) {
        this.hostAddress = hostAddress;
        this.portNo = portNo;
    }

    public void request(int actionCode, String inputText) throws InvalidSocketException {
        try {
            connect();
            interact(actionCode,inputText);
        } catch (InvalidSocketException e) {
            throw new InvalidSocketException(e.getMessage());
        } catch (IOException e) {
            System.out.println(ERROR_STREAM);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(ERROR_CLOSE_SOCKET);
                }
            }
        }
    }

    private void connect() throws InvalidSocketException {
        socket = null;
        try {
            socket = new Socket(hostAddress, portNo);
        } catch (Exception e) {
            throw new InvalidSocketException(CONNECT_FAIL);
        }
    }

    private void interact(int actionCode, String inputText) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
        // Request format -> actionCode:word:(definition)
        out.write(String.format("%d:%s\n", actionCode, inputText));
        out.flush();
        String response = in.readLine();
        process(response);
    }

    private void process(String response) {
        // Response format -> requestStatus:definition(if applicable)
        String[] processedString = response.split(STRING_SEPARATOR);
        this.response = new ArrayList<>(Arrays.asList(processedString));
    }

    // Retrieves a formatted list of definitions suitable for display on the GUI
    public String getDefinition() {
        ArrayList<String> definitions = response;
        StringBuilder formattedDefinitions = new StringBuilder();
        int definitionCounter =  STARTING_DEFINITION_COUNTER;
        definitions.remove(REQUEST_STATUS_INDEX);
        for (String definition: definitions) {
            formattedDefinitions.append(String.format("%d: %s\n", definitionCounter, definition));
            definitionCounter++;
        }
        return formattedDefinitions.toString();
    }

    public boolean isValidWord(String word) {
        // Word does not have digits and may contain hyphens, alphabets, apostrophes
        return (word.matches(VALID_BODY_REGEX) && !word.matches(CONTAINS_DIGITS_REGEX) && word.trim().length() != 0 );
    }

    public boolean isEmptyInput(String input) {
        return input.trim().length() == 0 || input == null;
    }

    public String getRequestStatus() {return response.get(REQUEST_STATUS_INDEX);}

    public String getHostAddress() {return hostAddress;}

    public int getPortNo() {return portNo;}

    public void setHostAddress(String hostAddress) {this.hostAddress = hostAddress;}

    public void setPortNo(int portNo) {this.portNo = portNo;}
}
