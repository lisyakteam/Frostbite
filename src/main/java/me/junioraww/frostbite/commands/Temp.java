package me.junioraww.frostbite.commands;

import me.junioraww.frostbite.Main;
import me.junioraww.frostbite.utils.Body;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Temp implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      Map<String, Body> bodyTemps = Main.getPlugin().getBodyTemps();
      Body body = bodyTemps.get(player.getName().toLowerCase());
      player.sendRichMessage("<yellow>Температура тела: <white>" + Math.ceil(366 + body.getTemperature() / 2.0) / 10);
    }

    return true;
  }
}
