package me.junioraww.frostbite;

import me.junioraww.frostbite.commands.Temp;
import me.junioraww.frostbite.events.ItemEvents;
import me.junioraww.frostbite.events.LootEvents;
import me.junioraww.frostbite.events.PlayerEvents;
import me.junioraww.frostbite.events.PortalEvents;
import me.junioraww.frostbite.utils.Body;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
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

  private boolean worldEnabled = false;

  public boolean isWorldEnabled() {
    return worldEnabled;
  }

  @Override
  public void onEnable() {
    plugin = this;

    PlayerEvents.start();

    getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    getServer().getPluginManager().registerEvents(new PortalEvents(), this);
    getServer().getPluginManager().registerEvents(new LootEvents(), this);
    getServer().getPluginManager().registerEvents(new ItemEvents(), this);

    getCommand("temp").setExecutor(new Temp());

    WorldCreator creator = new WorldCreator("world_event");
    creator.environment(World.Environment.NORMAL);
    creator.type(WorldType.NORMAL);

    World world = Bukkit.createWorld(creator);
    world.setSpawnLocation(0, 100, 0);
    world.setAutoSave(true);

    LootEvents.generateLoot();

    Bukkit.getOnlinePlayers().forEach(
            player -> bodyTemps.put(player.getName().toLowerCase(), new Body())
    );
  }

  @Override
  public void onDisable() {
    plugin = null;
    bodyTemps.clear();
  }
}
