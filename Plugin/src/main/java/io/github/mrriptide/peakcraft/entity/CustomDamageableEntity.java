package io.github.mrriptide.peakcraft.entity;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.ChatColor;

public abstract class CustomDamageableEntity extends CustomEntity {
    protected double health;
    protected double maxHealth;
    protected double defense;

    protected CustomDamageableEntity(EntityTypes<? extends EntityCreature> type, World world) {
        super(type, world);
        updateName();
    }

    @Override
    public void updateName(){
        this.setCustomName(new ChatComponentText(name + " " + ChatColor.WHITE + ((int)health) + ChatColor.DARK_RED + " ❤"));
        this.setCustomNameVisible(true);
    }

    public void setMaxHealth(double maxHealth){
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        updateName();
    }
}