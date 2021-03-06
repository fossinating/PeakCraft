package io.github.mrriptide.peakcraft.listeners;

import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.actions.RightClickAction;
import io.github.mrriptide.peakcraft.entity.player.PlayerManager;
import io.github.mrriptide.peakcraft.entity.player.PlayerWrapper;
import io.github.mrriptide.peakcraft.exceptions.ItemException;
import io.github.mrriptide.peakcraft.guis.EnchantingGUI;
import io.github.mrriptide.peakcraft.items.ArmorItem;
import io.github.mrriptide.peakcraft.items.Item;
import io.github.mrriptide.peakcraft.recipes.CustomItemStack;
import io.github.mrriptide.peakcraft.runnables.UpdatePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class PlayerListener implements Listener {
    private HashMap<UUID, Long> lastInteractTime = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        PlayerManager.logInPlayer(e.getPlayer());
        BukkitTask task = new UpdatePlayer(e.getPlayer()).runTaskTimer(PeakCraft.instance, 0, UpdatePlayer.ticksPerUpdate);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        PlayerManager.logOutPlayer(e.getPlayer());
    }

    private boolean isOffCooldown(PlayerEvent e) {
        if (System.nanoTime() - lastInteractTime.getOrDefault(e.getPlayer().getUniqueId(), (long) 0) < 50000000){
            ((e instanceof PlayerInteractEntityEvent) ? (PlayerInteractEntityEvent)e : (PlayerInteractEvent)e).setCancelled(true);
            PeakCraft.getPlugin().getLogger().info("Skipping event " + e.getEventName());
            return false;
        } else {
            lastInteractTime.put(e.getPlayer().getUniqueId(), System.nanoTime());
            PeakCraft.getPlugin().getLogger().info("Not skipping event " + e.getEventName());
            return true;
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        PlayerWrapper playerWrapper = PlayerManager.getPlayer(e.getPlayer());
        playerWrapper.resetStats();
        playerWrapper.setDead(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE){
            (e.getPlayer()).openInventory((new EnchantingGUI()).getInventory());
            e.setCancelled(true);
            return;
        }
        if (e.getItem() != null){
            try{
                Item item = new CustomItemStack(e.getItem()).getItem();
                if (item.hasAbility()){
                    PlayerWrapper player = PlayerManager.getPlayer(e.getPlayer());
                    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                        RightClickAction action = new RightClickAction(player);
                        action.runAction();
                    }
                    e.setCancelled(false);
                }

                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if (item instanceof ArmorItem){
                        ArrayList<String> types = new ArrayList<>();
                        types.add("helmet");
                        types.add("chestplate");
                        types.add("leggings");
                        types.add("boots");

                        int slot = types.indexOf(item.getType().toLowerCase(Locale.ROOT));

                        ItemStack[] armorContents = e.getPlayer().getInventory().getArmorContents();
                        if (slot >= 0 && armorContents[3-slot] == null){
                            armorContents[3-slot] = e.getItem();
                            e.getPlayer().getInventory().setArmorContents(armorContents);
                            if (e.getHand() == EquipmentSlot.HAND){
                                e.getPlayer().getInventory().setItemInMainHand(null);
                            } else if (e.getHand() == EquipmentSlot.OFF_HAND){
                                e.getPlayer().getInventory().setItemInOffHand(null);
                            }
                        } else {
                            for (int i = 0; i < 4; i++){
                                if (armorContents[i] == null){
                                    armorContents[3-i] = e.getItem();
                                    e.getPlayer().getInventory().setArmorContents(armorContents);
                                    if (e.getHand() == EquipmentSlot.HAND){
                                        e.getPlayer().getInventory().setItemInMainHand(null);
                                    } else if (e.getHand() == EquipmentSlot.OFF_HAND){
                                        e.getPlayer().getInventory().setItemInOffHand(null);
                                    }
                                    break;
                                }
                            }
                        }
                        e.setCancelled(true);
                    }
                }
            } catch (ItemException error){
                e.getPlayer().sendMessage("That item has an invalid id, please report this!");
                PeakCraft.getPlugin().getLogger().warning("Player " + e.getPlayer().getName() + " interacted with an invalid item!");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e){
        if (e.getPlayer().getEquipment() != null){
            try{
                Item item = new CustomItemStack(e.getPlayer().getEquipment().getItem(e.getHand())).getItem();

                if (item.hasAbility()) {
                    PlayerWrapper player = PlayerManager.getPlayer(e.getPlayer());
                    RightClickAction action = new RightClickAction(player);
                    action.runAction();
                    e.setCancelled(true);
                }
            } catch (ItemException error){
                e.getPlayer().sendMessage("That item has an invalid id, please report this!");
                PeakCraft.getPlugin().getLogger().warning("Player " + e.getPlayer().getName() + " interacted with an invalid item!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        PlayerWrapper player = PlayerManager.getPlayer(e.getEntity().getPlayer());
        if (player.isDead()){
            e.setDeathMessage("");
        } else {
            String message;
            EntityDamageEvent lastCause = e.getEntity().getLastDamageCause();
            if (lastCause != null) {
                switch (e.getEntity().getLastDamageCause().getCause()){
                    case FALL -> message = "<player> fell from a high place";
                    case PROJECTILE -> message = "<player> was shot by <killer>";
                    case BLOCK_EXPLOSION -> message = "<player> blew up";
                }
            }
            player.setDead(true);
        }
    }

    @EventHandler
    public void onArmorEquip(InventoryClickEvent e){
        //if (e.getSlotType() == InventoryType.SlotType.ARMOR)
    }
}
