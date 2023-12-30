package net.flexevent.managers;

import net.flexevent.FlexEvent;
import net.flexevent.arena.FArenaMatch;
import net.flexevent.commands.MainCommands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchMaker {
    public static final MatchMaker instance = new MatchMaker();
    public final List<Player> playersInQueue = new ArrayList<>();

    public void joinQueue() {
        new BukkitRunnable() {
            private int elapsedTime = 0;
            private final int maxWaitingTime = getConfig().getInt("settings.wait_time");
            private final int maxPlayers = getConfig().getInt("settings.max_players");

            @Override
            public void run() {
                int currentPlayers = playersInQueue.size();

                if (currentPlayers >= maxPlayers || elapsedTime >= maxWaitingTime) {
                    startMatch();
                }

                elapsedTime++;
            }
        }.runTaskTimer(FlexEvent.getInstance(), 0L, 20L);
    }

    private void startMatch() {
        Collections.shuffle(playersInQueue);

        if (playersInQueue.size() >= 2) {
            Player player1 = playersInQueue.get(0);
            Player player2 = playersInQueue.get(1);

            teleportPlayers(playersInQueue);

            broadcastWaitMessages();

            MainCommands.atomicBoolean.set(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    FArenaMatch arenaMatch = FArenaMatch.getArenaBy("arena");
                    if (arenaMatch != null) {
                        arenaMatch.startGame(player1, player2);
                    }
                }
            }.runTaskLater(FlexEvent.getInstance(), 100L);

            playersInQueue.clear();
        }
    }

    private void teleportPlayers(List<Player> players) {
        int x = getConfig().getInt("location.x");
        int y = getConfig().getInt("location.y");
        int z = getConfig().getInt("location.z");
        World world = Bukkit.getWorld(getConfig().getString("world"));
        Location location = new Location(world, x, y, z);

        players.forEach(player -> player.teleport(location));
    }

    private void broadcastWaitMessages() {
        getConfig().getStringList("messages.wait").forEach(message ->
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message)));
    }

    private FileConfiguration getConfig() {
        return FlexEvent.getInstance().getConfig();
    }

    public void leaveQueue(Player player) {
        this.playersInQueue.remove(player);
    }
}
