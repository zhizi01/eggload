package org.zhizi.eggload.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RuleManager {
    private static final Logger LOGGER = LogManager.getLogger(RuleManager.class);
    private static final Path FILE_PATH = Paths.get("config", "eggload_rule.json");

    public boolean RuleModelWrite(String ruleName, String ruleValue) {
        try {
            JsonObject rootObject = new JsonObject();
            if (Files.exists(FILE_PATH)) {
                rootObject = JsonParser.parseReader(new FileReader(FILE_PATH.toFile())).getAsJsonObject();
            }
            rootObject.addProperty(ruleName, ruleValue);
            try (FileWriter writer = new FileWriter(FILE_PATH.toFile())) {
                writer.write(rootObject.toString());
                LOGGER.info("Successfully wrote rule: {} with value: {}", ruleName, ruleValue);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to write rule: {} with value: {}", ruleName, ruleValue, e);
            return false;
        }
    }

    public String RuleModelRead(String ruleName) {
        try {
            File file = FILE_PATH.toFile();
            if (file.exists()) {
                JsonObject rootObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                JsonElement element = rootObject.get(ruleName);
                if (element != null) {
                    return element.getAsString();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read rule: {}", ruleName, e);
        }
        return "disable"; // 默认值
    }
}
