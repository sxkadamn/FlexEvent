package net.flexevent.arena;
import net.flexevent.FlexEvent;
import net.flexevent.commands.MainCommands;
import net.flexevent.managers.MatchMaker;
import net.flexevent.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FArenaEvents implements Listener {


    private List<Player> winners = new ArrayList<>();

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (MatchMaker.instance.playersInQueue.contains(player)) {
            player.setHealth(0);
            MatchMaker.instance.playersInQueue.remove(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (MatchMaker.instance.playersInQueue.contains(victim)) {
            handleVictimInQueue(victim, killer, event);
            event.getDrops().clear();
        }
    }

    private void handleVictimInQueue(Player victim, Player killer, PlayerDeathEvent event) {
        MatchMaker.instance.playersInQueue.remove(victim);

        event.getDrops().clear();
        victim.getInventory().clear();
        victim.updateInventory();
        killer.getInventory().clear();
        killer.updateInventory();

        if (MatchMaker.instance.playersInQueue.size() == 2) {
            handleCommonCleanUp(victim, killer);
            teleportAndAddWinner(victim, killer);
        } else if (MatchMaker.instance.playersInQueue.size() == 1) {
            handleCommonCleanUp(victim, killer);
            teleportAndAddWinners(victim, killer);
            winnerTeleportAndCleanUp(killer);
        } else {
            handleCommonCleanUp(victim, killer);
            handleMultiplePlayersLeft();
        }
    }

    private void handleCommonCleanUp(Player victim, Player killer) {
        ServerUtils serverUtils = new ServerUtils(FlexEvent.getInstance());
        serverUtils.broadcastMessage("messages.pairs_win", "{win1}", victim.getName(), "{win2}", killer.getName());

        int x = serverUtils.getConfigInt("location.x");
        int y = serverUtils.getConfigInt("location.y");
        int z = serverUtils.getConfigInt("location.z");
        World world = Bukkit.getWorld(serverUtils.getConfigString("world"));
        Location location = new Location(world, x, y, z);
        killer.teleport(location);
        winnerTeleportAndCleanUp(killer);
    }

    private void teleportAndAddWinner(Player victim, Player killer) {
        ServerUtils serverUtils = new ServerUtils(FlexEvent.getInstance());
        int x = serverUtils.getConfigInt("location.x");
        int y = serverUtils.getConfigInt("location.y");
        int z = serverUtils.getConfigInt("location.z");
        World world = Bukkit.getWorld(serverUtils.getConfigString("world"));
        Location location = new Location(world, x, y, z);
        killer.teleport(location);
        winners.add(victim);
    }

    private void teleportAndAddWinners(Player victim, Player killer) {
        teleportAndAddWinner(victim, killer);
        winners.add(killer);
    }

    private void handleMultiplePlayersLeft() {
        FArenaMatch fArenaMatch = FArenaMatch.getArenaBy("arena");
        Collections.shuffle(MatchMaker.instance.playersInQueue);

        if (fArenaMatch != null) {
            Player player1 = MatchMaker.instance.playersInQueue.get(0);
            Player player2 = MatchMaker.instance.playersInQueue.get(1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    fArenaMatch.startGame(player1, player2);
                }
            }.runTaskLater(FlexEvent.getInstance(), 100L);
        }
    }

    private void winnerTeleportAndCleanUp(Player player) {
        player.setHealth(20);
        player.getActivePotionEffects().clear();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
        winners.clear();
        MatchMaker.instance.playersInQueue.clear();
        MainCommands.atomicBoolean.set(true);
    }

}
