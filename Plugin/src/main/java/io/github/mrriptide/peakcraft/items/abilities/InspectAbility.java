package io.github.mrriptide.peakcraft.items.abilities;

import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.actions.RightClickAction;
import io.github.mrriptide.peakcraft.entity.EntityManager;
import io.github.mrriptide.peakcraft.entity.player.PlayerWrapper;
import io.github.mrriptide.peakcraft.entity.wrappers.LivingEntityWrapper;
import io.github.mrriptide.peakcraft.exceptions.EntityException;
import io.github.mrriptide.peakcraft.items.abilities.triggers.AbilityTrigger;
import io.github.mrriptide.peakcraft.items.abilities.triggers.RightClickAbilityTrigger;
import io.github.mrriptide.peakcraft.util.CustomColors;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;

public class InspectAbility extends Ability {
    public InspectAbility() {
        super("inspect_ability",
                AbilityType.RIGHT_CLICK,
                "Inspect",
                "Inspects a selected block or entity",
                0,
                0);
    }

    @Override
    public boolean canUseAbility(PlayerWrapper player, AbilityTrigger trigger){
        if (!(trigger instanceof RightClickAbilityTrigger)){
            return false;
        }
        if (((RightClickAbilityTrigger)trigger).hasEntity()){
            // send entity info
            return true;
        } else if (((RightClickAbilityTrigger)trigger).hasAction()){
            if (((RightClickAbilityTrigger)trigger).getAction() == Action.RIGHT_CLICK_AIR){
                player.getSource().sendMessage(CustomColors.ERROR + "Select a block or entity to use this");
                return false;
            } else if (((RightClickAbilityTrigger)trigger).getAction() == Action.RIGHT_CLICK_BLOCK){
                // send block info
                return true;
            }
        }
        return super.canUseAbility(player, trigger);
    }

    @Override
    public void useAbility(PlayerWrapper player, AbilityTrigger trigger) {
        if (((RightClickAbilityTrigger)trigger).hasEntity()){
            // send entity info
            if (EntityManager.isCustomMob(((RightClickAbilityTrigger) trigger).getEntity())){
                LivingEntityWrapper entity = null;
                try {
                    entity = new LivingEntityWrapper((LivingEntity) ((RightClickAbilityTrigger) trigger).getEntity());
                } catch (EntityException e) {
                    PeakCraft.getPlugin().getLogger().warning("An entity was recognized as a custom mob but something failed in wrapping! Please report this to the developers");
                    e.printStackTrace();
                    return;
                }
                var entityData = entity.getData();
                StringBuilder message = new StringBuilder();
                message.append("\nEntity Data:");
                for (String line : entityData){
                    message.append(CustomColors.DEFAULT_CHAT + "\n" + line);
                }
                player.getSource().sendMessage(message.toString());
            } else {
                player.getSource().sendMessage(CustomColors.ERROR + "Not a custom entity");
            }
        } else if (((RightClickAbilityTrigger)trigger).hasBlock()){
            if (((RightClickAbilityTrigger)trigger).getAction() == Action.RIGHT_CLICK_BLOCK){
                // send block info
                player.getSource().sendMessage(((RightClickAbilityTrigger)trigger).getBlock().getBlockData().getAsString());
            }
        }
    }

    @Override
    public PriorityLevel getListeningLevel() {
        return PriorityLevel.MIDDLE;
    }

    @Override
    public boolean listensTo(io.github.mrriptide.peakcraft.actions.Action action) {
        return action instanceof RightClickAction;
    }

    @Override
    public void onAction(io.github.mrriptide.peakcraft.actions.Action action) {

    }
}
