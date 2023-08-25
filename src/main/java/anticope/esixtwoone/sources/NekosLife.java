package anticope.esixtwoone.sources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import meteordevelopment.meteorclient.utils.network.Http;

public class NekosLife extends Source {

    private final String domain;

    public NekosLife(String domain) {
        this.domain = domain;
    }

    @Override
    public void reset() {}

    @Override
    public String randomImage(String filter, Size size) {
        String query = String.format("%s/api/v2/img/%s", domain, filter);
        JsonElement result = Http.get(query).sendJson(JsonElement.class);
        if (result == null) return null;

        if (result instanceof JsonObject object) {
            if (object.get("url") != null) {
                return object.get("url").getAsString();
            }
        }

        return null;
    }
}
