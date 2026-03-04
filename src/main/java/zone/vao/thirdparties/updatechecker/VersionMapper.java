package zone.vao.thirdparties.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;

interface VersionMapper {
    ThrowingFunction<BufferedReader,String,IOException> TRIM_FIRST_LINE = reader -> reader.readLine().trim();

    ThrowingFunction<BufferedReader,String,IOException> SPIGET = reader -> new Gson().fromJson(reader, JsonObject.class).get("name").getAsString();

    ThrowingFunction<BufferedReader,String,IOException> GITHUB_RELEASE_TAG = reader -> {
        JsonArray array = new Gson().fromJson(reader, JsonArray.class);
        if(array.size()==0) {
            throw new IOException("Could not check for updates: no GitHub release found.");
        }
        JsonObject release = array.get(0).getAsJsonObject();
        return release.get("tag_name").getAsString();
    };
    ThrowingFunction<BufferedReader, String, IOException> SPIGOT = reader -> new Gson().fromJson(reader, JsonObject.class).get("current_version").getAsString();
}
