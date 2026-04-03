package me.junioraww.frostbite.events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootEvents implements Listener {
  private static final MiniMessage serializer = MiniMessage.miniMessage();
  private static List<ItemStack> availableLoot = new ArrayList<>();

  public static void generateLoot() {
    for (int i = 0; i < 3; i++) {
      for (int t = 1; t <= 2; t++) {
        var thermo = new ItemStack(Material.CARROT_ON_A_STICK);
        var meta = thermo.getItemMeta();
        var customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of("frostbite", "thermo_" + t, "var_" + i));
        meta.setCustomModelDataComponent(customData);

        if (t == 1) meta.customName(serializer.deserialize("<!italic>Термометр"));
        else if (t == 2) meta.customName(serializer.deserialize("<!italic>Градусник"));
        meta.lore(List.of(serializer.deserialize("<!italic><yellow>Апрельское событие 2026")));

        thermo.setItemMeta(meta);
        availableLoot.add(thermo);
      }
    }
  }

  @EventHandler
  public void onLootGenerate(LootGenerateEvent event) {
    if (!event.getWorld().getName().equals("world_event")) return;
      event.getLoot().add(new ItemStack(Material.POTION));
      event.getLoot().add(new ItemStack(Material.POTION));
      event.getLoot().add(availableLoot.get((int) (Math.random() * availableLoot.size())));
  }
}
