package net.flexevent.commands;

import net.flexevent.FlexEvent;
import net.flexevent.arena.FArenaMatch;
import net.flexevent.managers.MatchMaker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainCommands implements CommandExecutor {

    public static AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (player.isOp()) {
                FlexEvent.getInstance().getConfig().getStringList("messages.help").forEach(s1 -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', s1)));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Зарегистрироваться на турнир: &f&l/flexevent join"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("start") && player.isOp()) {
            if (!atomicBoolean.get()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.register_already")));
                return true;
            }

            Bukkit.getOnlinePlayers().forEach(players ->
                    FlexEvent.getInstance().getConfig().getStringList("messages.start_event").forEach(s1 ->
                            players.sendMessage(ChatColor.translateAlternateColorCodes('&', s1))));

            atomicBoolean.set(false);
            MatchMaker.instance.joinQueue();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.register_start")));
            return true;
        }
        if (args[0].equalsIgnoreCase("join")) {
            if (atomicBoolean.get()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.tournament_not_started")));
            } else if (hasItemsInInventory(player)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.clear_inventory")));
            } else if (!MatchMaker.instance.playersInQueue.contains(player)) {
                MatchMaker.instance.playersInQueue.add(player);

                for (Player players : Bukkit.getOnlinePlayers()) {
                    FlexEvent.getInstance().getConfig().getStringList("messages.joined").forEach(s1 ->
                            players.sendMessage(ChatColor.translateAlternateColorCodes('&', s1.replace("{joined}",
                                            String.valueOf(MatchMaker.instance.playersInQueue.size()))
                                    .replace("{name}", player.getName()))));
                }

                int x = FlexEvent.getInstance().getConfig().getInt("location.x");
                int y = FlexEvent.getInstance().getConfig().getInt("location.y");
                int z = FlexEvent.getInstance().getConfig().getInt("location.z");
                World world = Bukkit.getWorld(FlexEvent.getInstance().getConfig().getString("world"));
                Location location = new Location(world, x, y, z);
                player.teleport(location);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.success_register")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', FlexEvent.getInstance().getConfig().getString("messages.fall_register")));
            }

            return true;
        }
        if (args[0].equalsIgnoreCase("create") && player.isOp()) {
            if (args.length > 1) {
                new FArenaMatch(args[1]);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Вы создали арену: " + args[1]));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cУкажите имя арены."));
            }
            return true;
        }

        if (player.isOp()) {
            FArenaMatch fArenaMatch = null;

            if (args[0].equalsIgnoreCase("pos1")) {
                fArenaMatch = FArenaMatch.getArenaBy("arena");
                if (fArenaMatch != null) {
                    fArenaMatch.setFirstSpawn(player.getLocation());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Вы создали точку для арены #1"));
                }
            } else if (args[0].equalsIgnoreCase("pos2")) {
                fArenaMatch = FArenaMatch.getArenaBy("arena");
                if (fArenaMatch != null) {
                    fArenaMatch.setSecondSpawn(player.getLocation());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Вы создали точку для арены #2"));
                }
            }

            if (fArenaMatch != null) {
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("setinv") && player.isOp()) {
            ItemStack[] main = player.getInventory().getContents();
            for (int i = main.length - 2; i > main.length - 6 && main.length == 36; i--) {
                main[i] = null;
            }
            setMainContent(main);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lВы успешно создали кит на турнир"));
            player.updateInventory();
            return true;
        } else if (args[0].equalsIgnoreCase("leave") && MatchMaker.instance.playersInQueue.contains(player)) {
            MatchMaker.instance.playersInQueue.remove(player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dВы покинули турнир!"));
            return true;
        }
        return true;
    }

    public void setMainContent(ItemStack[] mainContent) {
        List<String> serializedItems = new ArrayList<>();
        for (ItemStack item : mainContent) {
            if (item != null) {
                serializedItems.add(serializeItemStack(item));
            } else {
                serializedItems.add("");
            }
        }

        FlexEvent.getInstance().getConfig().set("inventory", serializedItems);
        FlexEvent.getInstance().saveConfig();
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

    private String serializeItemStack(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
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
    private boolean hasItemsInInventory(Player player) {
        return Arrays.stream(player.getInventory().getContents()).anyMatch(Objects::nonNull);
    }
}
