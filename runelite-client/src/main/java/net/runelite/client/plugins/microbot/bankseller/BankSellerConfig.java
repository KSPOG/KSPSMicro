package net.runelite.client.plugins.microbot.bankseller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("BankSeller")
public interface BankSellerConfig extends Config {
    @ConfigItem(
            keyName = "blacklist",
            name = "Blacklist",
            description = "Comma separated list of items not to sell",
            position = 0
    )
    default String blacklist() { return ""; }
}
