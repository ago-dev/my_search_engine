package utils;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Class responsible for file read/write
 */
public final class FileHandler {
    private FileHandler() {}

    public static void createAndWriteDocument (JSONObject jsonObject) throws IOException {
        try (FileWriter fileWriter = new FileWriter("./documents/" + UUID.randomUUID() + ".json")) {
            fileWriter.write(jsonObject.toJSONString());
        }
    }

    public static JSONObject createJsonObjectForDocument (int id, List<String > tokens){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tokens", tokens);
        jsonObject.put("id", id);
        return jsonObject;
    }
}
