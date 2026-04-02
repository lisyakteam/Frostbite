package me.junioraww.frostbite.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.type.Candle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
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
    int impact;

    PlayerInventory inv = player.getInventory();
    var boots = inv.getBoots();
    var leg = inv.getLeggings();
    var chest = inv.getChestplate();
    var helmet = inv.getHelmet();

    if (temperature > 0) impact = -1;
    else if (temperature < 0) {
      impact = 1;
      if (chest.getType().equals(Material.LEATHER_CHESTPLATE)) impact += 1;
    }
    else impact = 0;

    if (player.isSprinting()) impact += 1;

    if (blockTemperature > 25) {
      int add = blockTemperature - 25;

      if (helmet != null && !helmet.isEmpty()) {
        Material type = helmet.getType();
        if (type.equals(Material.TURTLE_HELMET)) add /= 4;
        else if (!type.equals(Material.CHAINMAIL_HELMET) && !type.equals(Material.LEATHER_HELMET)) add *= 1.5;
      }
      if (chest != null && !chest.isEmpty()) {
        Material type = chest.getType();
        if (!type.equals(Material.CHAINMAIL_CHESTPLATE) && !type.equals(Material.LEATHER_CHESTPLATE)) add *= 1.5;
      }
      if (leg != null && !leg.isEmpty()) {
        Material type = leg.getType();
        if (!type.equals(Material.CHAINMAIL_LEGGINGS) && !type.equals(Material.LEATHER_LEGGINGS)) add *= 1.5;
      }
      if (boots != null && !boots.isEmpty()) {
        Material type = boots.getType();
        if (!type.equals(Material.CHAINMAIL_BOOTS) && !type.equals(Material.LEATHER_BOOTS)) add *= 1.5;
      }

      impact += add;
    }
    if (blockTemperature < 10) {
      double sub = blockTemperature - 10;

      if (helmet != null && !helmet.isEmpty()) {
        Material type = helmet.getType();
        if (type.equals(Material.LEATHER_HELMET)) sub /= 1.5;
        else if (!type.equals(Material.TURTLE_HELMET)) sub *= 1.5;
      }
      if (chest != null && !chest.isEmpty()) {
        Material type = chest.getType();
        if (type.equals(Material.LEATHER_CHESTPLATE)) sub /= 1.5;
        else sub *= 1.5;
      }
      if (leg != null && !leg.isEmpty()) {
        Material type = leg.getType();
        if (type.equals(Material.LEATHER_LEGGINGS)) sub /= 1.5;
        else sub *= 1.5;
      }
      if (boots != null && !boots.isEmpty()) {
        Material type = boots.getType();
        if (type.equals(Material.LEATHER_BOOTS)) sub /= 1.5;
        else sub *= 1.5;
      }

      impact = impact + (int) sub;
    }

    Block center = player.getLocation().getBlock();

    /*if (!center.getWorld().isClearWeather()) {
      player.sendMessage("weather");
      if (player.getY() >= center.getWorld().getHighestBlockYAt(center.getX(), center.getZ())) {
        impact = impact / 3 - 3;
      }
    }*/

    for (int[] side : sides) {
      Block relative = center.getRelative(side[0], side[1], side[2]);
      if (relative.getState() instanceof Furnace furnace && furnace.getBurnTime() > 0) {
        if (side[1] == -1) player.damage(1, fireDamage);
        impact += 28;
        break;
      }
      else if (relative.getState() instanceof Campfire) {
        impact += 14;
        break;
      }
      else if (relative.getType().equals(Material.TORCH) || relative.getState() instanceof Candle) {
        impact += 5;
        break;
      }
      else if (relative.getType().equals(Material.MAGMA_BLOCK)) {
        impact += 3;
      }
      else if (relative.getType().equals(Material.LAVA)) {
        impact += 5;
      }
    }

    if (center.getType().equals(Material.WATER)) impact /= 2;

    if (player.getFireTicks() > 0) impact += 4;

    return impact;
  }

  static final int[][] sides = new int[][] {
          {0, 0, 0},
          { 0, -1, 0 },
          { 0, 2, 0 },
          { 1, 0, 0 },
          { -1, 0, 0 },
          { 0, 0, 1 },
          { 0, 0, -1 },
  };
}
