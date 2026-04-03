package me.junioraww.frostbite.utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Body {
  private static final DamageSource fireDamage = DamageSource.builder(DamageType.IN_FIRE).build();

  private int temperature = 0;

  public int getTemperature() {
    return temperature;
  }

  public void setTemperature(int temperature) {
    this.temperature = temperature;
  }

  public void add(int amount) {
    this.temperature += amount;
  }

  public void sub(int amount) {
    this.temperature -= amount;
  }

  public int calculateImpact(Player player, int blockTemperature) {
    double impact;
    int impact_2 = 0;

    PlayerInventory inventory = player.getInventory();
    ItemStack boots = inventory.getBoots();
    ItemStack chestplate = inventory.getChestplate();
    ItemStack helmet = inventory.getHelmet();

    Block center = player.getLocation().getBlock();
    World world = center.getWorld();
    byte light = center.getLightFromSky();

    if (center.getType().equals(Material.WATER)) {
      if (temperature > 0) temperature = 0;
    }

    for (int x = -2; x <= 2; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -2; z <= 2; z++) {
          Block block = center.getRelative(x, y, z);
          Material type = block.getType();
          switch (type) {
            case FURNACE -> {
              Furnace furnace = (Furnace) block.getState();
              if (furnace.getBurnTime() > 0) impact_2 += 24;
            }
            case CAMPFIRE -> impact_2 += 12;
            case TORCH -> impact_2 += 4;
            case LAVA -> impact_2 += 8;
            case NETHER_PORTAL -> impact_2 += 6;
          }
        }
      }
    }

    if (inventory.getItemInMainHand().getType().equals(Material.TORCH)
    || inventory.getItemInOffHand().getType().equals(Material.TORCH)) impact_2 += 4;
    if (player.getFireTicks() > 0) impact_2 += 10;

    World.Environment environment = world.getEnvironment();

    if (environment == World.Environment.NORMAL && light == 0) return impact_2;

    if (blockTemperature < 10) {
      impact = (blockTemperature - 10) * (light / 15.0);
      if (chestplate.getType().equals(Material.LEATHER_CHESTPLATE)) impact /= 2;
      else if (boots.getType().equals(Material.LEATHER_BOOTS)) impact /= 2;

      if (!world.isClearWeather() && center.getY() >= world.getHighestBlockYAt(center.getX(), center.getZ())) impact *= 3;
    }
    else if (blockTemperature > 25) {
      impact = environment.equals(World.Environment.NORMAL) ?
              (blockTemperature - 25) * (light / 15.0) // Normal (depends on sky light)
            : (blockTemperature - 25);               // Other

      if (helmet.getType().equals(Material.TURTLE_HELMET)) impact /= 4;
      else if (helmet.getType() != Material.AIR) impact *= (1 + light / 15.0);

      if (chestplate.getType() != Material.AIR) impact *= (1 + light / 30.0);
    }
    else {
      if (temperature > 0) {
        impact = -1;
      }
      else if (temperature < 0) {
        impact = 1;
        if (temperature < -3 && chestplate.getType().equals(Material.LEATHER_CHESTPLATE)) impact += 2;
        if (temperature < -3 && boots.getType().equals(Material.LEATHER_BOOTS)) impact += 2;
      }
      else impact = 0;
    }

    return (int) impact + impact_2;
  }
}
