package net.flexevent;

import net.flexevent.arena.FArenaEvents;
import net.flexevent.commands.MainCommands;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlexEvent extends JavaPlugin {

    private static FlexEvent instance;

    public static FlexEvent getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new FArenaEvents(), this);
        getCommand("flexevent").setExecutor(new MainCommands());
    }

}
