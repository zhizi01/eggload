package org.zhizi.eggload.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerCuleManager {
    private static final Logger LOGGER = LogManager.getLogger(RuleManager.class);
    private static final Path FILE_PATH = Paths.get("config", "eggload_clue.json");

    public boolean ClueModelWrite(String playerUUID, String actionType, String message) {
        try {
            JsonObject rootObject;
            if (Files.exists(FILE_PATH)) {
                rootObject = JsonParser.parseReader(new FileReader(FILE_PATH.toFile())).getAsJsonObject();
            } else {
                rootObject = new JsonObject(); // 如果文件不存在，初始化一个新的JSON对象
            }

            JsonObject playerActions;
            if (rootObject.has(playerUUID)) {
                // 如果已经存在该UUID的数据，获取现有数据
                playerActions = rootObject.getAsJsonObject(playerUUID);
            } else {
                // 如果不存在该UUID的数据，创建新的JSONObject
                playerActions = new JsonObject();
            }

            // 根据actionType更新对应字段
            if (actionType.equals("join")) {
                playerActions.addProperty("join", message);
            } else if (actionType.equals("quit")) {
                playerActions.addProperty("quit", message);
            }

            // 将更新后的玩家数据对象放入以UUID为键的JSON对象中
            rootObject.add(playerUUID, playerActions);

            // 写入文件
            try (FileWriter writer = new FileWriter(FILE_PATH.toFile())) {
                writer.write(rootObject.toString());
                LOGGER.info("Successfully updated {} for UUID: {}", actionType, playerUUID);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to update {} for UUID: {}", actionType, playerUUID, e);
            return false;
        }
    }

    public String ClueModelRead(String playerUUID, String action) {
        try {
            File file = FILE_PATH.toFile();
            if (file.exists()) {
                JsonObject rootObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                JsonObject playerData = rootObject.getAsJsonObject(playerUUID);
                if (playerData != null && playerData.has(action)) {
                    return playerData.get(action).getAsString();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read data for UUID: {}", playerUUID, e);
        }
        return "";
    }

}
