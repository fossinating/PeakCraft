package io.github.mrriptide.peakcraft.recipes;

import io.github.mrriptide.peakcraft.PeakCraft;
import io.github.mrriptide.peakcraft.exceptions.ItemException;
import io.github.mrriptide.peakcraft.items.Item;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class RecipeItem {
    private String id;
    private String oreDict;
    private int count;

    public RecipeItem(){
        this.id = "air";
        this.oreDict = "";
        this.count = 0;
    }

    public RecipeItem(String id, int count) throws ItemException {
        this.id = id;
        this.oreDict = (new Item(id)).getOreDict();
        this.count = count;
    }

    public RecipeItem(String id, int count, boolean useOreDict) throws ItemException {
        this.id = id;
        if (useOreDict){
            this.oreDict = (new Item(id)).getOreDict();
        } else {
            this.oreDict = "";
        }
        this.count = count;
    }

    public RecipeItem(String id) throws ItemException {
        this.id = id;
        this.oreDict = (new Item(id)).getOreDict();
        this.count = 0;
    }

    public RecipeItem(ItemStack itemStack) throws ItemException {
        // Default option
        this.id = itemStack.getType().name();

        // If a PeakCraft-specific id is defined, use that instead
        NamespacedKey itemIDKey = new NamespacedKey(PeakCraft.getPlugin(), "ITEM_ID");
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null){
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(itemIDKey, PersistentDataType.STRING)){
                this.id = container.get(itemIDKey, PersistentDataType.STRING);
            }
        }

        this.oreDict = (new Item(id)).getOreDict();

        this.count = itemStack.getAmount();
    }

    public String getId(){
        return id;
    }

    public String getOreDict() {
        return (oreDict == null) ? "" : oreDict;
    }

    public int getCount() {
        return count;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setCount(int count){
        this.count = count;
    }

    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }

        if (!(o instanceof RecipeItem)){
            return false;
        }

        RecipeItem item = (RecipeItem)o;
        return this.getId().equals(item.getId()) && this.getCount() == item.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, count);
    }

    /*@JsonIgnore
    public ItemStack getItemStack(){
        Item item = new Item(id);
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(count);

        return itemStack;
    }*/

    public boolean test(RecipeItem item){
        return item != null && this.getId().equals(item.getId()) && item.getCount() >= this.getCount();
    }

    /*
    *
    * Copying code from Ingredient
    *
    * */

    /*public void toNetwork(FriendlyByteBuf packetdataserializer) {
        try {
            packetdataserializer.writeCollection(Arrays.asList(CraftItemStack.asNMSCopy(ItemManager.getItem(id).getItemStack())), FriendlyByteBuf::writeItem);
        } catch (ItemException e) {
            e.printStackTrace();
        }
    }*/
}
