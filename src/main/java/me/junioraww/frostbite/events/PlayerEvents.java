package me.junioraww.frostbite.events;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.junioraww.frostbite.Main;
import me.junioraww.frostbite.utils.Body;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents implements Listener {
  private static final DamageSource fireDamage = DamageSource.builder(DamageType.IN_FIRE).build();
  private static final DamageSource coldDamage = DamageSource.builder(DamageType.FREEZE).build();
  private static final MiniMessage serializer = MiniMessage.miniMessage();

  @EventHandler
  public void playerJoined(PlayerJoinEvent event) {
    Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();
    Player player = event.getPlayer();
    String name = player.getName().toLowerCase();

    if (!bodyTemps.containsKey(name)) {
      bodyTemps.put(name, new Body());
    }
  }

  @EventHandler
  public void playerRespawn(PlayerRespawnEvent event) {
    Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();
    Player player = event.getPlayer();
    String name = player.getName().toLowerCase();

    if (bodyTemps.containsKey(name)) {
      bodyTemps.get(name).setTemperature(0);
    }
  }

  @EventHandler
  public void playerDrink(PlayerItemConsumeEvent event) {
    Material type = event.getItem().getType();
    if (type == Material.POTION) {
      Player player = event.getPlayer();
      Body body = Main.getPlugin().getBodyTemps().get(player.getName().toLowerCase());

      if (body.getTemperature() > 0 && body.getTemperature() < 1000) body.setTemperature(0);
      else if (body.getTemperature() >= 1000) body.sub(1000);
    }
    else if (type == Material.BAKED_POTATO) {
      Player player = event.getPlayer();
      Body body = Main.getPlugin().getBodyTemps().get(player.getName().toLowerCase());

      if (body.getTemperature() < 0 && body.getTemperature() > -1000) body.setTemperature(0);
      else if (body.getTemperature() <= -1000) body.add(1000);
    }
    else if (type == Material.POISONOUS_POTATO) {
      Player player = event.getPlayer();
      Body body = Main.getPlugin().getBodyTemps().get(player.getName().toLowerCase());
      String model = event.getItem().getItemMeta().getItemModel().asString();
      if (model.equals("cnk.tea")) {
        if (body.getTemperature() > -1000 && body.getTemperature() < 0) body.setTemperature(0);
        else if (body.getTemperature() <= -1000) body.add(1000);
      }
      else if (model.equals("cnk.coffee")) {
        if (body.getTemperature() > -1000 && body.getTemperature() < 0) body.setTemperature(0);
        else if (body.getTemperature() <= -1000) body.add(1000);
      }
      else if (model.equals("cnk.beer")) {
        if (body.getTemperature() > -1000 && body.getTemperature() < 0) body.setTemperature(0);
        else if (body.getTemperature() <= -1000) body.add(1000);
      }
    }
  }

  @EventHandler
  public void playerJump(PlayerJumpEvent event) {
    Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();
    Player player = event.getPlayer();
    String name = player.getName().toLowerCase();
    Body body = bodyTemps.get(name);
    body.add(1);
  }

  private static Map<UUID, Integer> itemUse = new HashMap<>();

  public static void start() {
    Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();

    Main.getPlugin().getServer().getScheduler().runTaskTimer(Main.getPlugin(), task -> {
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getLocation().distanceSquared(player.getWorld().getSpawnLocation()) < 225) continue;

        Block block = player.getLocation().getBlock();
        Body body = bodyTemps.get(player.getName().toLowerCase());

        int blockTemperature = getTemp(block);
        int bodyTemperature = body.getTemperature();

        int impact = body.calculateImpact(player, blockTemperature);

        if (player.getWorld().getName().equals("world_event")) impact *= 2;

        if (bodyTemperature <= 4000 && bodyTemperature >= -4000) {
          body.add(impact);
          if (body.getTemperature() > 4000) body.setTemperature(4000);
          else if (body.getTemperature() < -4000) body.setTemperature(-4000);
        }

        int T = body.getTemperature();

        if (T < -1000) player.sendActionBar(serializer.deserialize("<aqua>Вам холодно..."));
        if (T > 1000) {
          ItemStack hand = player.getInventory().getItemInMainHand();
          if (hand.getType() == Material.ICE || hand.getType() == Material.PACKED_ICE || hand.getType() == Material.BLUE_ICE) {
            if (!itemUse.containsKey(player.getUniqueId())) itemUse.put(player.getUniqueId(), 5);
            else itemUse.put(player.getUniqueId(), itemUse.get(player.getUniqueId()) - 1);

            int value = itemUse.get(player.getUniqueId());
            if (value == 0) {
              hand.setAmount(hand.getAmount() - 1);
              body.setTemperature(0);
              itemUse.remove(player.getUniqueId());
              player.sendActionBar(Component.text());
              player.playSound(Sound.sound(Key.key("minecraft:entity.player.burp"), Sound.Source.PLAYER, 1, 1));
            }
            else player.sendActionBar(serializer.deserialize("<gold>Использование через: <white>" + value));
          }
          else {
            if (itemUse.containsKey(player.getUniqueId())) itemUse.remove(player.getUniqueId());
            player.sendActionBar(serializer.deserialize("<gold>Вам жарко..."));
          }
        }

        if (T > 3900 && Math.random() > 0.8) player.damage(1, fireDamage);
        else if (T < -3900) player.setFreezeTicks(70);

        if (T > 2500) {
          double radius = 10 * Math.sqrt(Math.random());
          double angle = Math.random() * 2 * Math.PI;
          double xOffset = Math.cos(angle) * radius;
          double zOffset = Math.sin(angle) * radius;
          Location center = player.getLocation().clone().add(xOffset, 0, zOffset);
          if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
            Block highest = center.getWorld().getHighestBlockAt(center.getBlockX(), center.getBlockZ());
            center.setY(highest.getY());
            if (Math.abs(center.getY() - player.getY()) <= 2) {
              player.sendBlockChange(center, Material.WATER.createBlockData());
            }
          }
          else {
            boolean found = false;
            for (int y = player.getLocation().getBlockY() + 2; y > player.getLocation().getBlockY() - 2; y--) {
              center.setY(y);
              Material type = center.getBlock().getType();
              if (!type.isAir() && type.isOccluding()) {
                found = true;
                break;
              }
            }
            if (!found) return;
            player.sendBlockChange(center, Material.WATER.createBlockData());
          }
        }

        if (T > 1000 || T < -1000) {
          PotionEffect effect = null;
          if (T > 3000) effect = getEffect(PotionEffectType.WEAKNESS, 3);
          else if (T > 2000) effect = getEffect(PotionEffectType.SPEED, 2);
          else if (T > 1000) effect = getEffect(PotionEffectType.WEAKNESS, 1);
          else if (T < -3000) effect = getEffect(PotionEffectType.SLOWNESS, 3);
          else if (T < -2000) effect = getEffect(PotionEffectType.SLOWNESS, 2);
          else if (T < -1000) effect = getEffect(PotionEffectType.SLOWNESS, 1);

          if (player.hasPotionEffect(effect.getType()))
            player.getPotionEffect(effect.getType()).withAmplifier(effect.getAmplifier()).withDuration(70).apply(player);
          else player.addPotionEffect(effect);
        }
      }
    }, 20L, 20L);
  }

  private static PotionEffect getEffect(PotionEffectType type, int amplifier) {
    return new PotionEffect(type, 70, amplifier, true, false);
  }

  private static final Range defaultRange = new Range(10, 25);

  public static int getTemp(Block block) {
    World world = block.getWorld();
    Range range = temps.get(block.getBiome());

    if (range == null) range = defaultRange;

    double time = ((world.getTime() % 24000 + 6000) % 24000) / 240.0;
    double dayFactor = (Math.sin((time / 100.0) * Math.PI) );
    double temp = range.getNight() + (range.getDay() - range.getNight()) * dayFactor;

    if (world.hasStorm()) {
      temp = range.getNight();
    }

    int y = block.getY();
    if (y > 50 && temp > 0) {
      int steps = (y - 50) / 10;
      temp -= steps;
    }

    double x = block.getX();
    double z = block.getZ();
    double noise =
            Math.sin(x * 0.01) +
                    Math.sin(z * 0.01) +
                    Math.sin((x + z) * 0.01);

    noise = noise / 3.0;
    noise *= 2.0;
    temp += noise;

    return (int) Math.round(temp);
  }

  private final static Map<Biome, Range> temps = Map.<Biome, Range>ofEntries(
          Map.entry(Biome.BAMBOO_JUNGLE, new Range(20, 32)),
          Map.entry(Biome.BASALT_DELTAS, new Range(30, 40)), // ад
          Map.entry(Biome.BEACH, new Range(15, 28)),
          Map.entry(Biome.BIRCH_FOREST, new Range(10, 22)),
          Map.entry(Biome.CHERRY_GROVE, new Range(8, 20)),
          Map.entry(Biome.COLD_OCEAN, new Range(0, 10)),
          Map.entry(Biome.CRIMSON_FOREST, new Range(25, 40)),
          Map.entry(Biome.DARK_FOREST, new Range(8, 18)),
          Map.entry(Biome.DEEP_COLD_OCEAN, new Range(0, 6)),
          Map.entry(Biome.DEEP_DARK, new Range(5, 12)),
          Map.entry(Biome.DEEP_FROZEN_OCEAN, new Range(-5, 2)),
          Map.entry(Biome.DEEP_LUKEWARM_OCEAN, new Range(10, 20)),
          Map.entry(Biome.DEEP_OCEAN, new Range(4, 12)),
          Map.entry(Biome.DESERT, new Range(5, 42)),
          Map.entry(Biome.DRIPSTONE_CAVES, new Range(10, 16)),
          Map.entry(Biome.END_BARRENS, new Range(-20, -5)),
          Map.entry(Biome.END_HIGHLANDS, new Range(-25, -10)),
          Map.entry(Biome.END_MIDLANDS, new Range(-22, -8)),
          Map.entry(Biome.ERODED_BADLANDS, new Range(10, 38)),
          Map.entry(Biome.FLOWER_FOREST, new Range(10, 22)),
          Map.entry(Biome.FOREST, new Range(8, 20)),
          Map.entry(Biome.FROZEN_OCEAN, new Range(-5, 3)),
          Map.entry(Biome.FROZEN_PEAKS, new Range(-30, -10)),
          Map.entry(Biome.FROZEN_RIVER, new Range(-10, 2)),
          Map.entry(Biome.GROVE, new Range(-5, 8)),
          Map.entry(Biome.ICE_SPIKES, new Range(-25, -8)),
          Map.entry(Biome.JAGGED_PEAKS, new Range(-20, -5)),
          Map.entry(Biome.JUNGLE, new Range(22, 34)),
          Map.entry(Biome.LUKEWARM_OCEAN, new Range(12, 24)),
          Map.entry(Biome.LUSH_CAVES, new Range(14, 20)),
          Map.entry(Biome.MANGROVE_SWAMP, new Range(22, 34)),
          Map.entry(Biome.MEADOW, new Range(5, 18)),
          Map.entry(Biome.MUSHROOM_FIELDS, new Range(12, 20)),
          Map.entry(Biome.NETHER_WASTES, new Range(35, 40)),
          Map.entry(Biome.OCEAN, new Range(5, 18)),
          Map.entry(Biome.OLD_GROWTH_BIRCH_FOREST, new Range(8, 18)),
          Map.entry(Biome.OLD_GROWTH_PINE_TAIGA, new Range(-5, 15)),
          Map.entry(Biome.OLD_GROWTH_SPRUCE_TAIGA, new Range(-8, 14)),
          Map.entry(Biome.PALE_GARDEN, new Range(6, 16)),
          Map.entry(Biome.PLAINS, new Range(10, 25)),
          Map.entry(Biome.RIVER, new Range(8, 20)),
          Map.entry(Biome.SAVANNA, new Range(18, 35)),
          Map.entry(Biome.SAVANNA_PLATEAU, new Range(15, 30)),
          Map.entry(Biome.SMALL_END_ISLANDS, new Range(-20, -10)),
          Map.entry(Biome.SNOWY_BEACH, new Range(-5, 5)),
          Map.entry(Biome.SNOWY_PLAINS, new Range(-15, 0)),
          Map.entry(Biome.SNOWY_SLOPES, new Range(-20, -5)),
          Map.entry(Biome.SNOWY_TAIGA, new Range(-10, 5)),
          Map.entry(Biome.SOUL_SAND_VALLEY, new Range(30, 40)),
          Map.entry(Biome.SPARSE_JUNGLE, new Range(20, 32)),
          Map.entry(Biome.STONY_PEAKS, new Range(-10, 5)),
          Map.entry(Biome.STONY_SHORE, new Range(5, 18)),
          Map.entry(Biome.SUNFLOWER_PLAINS, new Range(12, 26)),
          Map.entry(Biome.SWAMP, new Range(18, 30)),
          Map.entry(Biome.TAIGA, new Range(-5, 15)),
          Map.entry(Biome.THE_END, new Range(-25, -10)),
          Map.entry(Biome.THE_VOID, new Range(10, 15)),
          Map.entry(Biome.WARM_OCEAN, new Range(20, 30)),
          Map.entry(Biome.WARPED_FOREST, new Range(25, 40)),
          Map.entry(Biome.WINDSWEPT_FOREST, new Range(5, 18)),
          Map.entry(Biome.WINDSWEPT_GRAVELLY_HILLS, new Range(2, 15)),
          Map.entry(Biome.WINDSWEPT_HILLS, new Range(3, 16)),
          Map.entry(Biome.WINDSWEPT_SAVANNA, new Range(15, 32)),
          Map.entry(Biome.WOODED_BADLANDS, new Range(12, 36))
  );

  public static class Range {
    final int night;
    final int day;

    public Range(int night, int day) {
      this.night = night;
      this.day = day;
    }

    public int getDay() {
      return day;
    }

    public int getNight() {
      return night;
    }
  }
}
