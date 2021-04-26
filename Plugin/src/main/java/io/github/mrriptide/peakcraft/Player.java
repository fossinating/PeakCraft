package io.github.mrriptide.peakcraft;

import io.github.mrriptide.peakcraft.items.Item;
import io.github.mrriptide.peakcraft.util.PersistentDataManager;
import org.bukkit.persistence.PersistentDataType;

public class Player {
    private double mana;
    private double maxMana;
    private double strength;
    private double health;
    private double maxHealth;
    private double defense;
    private org.bukkit.entity.Player player;

    public Player(org.bukkit.entity.Player player){
        this.player = player;

        this.mana = PersistentDataManager.getValueOrDefault(player, PersistentDataType.DOUBLE, "mana", 100);
        this.health = PersistentDataManager.getValueOrDefault(player, PersistentDataType.DOUBLE, "health", 100);
    }

    public void processAttack(Item weapon, double strength){
        weapon.bakeAttributes();
        int damage = (weapon.getBakedAttribute("damage")!=0) ? weapon.getBakedAttribute("damage") : 10;
        this.health -= damage*(1+0.05*strength)/(1+defense*0.05);
    }

    public void updatePlayer(){
        PersistentDataManager.setValue(this.player, PersistentDataType.DOUBLE, "mana", this.mana);
        PersistentDataManager.setValue(this.player, PersistentDataType.DOUBLE, "health", this.health);
    }
}