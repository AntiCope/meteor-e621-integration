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

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import anticope.esixtwoone.sources.ESixTwoOne;
import anticope.esixtwoone.sources.Source;
import anticope.esixtwoone.sources.Source.Size;
import anticope.esixtwoone.sources.Source.SourceType;

import static meteordevelopment.meteorclient.utils.Utils.WHITE;

public class ImageHUD extends HudElement {
    private boolean locked = false;
    private boolean empty = true;
    private int ticks = 0;
    private Source source = new ESixTwoOne();

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
        .onChanged((v) -> {
            source.reset();
            empty = true;
        })
        .build()
    );

    private final Setting<Size> size = sgGeneral.add(new EnumSetting.Builder<Size>()
        .name("size")
        .description("Size mode.")
        .defaultValue(Size.preview)
        .onChanged((v) -> {
            source.reset();
            empty = true;
        })
        .build()
    );

    private final Setting<SourceType> sourceType = sgGeneral.add(new EnumSetting.Builder<SourceType>()
        .name("source")
        .description("Source Type.")
        .defaultValue(SourceType.e621)
        .onChanged((v) -> {
            source = Source.getSource(v);
            source.reset();
            empty = true;
        })
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
        if (locked || source == null)
            return;
        new Thread(() -> {
            try {
                locked = true;
                String url = source.getRandomImage(tags.get(), size.get());
                if (url == null) {
                    locked = false;
                    return;
                }
                TemplateAddon.LOG.info(url);
                var img = NativeImage.read(Http.get(url).sendInputStream());
                mc.getTextureManager().registerTexture(TEXID, new NativeImageBackedTexture(img));
                empty = false;
            } catch (Exception ex) {
                TemplateAddon.LOG.error("Failed to render the image.", ex);
            }
            locked = false;
        }).start();
    }
}
