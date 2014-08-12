package mythruna.util;

import org.progeeks.json.JsonParser;
import org.progeeks.json.JsonPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {
    private static JsonParser jsonParser = new JsonParser();
    private static JsonPrinter jsonPrinter = new JsonPrinter();

    public JsonUtils() {
    }

    public static Object loadJson(File file) throws IOException {
        if (!file.exists())
            return null;
        FileReader in = new FileReader(file);
        try {
            return jsonParser.parse(in);
        } finally {
            in.close();
        }
    }

    public static void writeJson(File file, Object o)
            throws IOException {
        FileWriter out = new FileWriter(file);
        try {
            jsonPrinter.write(o, out);
        } finally {
            out.close();
        }
    }
}