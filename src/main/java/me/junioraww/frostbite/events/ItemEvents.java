package me.junioraww.frostbite.events;

import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.junioraww.frostbite.Main;
import me.junioraww.frostbite.utils.Body;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemEvents implements Listener {
  private static final MiniMessage serializer = MiniMessage.miniMessage();

  @EventHandler
  public void specialItems(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item != null && item.getType().equals(Material.CARROT_ON_A_STICK) &&
            event.getAction().isRightClick()) {
      if (item.getItemMeta() == null) return;
      var custom = item.getItemMeta().getCustomModelDataComponent().getStrings();
      if (custom.getFirst().equals("frostbite")) {
        String itemType = custom.get(1);
        Player player = event.getPlayer();
        if (itemType.equals("thermo_1")) {
          int stat = PlayerEvents.getTemp(player.getLocation().getBlock());
          Component component = serializer.deserialize(
                  "<blue>Температура среды: <white>" + stat + "°C"
          );
          player.sendActionBar(component);
          player.playSound(Sound.sound(Key.key("minecraft:block.note_block.xylophone"),
                  Sound.Source.AMBIENT, 1, (float) 1 - (20 - stat) / 50f));
          player.swingHand(event.getHand());
        }
        else if (itemType.equals("thermo_2")) {
          Body body = Main.getPlugin().getBodyTemps().get(player.getName().toLowerCase());
          Component component = serializer.deserialize(
                  "<blue>Температура тела: <white>" + Math.ceil((3660 + body.getTemperature() / 8.0)) / 100 + "°C"
          );
          player.sendActionBar(component);
          player.playSound(Sound.sound(Key.key("minecraft:block.note_block.xylophone"),
                  Sound.Source.AMBIENT, 1, (float) 1 + (body.getTemperature() / 4000f)));
          player.swingHand(event.getHand());
        }
      }
    }
  }

  @EventHandler
  public void specialItems(PlayerInteractEntityEvent event) {
    ItemStack item = event.getPlayer().getActiveItem();
    if (item.getType() != Material.AIR && item.getType().equals(Material.CARROT_ON_A_STICK) && event.getHand().equals(EquipmentSlot.HAND)) {
      if (item.getItemMeta() == null) return;
      var custom = item.getItemMeta().getCustomModelDataComponent().getStrings();
      if (custom.getFirst().equals("frostbite")) {
        Entity entity = event.getRightClicked();
        String itemType = custom.get(1);
        Player player = event.getPlayer();
        if (itemType.equals("thermo_2")) {
          Body body;
          if (entity instanceof Player clicked) body = Main.getPlugin().getBodyTemps().get(clicked.getName().toLowerCase());
          else body = new Body();
          Component component = serializer.deserialize(
                  "<blue>Температура игрока: <white>" + Math.ceil((3660 + body.getTemperature() / 10.0)) / 100 + "°C"
          );
          player.sendActionBar(component);
          player.playSound(Sound.sound(Key.key("minecraft:block.note_block.xylophone"),
                  Sound.Source.AMBIENT, 1, (float) 1 + (body.getTemperature() / 4000f)));
          player.swingHand(event.getHand());
        }
      }
    }
  }
}
