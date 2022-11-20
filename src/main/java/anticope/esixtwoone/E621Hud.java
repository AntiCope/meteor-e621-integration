package anticope.esixtwoone;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;

import meteordevelopment.meteorclient.systems.hud.Hud;
import org.slf4j.Logger;

import net.fabricmc.loader.api.FabricLoader;

public class E621Hud extends MeteorAddon {
	public static final Logger LOG = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		LOG.info("Initializing E621");

		// HUD
        Hud hud = Hud.get();
        hud.register(ImageHUD.INFO);
	}

    @Override
    public String getPackage() {
        return "anticope.esixtwoone";
    }

    @Override
    public String getWebsite() {
        return "https://github.com/AntiCope/meteor-e621-integration";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("AntiCope", "meteor-e621-integration");
    }

    @Override
    public String getCommit() {
        String commit = FabricLoader
            .getInstance()
            .getModContainer("e621-hud")
            .get().getMetadata()
            .getCustomValue("github:sha")
            .getAsString();
        return commit.isEmpty() ? null : commit.trim();
    }
}
