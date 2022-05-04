package anticope.esixtwoone.sources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import meteordevelopment.meteorclient.utils.network.Http;

public class ESixTwoOne extends Source {

    private int maxPage = 30;

    @Override
    public void reset() {
        maxPage = 30;
    }

    @Override
    public String randomImage(String filter, Size size) {
        int pageNum = random.nextInt(1, maxPage);
        JsonObject result = Http.get("https://e621.net/posts.json?limit=320&tags="+filter+"&page="+ pageNum).sendJson(JsonObject.class);
        if (result.get("posts") instanceof JsonArray array) {
            if(array.size() <= 0) {
                maxPage = pageNum - 1;
                return null;
            }
            if (array.get(random.nextInt(array.size())) instanceof JsonObject post) {
                var url = post.get(size.toString()).getAsJsonObject().get("url").getAsString();
                return url;
            }
        }
        return null;
    }

}
