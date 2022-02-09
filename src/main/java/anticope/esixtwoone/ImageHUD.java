package anticope.esixtwoone;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.renderer.Texture;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.awt.image.BufferedImage;

import static meteordevelopment.meteorclient.utils.Utils.WHITE;

import java.util.Random;

import javax.imageio.ImageIO;

public class ImageHUD extends HudElement {
    private Texture texture;
    private boolean locked = false;
    private int ticks = 0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the image.")
        .defaultValue(3)
        .min(0.1)
        .sliderRange(0.1, 10)
        .build()
    );
    

    private final Setting<String> tags = sgGeneral.add(new StringSetting.Builder()
        .name("tags")
        .description("Tags")
        .defaultValue("femboy")
        .onChanged((v) -> texture = null)
        .build()
    );

    private final Setting<Integer> refreshRate = sgGeneral.add(new IntSetting.Builder()
        .name("refresh-rate")
        .description("How often to change (ticks).")
        .defaultValue(1200)
        .min(200)
        .build()
    );

    public ImageHUD(HUD hud) {
        super(hud, "e621-image", "sex");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!active) return;
        ticks ++;
        if (ticks >= refreshRate.get()) {
            ticks = 0;
            loadImage();
        }
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(64 * scale.get(), 64 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (texture == null) {
            loadImage();
            return;
        }

        texture.bind();
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(box.getX(), box.getY(), box.width, box.height, WHITE);
        Renderer2D.TEXTURE.render(null);
    }

    private void loadImage() {
        if (locked)
            return;
        new Thread(() -> {
            try {
                locked = true;
                var random = new Random();
                JsonObject result = Http.get("https://e621.net/posts.json?limit=1&tags="+tags.get().replace(" ", "+")+"&page="+ random.nextInt(1, 749)).sendJson(JsonObject.class);
                if (result.get("posts") instanceof JsonArray array) {
                    if (array.get(0) instanceof JsonObject post) {
                        var url = post.get("preview").getAsJsonObject().get("url").getAsString();
                        TemplateAddon.LOG.info(url);
                        // int width = post.get("preview").getAsJsonObject().get("width").getAsInt();
                        // int height = post.get("preview").getAsJsonObject().get("height").getAsInt();
                        int size = 120;//Math.min(width, height);
                        BufferedImage img = ImageIO.read(Http.get(url).sendInputStream());
                        byte[] data = new byte[size*size*3];
                        int[] pixel = new int[4];
                        int i = 0;
                        for (int x = 0; x < size; x++) {
                            for (int y = 0; y < size; y++) {
                                img.getData().getPixel(y, x, pixel);
                                for (int j = 0; j < 3; j++) {
                                    data[i] = (byte) pixel[j];
                                    i++;
                                }
                            }
                        }
                        texture = new Texture(size, size, data, Texture.Format.RGB, Texture.Filter.Nearest, Texture.Filter.Nearest);
                    }
                }
            } catch (Exception ex) {
                TemplateAddon.LOG.error(ex);
            }
            locked = false;
        }).start();
    }
}