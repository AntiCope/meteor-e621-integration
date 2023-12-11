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
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import anticope.esixtwoone.sources.Source;
import anticope.esixtwoone.sources.Source.Size;
import anticope.esixtwoone.sources.Source.SourceType;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.Utils.WHITE;

public class ImageHUD extends HudElement {
    public static final HudElementInfo<ImageHUD> INFO = new HudElementInfo<>(Hud.GROUP, "e621-image", "sex", ImageHUD::create);

    private boolean locked = false;
    private boolean empty = true;
    private int ticks = 0;
    private Source source;

    private static final Identifier TEXID = new Identifier("e621", "tex");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> imgWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("width")
        .description("The scale of the image.")
        .defaultValue(100)
        .min(10)
        .sliderRange(70, 1000)
        .onChanged(o -> updateSize())
        .build()
    );

    private final Setting<Double> imgHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("The scale of the image.")
        .defaultValue(100)
        .min(10)
        .sliderRange(70, 1000)
        .onChanged(o -> updateSize())
        .build()
    );

    private final Setting<String> tags = sgGeneral.add(new StringSetting.Builder()
        .name("tags")
        .description("Tags")
        .defaultValue("femboy")
        .onChanged((v) -> updateSource())
        .build()
    );

    private final Setting<Size> size = sgGeneral.add(new EnumSetting.Builder<Size>()
        .name("size")
        .description("Size mode.")
        .defaultValue(Size.preview)
        .onChanged((v) -> updateSource())
        .build()
    );

    private final Setting<SourceType> sourceType = sgGeneral.add(new EnumSetting.Builder<SourceType>()
        .name("source")
        .description("Source Type.")
        .defaultValue(SourceType.e621)
        .onChanged(v -> updateSource())
        .build()
    );

    private final Setting<Integer> refreshRate = sgGeneral.add(new IntSetting.Builder()
        .name("refresh-rate")
        .description("How often to change (ticks).")
        .defaultValue(1200)
        .max(3000)
        .min(20)
        .sliderRange(20, 3000)
        .build()
    );

    public ImageHUD() {
        super(INFO);
        updateSource();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void remove() {
        super.remove();
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    private static ImageHUD create() {
        return new ImageHUD();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        ticks ++;
        if (ticks >= refreshRate.get()) {
            ticks = 0;
            loadImage();
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        if (empty) {
            loadImage();
            return;
        }

        GL.bindTexture(TEXID);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, imgWidth.get(), imgHeight.get(), WHITE);
        Renderer2D.TEXTURE.render(null);
    }

    private void updateSize() {
        setSize(imgWidth.get(), imgHeight.get());
    }

    private void updateSource() {
        source = Source.getSource(sourceType.get());
        source.reset();
        empty = true;
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
                E621Hud.LOG.info(url);
                var img = NativeImage.read(Http.get(url).sendInputStream());
                mc.getTextureManager().registerTexture(TEXID, new NativeImageBackedTexture(img));
                empty = false;
            } catch (Exception ex) {
                E621Hud.LOG.error("Failed to render the image.", ex);
            }
            locked = false;
        }).start();
        updateSize();
    }
}
