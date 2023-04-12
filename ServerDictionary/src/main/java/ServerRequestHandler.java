import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerRequestHandler extends Thread{

    private Socket clientSocket;
    private Dictionary dictionary;
    private ArrayList<String> response;

    // Request Status
    private final String REQUEST_SUCCESS = "Success";
    private final String REQUEST_FAILURE = "Failure";
    private final String REQUEST_ERROR = "Error";
    private final int REQUEST_STATUS_INDEX = 0;

    // Action Codes
    private final int QUERY_CODE = 1;
    private final int ADD_CODE = 2;
    private final int REMOVE_CODE = 3;
    private final int UPDATE_CODE = 4;

    // Client Request Indexes
    private final int ACTION_CODE_INDEX = 0;
    private final int WORD_INDEX = 1;
    private final int DEFINITION_INDEX = 2;

    private final String ERROR_STREAM = "Unable to maintain input/output streams from/to client socket.";
    private final String DEFINITION_SEPARATOR = "\\*"; // Handles word with multiple meanings
    private final String STRING_SEPARATOR = ":";

    public ServerRequestHandler(Socket clientSocket, Dictionary dictionary) {
        this.clientSocket = clientSocket;
        this.dictionary = dictionary;
        this.response = new ArrayList<>();
    }

    // Start of thread (per-request)
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
            String request;
            if ((request = in.readLine()) != null) {
                execute(request);
                // Response format -> requestStatus:definitions
                out.write(convertToString(response) + "\n");
                out.flush();
            }
        } catch (IOException e) {
            System.out.println(ERROR_STREAM);
        }
    }

    private void execute(String request) {
        // Request format => actionCode:word:definition
        String[] processedLine = request.split(STRING_SEPARATOR);
        int actionCode = Integer.parseInt(processedLine[ACTION_CODE_INDEX]);
        String word = processedLine[WORD_INDEX];
        try {
            switch (actionCode) {
                case QUERY_CODE:
                    response = dictionary.query(word); // Retrieves the list of definition if word is present
                    break;

                case ADD_CODE:
                    dictionary.add(word, processDefinitions(processedLine[DEFINITION_INDEX]));
                    break;

                case REMOVE_CODE:
                    dictionary.remove(word);
                    break;

                case UPDATE_CODE:
                    dictionary.update(word, processDefinitions(processedLine[DEFINITION_INDEX]));
                    break;
            }

            response.add(REQUEST_STATUS_INDEX,REQUEST_SUCCESS);
        } catch (InvalidRequestException e) {
            response.add(REQUEST_STATUS_INDEX,REQUEST_FAILURE);
            } catch (IOHandlerException e) {
            response.add(REQUEST_STATUS_INDEX,REQUEST_ERROR);
        }
    }

    // Process a word with more than one definition
    private String[] processDefinitions(String definitions) {
        return definitions.split(DEFINITION_SEPARATOR);
    }

    private String convertToString(ArrayList<String> arrayList) {
        return String.join(STRING_SEPARATOR, arrayList);
    }
}
