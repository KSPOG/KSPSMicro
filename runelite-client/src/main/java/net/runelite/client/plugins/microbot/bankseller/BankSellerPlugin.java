package net.runelite.client.plugins.microbot.bankseller;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Bank Seller",
        description = "Sells bank items using the Grand Exchange",
        tags = {"bank", "seller", "ge", "microbot"},
        enabledByDefault = false
)
public class BankSellerPlugin extends Plugin {
    static final String VERSION = "1.0";

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BankSellerOverlay overlay;
    @Inject
    private BankSellerConfig config;
    @Inject
    private BankSellerScript script;

    @Getter
    private Instant startTime;

    @Provides
    BankSellerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BankSellerConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        startTime = Instant.now();
        overlayManager.add(overlay);
        script.run(this, config);
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        overlayManager.remove(overlay);
    }

    int getItemsSold() { return script.getItemsSold(); }
}
