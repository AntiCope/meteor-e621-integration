package anticope.esixtwoone;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.orbit.EventHandler;
import com.google.gson.JsonObject;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;

import static meteordevelopment.meteorclient.utils.Utils.WHITE;

import java.util.Random;

public class ImageHUD extends HudElement {
    public enum Size {
        preview,
        sample,
        file
    }

    private boolean locked = false;
    private boolean empty = true;
    private int ticks = 0;

    private static final Identifier TEXID = new Identifier("e621", "tex");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> imgWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("width")
        .description("The scale of the image.")
        .defaultValue(100)
        .min(10)
        .sliderRange(70, 1000)
        .build()
    );

    private final Setting<Double> imgHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("The scale of the image.")
        .defaultValue(100)
        .min(10)
        .sliderRange(70, 1000)
        .build()
    );

    private final Setting<String> tags = sgGeneral.add(new StringSetting.Builder()
        .name("tags")
        .description("Tags")
        .defaultValue("femboy")
        .onChanged((v) -> empty = true)
        .build()
    );

    private final Setting<Size> size = sgGeneral.add(new EnumSetting.Builder<Size>()
        .name("size")
        .description("The mode for anti kick.")
        .defaultValue(Size.preview)
        .onChanged((v) -> empty = true)
        .build()
    );

    private final Setting<Integer> refreshRate = sgGeneral.add(new IntSetting.Builder()
        .name("refresh-rate")
        .description("How often to change (ticks).")
        .defaultValue(1200)
        .min(20)
        .build()
    );

    public ImageHUD(HUD hud) {
        super(hud, "e621-image", "sex");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!active) return;
        if (mc.world == null) return;
        ticks ++;
        if (ticks >= refreshRate.get()) {
            ticks = 0;
            loadImage();
        }
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(imgWidth.get(), imgHeight.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (empty) {
            loadImage();
            return;
        }

        GL.bindTexture(TEXID);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(box.getX(), box.getY(), imgWidth.get(), imgHeight.get(), WHITE);
        Renderer2D.TEXTURE.render(null);
    }

    private void loadImage() {
        if (locked)
            return;
        new Thread(() -> {
            try {
                locked = true;
                var random = new Random();
                JsonObject result = Http.get("https://e621.net/posts.json?limit=10&tags="+tags.get().replace(" ", "+")+"&page="+ random.nextInt(1, 749)).sendJson(JsonObject.class);
                if (result.get("posts") instanceof JsonArray array) {
                    if (array.get(random.nextInt(0, 11)) instanceof JsonObject post) {
                        var sizeMode = size.get().toString();
                        var url = post.get(sizeMode).getAsJsonObject().get("url").getAsString();
                        TemplateAddon.LOG.info(url);
                        //int width = post.get(sizeMode).getAsJsonObject().get("width").getAsInt();
                        //int height = post.get(sizeMode).getAsJsonObject().get("height").getAsInt();
                        var img = NativeImage.read(Http.get(url).sendInputStream());
                        mc.getTextureManager().registerTexture(TEXID, new NativeImageBackedTexture(img));
                        empty = false;
                    }
                }
            } catch (Exception ex) {
                TemplateAddon.LOG.error("Failed to fetch an image.", ex);
            }
            locked = false;
        }).start();
    }
}
