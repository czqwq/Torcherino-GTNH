package com.sci.torcherino.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by sci4me on 11/30/14.
 */
public final class UpdateData {
    public final ModVersion latest;
    public final String description;

    public UpdateData(ModVersion latest, String description) {
        this.latest = latest;
        this.description = description;
    }

    public static UpdateData parse(final String json) {
        final JsonParser parser = new JsonParser();
        final JsonObject root = parser.parse(json).getAsJsonObject();
        return new UpdateData(ModVersion.parse(root.get("version").getAsString()), root.get("description").getAsString());
    }
}