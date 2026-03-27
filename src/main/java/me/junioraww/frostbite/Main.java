package me.junioraww.frostbite;

import me.junioraww.frostbite.commands.Temp;
import me.junioraww.frostbite.events.PlayerEvents;
import me.junioraww.frostbite.utils.Body;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin {
  private static Main plugin;
  private Map<String, Body> bodyTemps = new HashMap<>();

  public static Main getPlugin() {
    return plugin;
  }

  public Map<String, Body> getBodyTemps() {
    return bodyTemps;
  }

  @Override
  public void onEnable() {
    plugin = this;

    PlayerEvents.start();

    getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

    getCommand("temp").setExecutor(new Temp());
  }

  @Override
  public void onDisable() {
    plugin = null;
    bodyTemps.clear();
  }
}
