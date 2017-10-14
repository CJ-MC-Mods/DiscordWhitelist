package com.cjm721.discordwhitelist;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class RoleRepository {
    BiMap<String,String> mcToDiscord;

    public RoleRepository() {
        this.mcToDiscord = HashBiMap.create();
    }

    public static RoleRepository create(File file) throws IOException {
        Gson gson = gson();

        if (!file.exists()) {
            String json = gson.toJson(new RoleRepository(), RoleRepository.class);
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.flush();
            writer.close();
        }

        try {
            return gson.fromJson(new FileReader(file), RoleRepository.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Impossible Exception, repositoryFile existed moments ago", e);
        }
    }

    private static Gson gson() {
        return new GsonBuilder().registerTypeAdapter(BiMap.class, new JsonDeserializer<BiMap>() {
            @Override
            public BiMap deserialize(JsonElement json,
                                     Type type,
                                     JsonDeserializationContext context)
                    throws JsonParseException {
                BiMap mapping = HashBiMap.create();
                JsonObject object = (JsonObject) json;
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String value = entry.getValue().getAsString();
                    mapping.put(entry.getKey(),value);
                }
                return mapping;
            }
        }).setPrettyPrinting().create();
    }

    @Nullable
    public UUID addEntry(String discordID,@Nonnull UUID minecraftId) {
        String oldEntry = mcToDiscord.inverse().put(discordID,minecraftId.toString());

        Gson gson = gson();
        String json = gson.toJson(this, this.getClass());
        try (FileWriter writer = new FileWriter(DiscordWhitelist.repositoryFile)){
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return oldEntry == null ? null : UUID.fromString(oldEntry);
    }

    public String getDiscordId(@Nonnull UUID minecraftId) {
        return mcToDiscord.get(minecraftId.toString());
    }
}
