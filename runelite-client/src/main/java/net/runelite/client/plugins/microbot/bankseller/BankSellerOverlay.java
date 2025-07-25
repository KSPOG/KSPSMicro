package net.runelite.client.plugins.microbot.bankseller;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.misc.TimeUtils;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

public class BankSellerOverlay extends OverlayPanel {
    private final BankSellerPlugin plugin;

    @Inject
    BankSellerOverlay(BankSellerPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 100));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Bank Seller v" + BankSellerPlugin.VERSION)
                .color(Color.GREEN)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Items Sold:")
                .right(Integer.toString(plugin.getItemsSold()))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time running:")
                .right(TimeUtils.getFormattedDurationBetween(plugin.getStartTime(), Instant.now()))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(Microbot.status)
                .build());
        return super.render(graphics);
    }
}
