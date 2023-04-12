import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;

public class Dictionary {

    private String dictionaryFilePath;
    private IOHandler ioHandler;
    private TreeMap<String, ArrayList<String>> dictionary;

    public Dictionary(String dictionaryFilePath) {
        this.dictionaryFilePath = dictionaryFilePath;
        this.ioHandler = new IOHandler();
        this.dictionary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // Ignore capitalization of words(key)
    }

    public synchronized void parseDictionary() throws IOHandlerException {
        JSONObject jsonObject = null;
        try {
            jsonObject = ioHandler.readJSONFile(dictionaryFilePath);
            if (jsonObject != null) {
                // If dictionary JSON file is not empty, load the data into a TreeMap
                for (Object key: jsonObject.keySet()) {
                    String word = (String) key;
                    JSONArray definition = (JSONArray) jsonObject.get(word);
                    dictionary.put(word, ioHandler.convertJSONArray(definition)); // Add current word and its definition
                }
            }
        } catch (IOHandlerException e) {
            System.out.println(e.getMessage());
            throw new IOHandlerException();
        }
    }

    // Updates the dictionary JSON file with the new changes
    public synchronized void syncDictionary() throws IOHandlerException {
        JSONObject jsonObject = new JSONObject(dictionary);
        try {
            ioHandler.writeJSONFile(dictionaryFilePath, jsonObject);
        } catch (IOHandlerException e) {
            System.out.println(e.getMessage());
            throw new IOHandlerException();
        }
    }

    public synchronized ArrayList<String> query(String word) throws InvalidRequestException, IOHandlerException {
        parseDictionary();
        if (wordExists(word)) {
            return dictionary.get(word);
        } else {
            throw new InvalidRequestException();
        }
    }

    public synchronized void add(String word, String[] definitions) throws InvalidRequestException, IOHandlerException {
        parseDictionary();
        if (!wordExists(word)) {
            dictionary.put(word, new ArrayList<>(Arrays.asList(definitions)));
            syncDictionary();
        } else {
            throw new InvalidRequestException();
        }
    }

    public synchronized void remove(String word) throws InvalidRequestException, IOHandlerException {
        parseDictionary();
        if (wordExists(word)) {
            dictionary.remove(word);
            syncDictionary();
        } else {
            throw new InvalidRequestException();
        }
    }

    public synchronized void update(String word, String[] definitions) throws InvalidRequestException, IOHandlerException {
        parseDictionary();
        if (wordExists(word)) {
            dictionary.put(word,new ArrayList<>(Arrays.asList(definitions)));
            syncDictionary();
        } else {
            throw new InvalidRequestException();
        }
    }

    private boolean wordExists(String word) {
        return dictionary.containsKey(word);
    }
}
