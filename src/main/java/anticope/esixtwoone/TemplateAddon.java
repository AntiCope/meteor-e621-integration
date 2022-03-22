package anticope.esixtwoone;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.HUD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandles;

public class TemplateAddon extends MeteorAddon {
	public static final Logger LOG = LoggerFactory.getLogger("e621");

	@Override
	public void onInitialize() {
		LOG.info("Initializing E621");

		MeteorClient.EVENT_BUS.registerLambdaFactory("anticope.esixtwoone", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// HUD
		HUD hud = Systems.get(HUD.class);
		hud.elements.add(new ImageHUD(hud));
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
