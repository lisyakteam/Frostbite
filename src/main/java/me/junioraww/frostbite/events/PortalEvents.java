package me.junioraww.frostbite.events;

import me.junioraww.frostbite.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalEvents implements Listener {
  @EventHandler
  public void onPortal(PlayerPortalEvent event) {
    Player player = event.getPlayer();
    Location from = event.getFrom();
    World fromWorld = from.getWorld();

    if (fromWorld == null) return;

    World world = Bukkit.getWorld("world");
    World eventWorld = Bukkit.getWorld("world_event");

    if (world == null || eventWorld == null) return;

    boolean foundGlowstone = false;

    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        Block check = from.getBlock().getRelative(x, -1, z);

        if (check.getType() == Material.GLOWSTONE) {
          foundGlowstone = true;
          break;
        }
      }
    }

    if (!foundGlowstone) return;

    Location to;

    if (fromWorld.getName().equalsIgnoreCase("world")) {
      double x = from.getX() / 5.0;
      double y = from.getY();
      double z = from.getZ() / 5.0;

      to = new Location(eventWorld, x, y, z, from.getYaw(), from.getPitch());
    } else if (fromWorld.getName().equalsIgnoreCase("world_event")) {
      double x = from.getX() * 5.0;
      double y = from.getY();
      double z = from.getZ() * 5.0;

      to = new Location(world, x, y, z, from.getYaw(), from.getPitch());
    } else {
      return;
    }

    event.setCancelled(true);

    Location existingPortal = findNearbyPortal(to, 5); // radius = 5 блоков
    if (existingPortal != null) {
      Bukkit.getLogger().info("Teleport to: " + to);
      Bukkit.getScheduler().runTask(Main.getPlugin(), task -> {
        player.teleport(existingPortal);
      });
    } else {
      Bukkit.getLogger().info("Teleport to with creation: " + to);
      Bukkit.getScheduler().runTask(Main.getPlugin(), task -> {
        player.teleport(to);
      });

      Bukkit.getScheduler().runTask(Main.getPlugin(), task -> {
        createGlowstonePortal(to);
      });
    }
  }

  public Location findNearbyPortal(Location loc, int radius) {
    World world = loc.getWorld();
    if (world == null) return null;

    int startX = loc.getBlockX() - radius;
    int endX = loc.getBlockX() + radius;
    int startY = loc.getBlockY() - radius;
    int endY = loc.getBlockY() + radius;
    int startZ = loc.getBlockZ() - radius;
    int endZ = loc.getBlockZ() + radius;

    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        for (int z = startZ; z <= endZ; z++) {
          Block block = world.getBlockAt(x, y, z);
          if (block.getType() == Material.NETHER_PORTAL) {
            return block.getLocation();
          }
        }
      }
    }
    return null;
  }

  public void createGlowstonePortal(Location loc) {
    for (int x = -1; x <= 2; x++) {
      for (int y = 0; y <= 4; y++) {
        Location l = loc.clone().add(x, y, 0);

        if (x == -1 || x == 2 || y == 0 || y == 4) {
          if (l.getBlock().isEmpty()) l.getBlock().setType(Material.GLOWSTONE, false);
        } else {
          if (l.getBlock().isEmpty()) l.getBlock().setType(Material.NETHER_PORTAL, false);
        }
      }
    }
  }

  @EventHandler
  public void ignite(BlockIgniteEvent event) {
    Block block = event.getBlock();
    Player player = event.getPlayer();
    if (player == null) return;

    Block base = event.getBlock().getRelative(0, -1, 0);
    if (base.getType() != Material.GLOWSTONE) return;

    Block second = null;

    search: for (int[] dir : dirs) {
      Block relative = base.getRelative(dir[0], 0, dir[1]);
      side: if (relative.getType() == Material.GLOWSTONE) {
        for (int i = 0; i < 3; i++) {
          if (base.getRelative(dir[0] * 2, 1 + i, dir[1] * 2).getType() != Material.GLOWSTONE)
            break side;
        }
        for (int i = 0; i < 3; i++) {
          if (base.getRelative(-dir[0], 1 + i, -dir[1]).getType() != Material.GLOWSTONE)
            break side;
        }
        if (base.getRelative(0, 4, 0).getType() != Material.GLOWSTONE) break side;
        if (base.getRelative(dir[0], 4, dir[1]).getType() != Material.GLOWSTONE) break side;
        for (int i = 0; i < 3; i++) {
          if (!base.getRelative(0, 1 + i, 0).isPassable()
          || !base.getRelative(dir[0], 1 + i, dir[1]).isPassable()) break side;
        }
        second = relative;
        break search;
      }
    }

    if (second == null) return;

    var dirX = block.getLocation().clone().subtract(second.getLocation()).getX();
    var center = block.getLocation().toCenterLocation().clone().add(second.getLocation().toCenterLocation()).multiply(0.5);
    block.getWorld().spawn(center, LightningStrike.class);

    Orientable data = (Orientable) Bukkit.createBlockData(Material.NETHER_PORTAL);
    data.setAxis(dirX != 0 ? Axis.X : Axis.Z);

    Block finalSecond = second;
    Bukkit.getScheduler().runTask(Main.getPlugin(), task -> {
      for (int i = 1; i < 4; i++) {
        Block relA = base.getRelative(0, i, 0);
        Block relB = finalSecond.getRelative(0, i, 0);
        relA.setType(Material.NETHER_PORTAL, true);
        relA.setBlockData(data, true);
        relB.setType(Material.NETHER_PORTAL, true);
        relB.setBlockData(data, true);
      }
    });
  }

  static final int[][] dirs = new int[][] {
          new int[]{ -1, 0 },
          new int[]{ 1, 0 },
          new int[]{ 0, -1 },
          new int[]{ 0, 1 },
  };
}
