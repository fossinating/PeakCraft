package io.github.mrriptide.peakcraft.items;

import com.google.common.collect.Sets;
import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.items.enchantments.EnchantmentManager;
import io.github.mrriptide.peakcraft.util.PersistentDataManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.Serializable;
import java.util.*;

public class Item implements Serializable {
    protected String id;
    private String oreDict;
    private String displayName;
    private int rarity;
    private String description;
    private Material material;
    protected String type;

    public Item(){

    }

    public Item(String id, String oreDict, String displayName, int rarity, String description, Material material, String type){
        this.id = id;
        this.oreDict = oreDict;
        this.displayName = displayName;
        this.rarity = rarity;
        this.description = description;
        this.material = material;
        this.type = (type != null && !type.isEmpty()) ? type : "item";
    }

    public Item(String id){
        Item item = ItemManager.getItem(id);

        assert item != null;
        this.id = item.id;
        this.oreDict = item.oreDict;
        this.displayName = item.displayName;
        this.rarity = item.rarity;
        this.description = item.description;
        this.material = item.material;
        this.type = item.type;
    }

    public Item(Item item){this.id = item.id;
        this.oreDict = item.oreDict;
        this.displayName = item.displayName;
        this.rarity = item.rarity;
        this.description = item.description;
        this.material = item.material;
        this.type = item.type;
    }

    public Item(ItemStack itemSource){
        // Get ID of the item from the ItemStack

        // Default option
        this.id = PersistentDataManager.getValueOrDefault(itemSource, PersistentDataType.STRING, "ITEM_ID", itemSource.getType().name());

        assert this.id != null;
        Item default_item = ItemManager.getItem(this.id);

        this.oreDict = default_item.oreDict;
        this.rarity = default_item.rarity;
        this.displayName = default_item.displayName;
        this.description = default_item.description;
        this.type = default_item.type;
        this.material = default_item.material;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public ItemStack getItemStack(){
        ItemStack item = new ItemStack(material);

        //PeakCraft.getPlugin().getLogger().info("Item: " + item);

        ItemMeta meta = item.getItemMeta();
        if (meta == null){
            return item;
        }

        // Add the lore to the item
        meta.setLore(getLore());

        // Set the custom name
        meta.setDisplayName(getRarityColor() + displayName);

        // Hide things
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        // put metadata on item

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack convertItem(ItemStack item){

        ItemMeta meta = item.getItemMeta();

        // Add the lore to the item
        assert meta != null;
        meta.setLore(getLore());

        // Set the custom name
        meta.setDisplayName(getFormattedDisplayName());

        item.setItemMeta(meta);

        return item;
    }

    public String getFormattedDisplayName(){
        return getRarityColor() + displayName;
    }

    public ArrayList<String> getLore(){
        ArrayList<String> lore = new ArrayList<>();

        if (this instanceof EnchantableItem){
            // Attributes of item

            HashMap<String, ChatColor> attributeColor = new HashMap<>();
            attributeColor.put("damage", ChatColor.DARK_RED);
            attributeColor.put("defense", ChatColor.GREEN);
            attributeColor.put("health", ChatColor.RED);
            if (((EnchantableItem)this).attributes.size() > 0){
                for (String attribute : ((EnchantableItem)this).attributes.keySet()){
                    lore.add(attributeColor.getOrDefault(attribute, ChatColor.DARK_PURPLE) + "" + ChatColor.BOLD + WordUtils.capitalizeFully(attribute) + ChatColor.RESET + ChatColor.WHITE + ": " + ((EnchantableItem)this).getAttribute(attribute));
                }

                lore.add("");
            }

            // Enchantments of item

            if (((EnchantableItem)this).enchantments.size() > 0){
                for (Map.Entry<String, Integer> enchantment : ((EnchantableItem)this).enchantments.entrySet()){
                    lore.add(ChatColor.LIGHT_PURPLE +
                            ((EnchantmentManager.validateEnchantment(enchantment.getKey()) ?
                                    EnchantmentManager.getEnchantment(enchantment.getKey()).getDisplayName() :
                                    "Unknown Enchantment")
                                    + " " + enchantment.getValue()));
                }

                lore.add("");
            }
        }

        // Description of item
        if (description != null && description.length() > 0){
            String[] wrapped_description = WordUtils.wrap(description, 30, "\n", true).split("\n");
            for (String line : wrapped_description){
                lore.add("§7" + line);
            }
            lore.add("");
        }

        // Rarity of item
        lore.add(getRarityColor() + getRarityName().toUpperCase() + " " + type.toUpperCase());

        return lore;
    }

    protected ChatColor getRarityColor(){
        ChatColor[] colors = {
                ChatColor.DARK_RED, // Broken
                ChatColor.GRAY, // Common
                ChatColor.GREEN, // Uncommon
                ChatColor.BLUE, // Rare
                ChatColor.DARK_PURPLE, // Epic
                ChatColor.GOLD, // Legendary
                ChatColor.LIGHT_PURPLE, // Mythic
                ChatColor.AQUA}; // Relic
        return colors[rarity];
    }

    protected String getRarityName() {
        String[] names = {"Broken", "Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic", "Relic"};

        return ChatColor.BOLD + names[rarity];
    }

    public Item clone() {
        Item clonedItem = new Item();

        clonedItem.id = this.id;
        clonedItem.oreDict = this.oreDict;
        clonedItem.displayName = this.displayName;
        clonedItem.rarity = this.rarity;
        clonedItem.description = this.description;
        clonedItem.material = this.material;
        clonedItem.type = this.type;
        return clonedItem;
    }

    public static Item loadFromHashMap(HashMap<String, String> itemData){
        Item item = new Item();
        item.id = itemData.get("id");
        item.oreDict = itemData.get("oreDict");
        item.displayName = itemData.get("displayName");
        item.rarity = Integer.parseInt(itemData.get("rarity"));
        item.description = itemData.get("description");
        item.material = Material.getMaterial(itemData.get("materialID").toUpperCase());
        item.type = itemData.get("type");


        return item;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOreDict() {
        return oreDict;
    }

    public void setOreDict(String oreDict) {
        this.oreDict = oreDict;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getMaterialStr() {
        return material.name();
    }

    public void setMaterialFromStr(String materialName) {
        this.material = Material.getMaterial(materialName);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
