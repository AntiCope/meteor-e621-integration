package anticope.esixtwoone.sources;

import java.net.URLEncoder;
import java.util.Random;

import anticope.esixtwoone.TemplateAddon;

public abstract class Source {
    public enum Size {
        preview,
        sample,
        file
    }

    public enum SourceType {
        e621,
        gelbooru,
        rule34
    }

    protected final Random random = new Random();

    public abstract void reset();

    protected abstract String randomImage(String filter, Size size);

    public String getRandomImage(String filter, Size size) {
        try {
            return randomImage(URLEncoder.encode(filter, "UTF-8"), size);
        } catch (Exception ex) {
            TemplateAddon.LOG.error("Failed to fetch an image.", ex);
        }
        return null;
    }

    public static Source getSource(SourceType type) {
        return switch (type) {
            case e621 -> new ESixTwoOne();
            case gelbooru -> new GelBooru("https://gelbooru.com/", 700);
            case rule34 -> new GelBooru("https://api.rule34.xxx/", 700);
            default -> null;
        };
    }
}
