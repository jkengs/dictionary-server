import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.util.ArrayList;

public class IOHandler {

    // Error Messages
    private final String ERROR_EMPTY = " File contents may be empty.";
    private final String ERROR_READ_JSON = "Unable to read any data from JSON file: ";
    private final String ERROR_WRITE_JSON = "Unable to write to the JSON file: ";
    private final String ERROR_FILE_CREATE = "Unable to create a new JSON File: ";
    private final String JSON_BRACKETS = "{}";

    public IOHandler(){}

    public JSONObject readJSONFile(String filePath) throws IOHandlerException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            FileReader reader = new FileReader(filePath);
            Object object = parser.parse(reader);
            jsonObject = (JSONObject) object;
        } catch (Exception e) {
            throw new IOHandlerException(ERROR_READ_JSON + filePath + ERROR_EMPTY);
        }
        return jsonObject;
    }

    public void writeJSONFile(String filePath, JSONObject jsonObject) throws IOHandlerException {
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(jsonObject.toJSONString());
            file.flush();
            file.close();
        } catch (Exception e) {
            throw new IOHandlerException(ERROR_WRITE_JSON + filePath);
        }
    }

    public ArrayList<String> convertJSONArray(JSONArray jsonArray) {
        ArrayList<String> convertedArrayList = new ArrayList<>();
        for (Object value : jsonArray) {
            convertedArrayList.add((String) value);
        }
        return convertedArrayList;
    }

    public void createFile(String filePath) throws IOHandlerException {
        File file = new File(filePath);
        try {
            file.createNewFile();
            // Add JSON Brackets to the newly created JSON file
            FileWriter fr = new FileWriter(file, false);
            PrintWriter pw = new PrintWriter(fr);
            pw.write(JSON_BRACKETS);
            pw.flush();
            pw.close();
            fr.close();
        } catch (Exception e) {
            throw new IOHandlerException(ERROR_FILE_CREATE + filePath);
        }
    }

    public boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
}
