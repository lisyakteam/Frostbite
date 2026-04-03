package me.junioraww.frostbite.commands;

import me.junioraww.frostbite.Main;
import me.junioraww.frostbite.events.PlayerEvents;
import me.junioraww.frostbite.utils.Body;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Temp implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && player.isOp()) {
      Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();
      Body body = bodyTemps.get(player.getName().toLowerCase());
      player.sendRichMessage("<yellow>Температура тела: <white>" + Math.ceil((3660 + body.getTemperature() / 8.0)) / 100);
      player.sendRichMessage("<yellow>Температура биома: <white>" + PlayerEvents.getTemp(player.getLocation().getBlock()));
      player.sendRichMessage("<yellow>Debug: <white>" + body.getTemperature());
    }

    return true;
  }
}
