package net.runelite.client.plugins.microbot.bankseller;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class BankSellerScript extends Script {
    private BankSellerPlugin plugin;
    private BankSellerConfig config;
    private final Set<String> blacklist = new HashSet<>();
    private int itemsSold = 0;

    int getItemsSold() { return itemsSold; }

    public boolean run(BankSellerPlugin plugin, BankSellerConfig config) {
        this.plugin = plugin;
        this.config = config;
        if (!config.blacklist().isBlank()) {
            Arrays.stream(config.blacklist().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(blacklist::add);
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                if (Rs2Inventory.isEmpty()) {
                    if (!handleBank()) {
                        plugin.shutDown();
                    }
                    return;
                }

                handleSelling();
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean handleBank() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.useBank();
            sleepUntil(Rs2Bank::isOpen);
        }

        if (!Rs2Bank.isOpen()) {
            return false;
        }

        if (!Rs2Inventory.isEmpty()) {
            Rs2Bank.depositAll();
            sleepUntil(Rs2Inventory::isEmpty);
        }

        Rs2Bank.setWithdrawAsNote();

        boolean withdrew = false;

        while (!Rs2Inventory.isFull()) {
            Rs2ItemModel nextItem = Rs2Bank.bankItems()
                    .filter(item -> item.isTradeable())
                    .filter(item -> !item.getName().equalsIgnoreCase("Coins"))
                    .filter(item -> blacklist.stream().noneMatch(b -> b.equalsIgnoreCase(item.getName())))
                    .findFirst()
                    .orElse(null);

            if (nextItem == null) {
                break;
            }

            String name = nextItem.getName();
            if (Rs2Bank.withdrawAll(name)) {
                withdrew = true;
                sleepUntil(() -> Rs2Inventory.hasItem(name));
            } else {
                break;
            }
        }

        Rs2Bank.closeBank();
        return withdrew;
    }

    private void handleSelling() {
        while (!Rs2Inventory.isEmpty()) {
            if (!Rs2GrandExchange.isOpen()) {
                Rs2GrandExchange.openExchange();
                sleepUntil(Rs2GrandExchange::isOpen);
                continue;
            }

            if (Rs2GrandExchange.getAvailableSlot() == null && Rs2GrandExchange.hasSoldOffer()) {
                Rs2GrandExchange.collectAllToBank();
                sleepUntil(() -> Rs2GrandExchange.getAvailableSlot() != null);
            }

            Rs2Inventory.items().forEachOrdered(item -> {
                if (!item.isTradeable()) return;
                String name = item.getName();
                if (name.equalsIgnoreCase("Coins")) return;
                if (blacklist.stream().anyMatch(b -> b.equalsIgnoreCase(name))) return;

                int price = Rs2GrandExchange.getPrice(item.getId());
                if (price <= 0) price = 1;
                int sellPrice = (int) (price * 0.85); // low price for quick sale
                Rs2GrandExchange.sellItem(name, item.getQuantity(), sellPrice);

                itemsSold += item.getQuantity();
                sleepUntil(() -> !Rs2GrandExchange.isOfferScreenOpen());
                sleep(300, 600);
            });
        }
    }
}
