package net.flexevent.arena;

import net.flexevent.FlexEvent;
import net.flexevent.managers.MatchMaker;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FArenaMatch {
    private static final List<FArenaMatch> arenas = new ArrayList<>();

    private final String name;
    private Location firstSpawn = null;
    private Location secondSpawn = null;
    private Player firstPlayer = null;
    private Player secondPlayer = null;

    public FArenaMatch(String name) {
        this.name = name;
        arenas.add(this);
    }

    public void startGame(Player firstPlayer, Player secondPlayer) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;

        firstPlayer.teleport(firstSpawn);
        secondPlayer.teleport(secondSpawn);

        preparePlayer(firstPlayer);
        preparePlayer(secondPlayer);

        for (Player player : MatchMaker.instance.playersInQueue) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FlexEvent.getInstance().getConfig().getString("messages.pvp")
                            .replace("{player}", firstPlayer.getName())
                            .replace("{enemy}", secondPlayer.getName())));
        }
    }

    public void preparePlayer(Player player) {
        player.setHealth(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getActivePotionEffects().clear();


        for (ItemStack item : getMainContent()) {
            if (item != null) {
                if (item.equals(getMainContent().length - 1)) {
                    player.getInventory().setItemInOffHand(item);
                } else if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(item);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }

    private ItemStack[] getMainContent() {
        List<String> serializedItems = FlexEvent.getInstance().getConfig().getStringList("inventory");
        ItemStack[] mainContent = new ItemStack[serializedItems.size()];

        for (int i = 0; i < serializedItems.size(); i++) {
            String serializedItem = serializedItems.get(i);
            if (!serializedItem.isEmpty()) {
                mainContent[i] = deserializeItemStack(serializedItem);
            }
        }

        return mainContent;
    }

    private ItemStack deserializeItemStack(String serializedItem) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(serializedItem);
            return config.getItemStack("item");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static FArenaMatch getArenaBy(String name) {
        for (FArenaMatch arena : arenas) {
            if (Objects.equals(arena.name, name)) {
                return arena;
            }
        }
        return null;
    }

    public static List<FArenaMatch> getArenas() {
        return arenas;
    }

    public String getName() {
        return name;
    }

    public Location getFirstSpawn() {
        return firstSpawn;
    }

    public void setFirstSpawn(Location firstSpawn) {
        this.firstSpawn = firstSpawn;
    }

    public Location getSecondSpawn() {
        return secondSpawn;
    }

    public void setSecondSpawn(Location secondSpawn) {
        this.secondSpawn = secondSpawn;
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(Player secondPlayer) {
        this.secondPlayer = secondPlayer;
    }

}
