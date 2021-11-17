package io.github.mrriptide.peakcraft.items;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.exceptions.ItemException;
import io.github.mrriptide.peakcraft.recipes.ShapedRecipe;
import io.github.mrriptide.peakcraft.util.MySQLHelper;
import io.github.mrriptide.peakcraft.util.PersistentDataManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.XMLEncoder;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ItemManager {

    private static String itemFilePath = "items.json";
    private static HashMap<String, Item> items;

    public static void getItemFromItemStack(ItemStack itemStack){
        Item item;

        String type = PersistentDataManager.getValueOrDefault(itemStack, PersistentDataType.STRING, "type", "item");
    }

    public static HashMap<String, Item> getItems(){
        return items;
    };

    public static void loadItems() {
        // check if the items table exists

        try {
            if (!MySQLHelper.tableExists("items")){
                Connection conn = MySQLHelper.getConnection();
                PreparedStatement statement = conn.prepareStatement("""CREATE TABLE items (
oredict varchar(255),
)""");
            }

            // load through all spigot items to confirm that they all exist in the database

            // I should load saved items on request time not on start to save on memory, shouldn't I?


        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            HashMap<String, HashMap<String, String>> itemsSource = objectMapper.readValue(file, new TypeReference<HashMap<String, HashMap<String, String>>>(){});

            for (HashMap<String, String> itemData : itemsSource.values()){

                Item item;

                String type = itemData.get("type").toLowerCase(Locale.ROOT);
                if (ArmorItem.validateType(type)) {
                    item = ArmorItem.loadFromHashMap(itemData);
                } else if (WeaponItem.validateType(type)) {
                    item = WeaponItem.loadFromHashMap(itemData);
                } else {
                    item = Item.loadFromHashMap(itemData);
                }

                items.put(item.getId().toUpperCase(), item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        PeakCraft.getPlugin().getLogger().info("Successfully loaded " + items.size() + " items.");
    }

    public static Item getItem(String id) throws ItemException {
        if (!items.containsKey(id.toUpperCase())){
            throw new ItemException("An item was requested that doesn't exist in the item database: \"" + id.toUpperCase() + "\"");
        }
        return items.get(id.toUpperCase()).clone();
    }

    public static Item convertItem(org.bukkit.inventory.ItemStack itemSource) throws ItemException {
        if (itemSource == null || itemSource.getType().equals(Material.AIR)){
            try {
                return getItem("air");
            } catch (ItemException e) {
                e.printStackTrace();
            }
        }
        String id = PersistentDataManager.getValueOrDefault(itemSource, PersistentDataType.STRING, "ITEM_ID", itemSource.getType().name());

        Item item = getItem(id);

        // Move any general attributes to the item from the itemstack
        item.setAmount(itemSource.getAmount());

        if (item instanceof EnchantableItem){

            ((EnchantableItem)item).enchantments = new HashMap<>();
            // register enchants
            for (NamespacedKey key : Objects.requireNonNull(itemSource.getItemMeta()).getPersistentDataContainer().getKeys()){
                if (key.getKey().startsWith("enchant_")){
                    ((EnchantableItem)item).addEnchantment(key.getKey().substring(8, key.getKey().length() - 6), PersistentDataManager.getValueOrDefault(itemSource, PersistentDataType.INTEGER, key.getKey(), 0));
                }
            }

            ((EnchantableItem)item).bakeAttributes();
        }

        return item;
    }

    private static void createItemList() {
        writeMaterialsToFile(Arrays.asList(Material.values()), itemFilePath);
    }

    public static void writeMaterialsToFile(List<Material> materials, String fileName){
        try {
            if (!PeakCraft.instance.getDataFolder().exists()){
                PeakCraft.instance.getDataFolder().mkdirs();
            }

            HashMap<String, HashMap<String, String>> items = new HashMap<>();

            for (Material mat : materials){
                if (mat.isItem()){
                    HashMap<String, String> map = new HashMap<>();
                    map.put("id", mat.name());
                    map.put("oreDict", "");
                    map.put("description", "");
                    map.put("displayName", WordUtils.capitalizeFully(mat.toString().toLowerCase().replace("_", " ")));
                    map.put("rarity", "1");
                    map.put("materialID", mat.name());
                    map.put("type", "Item");

                    items.put(mat.name(), map);
                } else {
                    PeakCraft.getPlugin().getLogger().info(mat.name() + " is not an item");
                }
            }

            // save using jackson https://stackabuse.com/reading-and-writing-json-in-java/

            File recipeFile = new File(PeakCraft.instance.getDataFolder() + File.separator + fileName);

            OutputStream outputStream = new FileOutputStream(recipeFile);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(outputStream, items);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
