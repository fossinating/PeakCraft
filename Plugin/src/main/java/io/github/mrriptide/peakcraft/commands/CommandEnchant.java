package io.github.mrriptide.peakcraft.commands;

import io.github.mrriptide.peakcraft.exceptions.ItemException;
import io.github.mrriptide.peakcraft.items.*;
import io.github.mrriptide.peakcraft.items.enchantments.Enchantment;
import io.github.mrriptide.peakcraft.items.enchantments.EnchantmentManager;
import io.github.mrriptide.peakcraft.recipes.CustomItemStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEnchant implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player enchantPlayer = Bukkit.getPlayer(args[0]);
        if (enchantPlayer == null){
            return false;
        }

        if (args.length != 3){
            sender.sendMessage("Please provide 3 arguments");
            return false;
        }

        String enchantName = args[1];

        if (!EnchantmentManager.validateEnchantment(enchantName)){
            sender.sendMessage("Invalid enchant name");
            return false;
        }

        int enchantLevel = Integer.parseInt(args[2]);

        if (enchantLevel < 0){
            sender.sendMessage("Cannot provide an enchantment level below 0");
            return false;
        } else{
            try{
                CustomItemStack itemStack = new CustomItemStack(enchantPlayer.getInventory().getItemInMainHand());
                try {
                    if (enchantLevel == 0){
                        sender.sendMessage("Removed enchantment");
                        itemStack.removeEnchantment(enchantName);
                    } else {
                        itemStack.addEnchantment(enchantName, enchantLevel);
                    }
                    enchantPlayer.getInventory().setItemInMainHand(itemStack);
                    return true;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("This item cannot be enchanted!");
                    return false;
                }
            } catch (ItemException e) {
                sender.sendMessage("This item is unregistered");
                return false;
            }
        }
    }
}
