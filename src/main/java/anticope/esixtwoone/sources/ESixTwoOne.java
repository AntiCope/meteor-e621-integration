package anticope.esixtwoone.sources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import meteordevelopment.meteorclient.utils.network.Http;

public class ESixTwoOne extends Source {

    @Override
    public String randomImage(String filter, Size size) {
        JsonObject result = Http.get("https://e621.net/posts.json?limit=10&tags="+filter+"&page="+ random.nextInt(1, 700)).sendJson(JsonObject.class);
        if (result.get("posts") instanceof JsonArray array) {
            if (array.get(random.nextInt(0, 11)) instanceof JsonObject post) {
                var url = post.get(size.toString()).getAsJsonObject().get("url").getAsString();
                return url;
            }
        }
        return null;
    }
    
}
