package net.runelite.client.plugins.microbot.bankseller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("BankSeller")
public interface BankSellerConfig extends Config {
    @ConfigItem(
            keyName = "blacklist",
            name = "Blacklist",
            description = "Comma separated list of items not to sell",
            position = 0
    )
    default String blacklist() { return ""; }

    @ConfigItem(
            keyName = "actionDelay",
            name = "Action Delay",
            description = "Base delay in ms between withdrawing and selling",
            position = 1
    )
    @Range(min = 100, max = 2000)
    default int actionDelay() { return 500; }
}
