package io.github.mrriptide.peakcraft.commands;

import io.github.mrriptide.peakcraft.entity.player.PlayerManager;
import io.github.mrriptide.peakcraft.entity.player.PlayerWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHeal implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (sender instanceof Player){
            PlayerWrapper player = PlayerManager.getPlayer((Player)sender);
            player.regenHealth(Integer.MAX_VALUE);
            ((Player)sender).setHealth(20);
            ((Player)sender).setSaturation(20);
            ((Player)sender).setFoodLevel(20);

            sender.sendMessage("Healed!");
            return true;
        }
        return false;
    }
}
