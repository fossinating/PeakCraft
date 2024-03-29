package io.github.mrriptide.peakcraft.entity.player;

import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.entity.wrappers.CombatEntityWrapper;
import io.github.mrriptide.peakcraft.exceptions.EntityException;
import io.github.mrriptide.peakcraft.exceptions.ItemException;
import io.github.mrriptide.peakcraft.items.ArmorItem;
import io.github.mrriptide.peakcraft.items.EnchantableItem;
import io.github.mrriptide.peakcraft.items.Item;
import io.github.mrriptide.peakcraft.items.ItemManager;
import io.github.mrriptide.peakcraft.items.fullsetbonus.FullSetBonus;
import io.github.mrriptide.peakcraft.items.fullsetbonus.FullSetBonusManager;
import io.github.mrriptide.peakcraft.recipes.CustomItemStack;
import io.github.mrriptide.peakcraft.runnables.UpdatePlayer;
import io.github.mrriptide.peakcraft.util.Attribute;
import io.github.mrriptide.peakcraft.util.CustomColors;
import io.github.mrriptide.peakcraft.util.MySQLHelper;
import io.github.mrriptide.peakcraft.util.PersistentDataManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.A;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class PlayerWrapper extends CombatEntityWrapper {
    protected double mana;
    protected Attribute maxMana;
    protected Attribute critChance;
    protected Attribute critDamage;
    protected long lastDamageTime;
    protected double hunger;
    protected final double maxHunger = 500;
    protected long coins;
    protected PlayerStatus status;
    private boolean isDead;

    public PlayerWrapper(Player player) throws EntityException {
        super();
        this.entity = player;
        this.id = "player";
        this.maxHealth = new Attribute(100);
        this.defense = new Attribute(0);
        this.strength = new Attribute(0);

        this.health = Math.min(PersistentDataManager.getValueOrDefault(player, PersistentDataType.DOUBLE, "health", maxHealth.getFinal()), maxHealth.getFinal());
        this.mana = PersistentDataManager.getValueOrDefault(player, PersistentDataType.DOUBLE, "mana", 0.0);
        this.hunger = PersistentDataManager.getValueOrDefault(player, PersistentDataType.DOUBLE, "hunger", 0.0);
        this.maxMana = PersistentDataManager.getAttribute(player, "maxMana", 100.0);
        this.lastDamageTime = PersistentDataManager.getValueOrDefault(player, PersistentDataType.LONG, "lastDamageTime", Long.valueOf(0));
        this.critChance = PersistentDataManager.getAttribute(player, "critChance", 0.5);
        this.critDamage = PersistentDataManager.getAttribute(player, "critDamage", 0.5);
        this.name = player.getName();
        this.isDead = health == 0;

        this.status = new PlayerStatus(player);

        try (Connection conn = MySQLHelper.getConnection()){
            PreparedStatement statement = conn.prepareStatement("select coins from player_coins where uuid = ?;");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                coins = resultSet.getLong("coins");
            } else {
                coins = 0;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException throwables) {
            PeakCraft.getPlugin().getLogger().warning("Failed to connect to the mysql database");
            throwables.printStackTrace();
        }

    }

    @Override
    public void resetAttributes(){
        super.resetAttributes();
        this.maxMana.reset();
        this.critChance.reset();
        this.critDamage.reset();
    }

    @Override
    public void processItem(EnchantableItem item){
        super.processItem(item);
        this.maxMana.addAdditive(item.getAttribute("intelligence").getFinal());
    }

    public void damage(double amount){
        lastDamageTime = (new Date()).getTime();
        super.damage(amount);
    }

    public void tryNaturalRegen(){
        if ((new Date()).getTime() - lastDamageTime > 5000){
            regenHealth(maxHealth.getFinal() / 100 * 5 / (20.0/UpdatePlayer.ticksPerUpdate));
        }
    }

    public void regenHealth(double amount){
        this.hunger -= Math.min(amount, maxHealth.getFinal()-health);

        super.regenHealth(amount);
    }

    public ArrayList<Item> getAbilityItems(){
        ArrayList<Item> abilityItems = new ArrayList<>();
        for (ItemStack itemStack : ((Player)entity).getInventory()){
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)){
                try{
                    Item item = new CustomItemStack(itemStack).getItem();
                    if (item.hasAbility()){
                        abilityItems.add(item);
                    }
                } catch (ItemException e) {
                    PeakCraft.getPlugin().getLogger().warning("Player " + entity.getName() + " has an invalid item in their inventory!");
                }
            }
        }

        return abilityItems;
    }

    @Override
    public void updateEntity(){
        super.updateEntity();

        ((org.bukkit.entity.Player)entity).setSaturation((float) Math.min(this.hunger/this.maxHunger*20.0, 20.0));
        PersistentDataManager.setValue(entity, "mana", this.mana);
        PersistentDataManager.setValue(entity, "hunger", this.mana);
        PersistentDataManager.setAttribute(entity, "critChance", this.critChance);
        PersistentDataManager.setAttribute(entity, "critDamage", this.critDamage);
        PersistentDataManager.setValue(entity, "lastDamageTime", this.lastDamageTime);
        /*
        temporary removal of using experience bar to show mana, I want to let people have experience racked up now
        ((Player)entity).setLevel((int)mana);
        ((Player)entity).setExp((float) (mana / maxMana));*/
        sendActionBar();
    }

    public void updateFromEntity(){
        try {
            this.weapon = ItemManager.convertItem(((Player)entity).getEquipment().getItem(EquipmentSlot.HAND));
            this.offHand = ItemManager.convertItem(((Player)entity).getEquipment().getItem(EquipmentSlot.OFF_HAND));
        } catch (ItemException e) {
            PeakCraft.getPlugin().getLogger().warning("Player " + entity.getName() + " has an invalid item in their hand");
            e.printStackTrace();
        }
    }

    @Override
    public void processAttack(CombatEntityWrapper attacker){
        // if attacked by an NPC or player, ignore
        if (!(attacker instanceof PlayerWrapper)){
            super.processAttack(attacker);
        }
    }

    public void resetStats(){
        this.health = maxHealth.getFinal();
        this.mana = maxMana.getFinal();
        updateEntity();
    }

    public void sendActionBar(){
        ArrayList<String> actionBarInfo = new ArrayList<>();

        actionBarInfo.add("" + CustomColors.HEALTH + "" + (int)this.health + "/" + (int)this.maxHealth.getFinal() + "❤");
        //actionBarInfo.add("" + CustomColors.MANA + (int)mana +  "/" + (int)maxMana.getFinal() + "MP");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < actionBarInfo.size(); i++){
            builder.append(actionBarInfo.get(i));
            if (i < actionBarInfo.size() - 1){
                builder.append("            ");
            }
        }

        ((Player)this.entity).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(builder.toString()));
    }

    public void regenMana(){
        mana = Math.min(mana + maxMana.getFinal()*0.05/(20.0/UpdatePlayer.ticksPerUpdate), maxMana.getFinal());
        updateEntity();
    }

    public boolean reduceMana(int amount){
        if (((Player)entity).getGameMode() == GameMode.CREATIVE){
            return true;
        }
        if (mana >= amount){
            mana -= amount;
            updateEntity();
            return true;
        } else {
            return false;
        }
    }

    public double getMana(){
        return mana;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public boolean hasFullSet(){
        ItemStack[] armorContents = ((Player)entity).getInventory().getArmorContents();
        for (int i = 0; i < 3; i++){
            try{
                if (!((ArmorItem)new CustomItemStack(armorContents[i]).getItem()).getSet().equals(
                        ((ArmorItem)new CustomItemStack(armorContents[i+1]).getItem()).getSet())){
                    return false;
                }
            } catch (ItemException e){
                PeakCraft.getPlugin().getLogger().warning("Player " + entity.getName() + " has an invalid armor item equipped!");
            }
        }
        return true;
    }

    public FullSetBonus getFullSet(){
        ItemStack[] armorContents = ((Player)entity).getInventory().getArmorContents();
        if (armorContents[0] == null){
            return null;
        }
        try{
            String setName = ((ArmorItem)new CustomItemStack(armorContents[0]).getItem()).getSetName();
            for (int i = 1; i < 4; i++){
                try{
                    if (armorContents[i] == null || !setName.equals(((ArmorItem)new CustomItemStack(armorContents[i]).getItem()).getSetName())){
                        return null;
                    }
                } catch (ItemException e){
                    PeakCraft.getPlugin().getLogger().warning("Player " + entity.getName() + " has an invalid armor item equipped!");
                    return null;
                }
            }
            if (FullSetBonusManager.validSet(setName)){
                return FullSetBonusManager.getSet(setName);
            } else {
                return null;
            }
        } catch (ItemException e){
            PeakCraft.getPlugin().getLogger().warning("Player " + entity.getName() + " has an invalid armor item equipped!");
            return null;
        }
    }

    @Override
    public Entity getEntity() {
        return Bukkit.getPlayer(this.entity.getUniqueId());
    }

    public Attribute getCritChance() {
        return critChance;
    }

    public Attribute getCritDamage() {
        return critDamage;
    }

    public Player getSource() {
        return ((Player)entity);
    }

    public long getCoins() {
        return coins;
    }


    public void giveItem(Item item){
        ((Player)entity).getInventory().addItem(new CustomItemStack(item));
    }

    public void giveItem(String name) throws ItemException {
        giveItem(ItemManager.getItem(name));
    }

    public boolean isDead(){
        return isDead;
    }

    public void setDead(boolean isDead){
        this.isDead = isDead;
    }

    public class PlayerStatus{

        private boolean flightAllowed;
        private boolean flying;
        private Attribute speed;
        private boolean blocking;

        public PlayerStatus(Player player){
            flightAllowed = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
            flying = player.isFlying();
            speed = PersistentDataManager.getAttribute(player, "speed", 200);
            blocking = player.isBlocking();
        }

        public void init(){
            flightAllowed = ((Player)entity).getGameMode() == GameMode.CREATIVE || ((Player)entity).getGameMode() == GameMode.SPECTATOR;
            flying = ((Player)entity).isFlying();
            speed.reset();
            blocking = ((Player)entity).isBlocking();
        }

        public void apply(){
            ((Player)entity).setAllowFlight(flightAllowed);
            ((Player)entity).setFlying(flightAllowed && flying);
            PersistentDataManager.setAttribute(entity, "speed", speed);
            ((Player)entity).setWalkSpeed((float) Math.min(1f, speed.getFinal()/1000));
        }

        public void setFlightAllowed(boolean flightAllowed){
            this.flightAllowed = ((Player)entity).getGameMode() == GameMode.CREATIVE || ((Player)entity).getGameMode() == GameMode.SPECTATOR || flightAllowed;
        }

        public void setFlying(boolean flying){
            this.flying = flying;
        }
        public boolean isFlightAllowed() {
            return flightAllowed;
        }

        public boolean isFlying() {
            return flying;
        }

        public void setBlocking(boolean blocking){
            this.blocking = blocking;
        }

        public boolean isBlocking() {
            return blocking;
        }

        public Attribute getSpeed() {
            return speed;
        }
    }
}
