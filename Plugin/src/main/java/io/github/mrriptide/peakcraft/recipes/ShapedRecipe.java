package io.github.mrriptide.peakcraft.recipes;

import io.github.mrriptide.peakcraft.exceptions.ItemException;
import net.minecraft.core.NonNullList;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNamespacedKey;

import java.util.Arrays;
import java.util.HashMap;

public class ShapedRecipe extends Recipe {
    private HashMap<Character, RecipeItem> ingredientMap;
    private String[] shape;

    public ShapedRecipe(){
        ingredientMap = new HashMap<>();
        this.setResult(null);
        this.setGroup("");
    }

    public ShapedRecipe(org.bukkit.inventory.ShapedRecipe recipeSource){
        try {
            this.setResult(new RecipeItem(recipeSource.getResult()));
        } catch (ItemException e) {
            e.printStackTrace();
        }
        this.setGroup(recipeSource.getGroup());
        this.shape = recipeSource.getShape();
        this.setKey(recipeSource.getKey());

        this.ingredientMap = new HashMap<>();
        if (shape != null){
            for (Character key : recipeSource.getIngredientMap().keySet()){
                if (recipeSource.getIngredientMap().get(key) != null){
                    try {
                        ingredientMap.put(key, new RecipeItem(recipeSource.getIngredientMap().get(key)));
                    } catch (ItemException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (int i = 0; i < shape.length; i++){
                        shape[i] = shape[i].replace(key, ' ');
                    }
                }
            }
        }
    }

    public ShapedRecipe(RecipeItem[][] ingredients, RecipeItem result){
        /*this.setResult(result);
        this.setGroup("");

        ArrayList<Integer> valid_cols = new ArrayList<>();
        ArrayList<Integer> valid_rows = new ArrayList<>();

        // loop through two dimensions
        for (int i = 0; i <= 2; i++){
            boolean row_contains_item = false;
            boolean col_contains_item = false;
            for (int j = 0; j <= 2; j++){
                // checks if column is empty
                if (ingredients[j][i].getItemStack().getType() != Material.AIR){
                    col_contains_item = true;
                }
                // checks if row is empty
                if (ingredients[i][j].getItemStack().getType() != Material.AIR){
                    row_contains_item = true;
                }
            }
            if (row_contains_item){
                valid_rows.add(i);
            }
            if (col_contains_item){
                valid_cols.add(i);
            }
        }

        // build the shape
        shape = new String[valid_rows.size()];

        // hashmap of the ingredients
        HashMap<RecipeItem, Character> reversedIngredientMap = new HashMap<>();
        Character[] ingredient_chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
        int i = 0;
        // go through only the valid rows and columns
        for (int row : valid_rows){
            shape[i] = "";
            for (int col : valid_cols){
                RecipeItem ingredient = ingredients[row][col];
                // if it is air, leave it blank in the recipe
                if (ingredient.getItemStack().getType() == Material.AIR){
                    shape[i] += " ";
                    continue;
                }
                // if it isnt already a defined ingredient, define it in the hashmap
                if (!reversedIngredientMap.containsKey(ingredient)) {
                    reversedIngredientMap.put(ingredient, ingredient_chars[reversedIngredientMap.size()]);
                }

                // use the hashmap to use consistent keys
                shape[i] += reversedIngredientMap.get(ingredient);
            }
            i++;
        }

        ingredientMap = new HashMap<>();
        for (RecipeItem ingredient : reversedIngredientMap.keySet()){
            ingredientMap.put(reversedIngredientMap.get(ingredient), ingredient);
        }*/
    }

    public HashMap<Character, RecipeItem> getIngredients(){
        return ingredientMap;
    }

    public String[] getShape(){return shape;}

    public void setShape(String[] shape){
        this.shape = shape;
    }

    /**
     *  Returns a boolean based on if the generic recipe matches the specific recipe passed in
     *
     * @TODO: OreDict feature for things like logs or stone
     *
     * @param   recipe  the crafted recipe to compare to
     * @return          if the generic recipe matches the specific recipe
     */
    @Override
    public boolean test(Recipe recipe){
        ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;

        if (this == recipe || this.equals(recipe)){
            return true;
        }

        if (Arrays.equals(this.shape, shapedRecipe.shape)){
            if (this.ingredientMap.equals(shapedRecipe.ingredientMap)){
                return true;
            } else {
                for (Character key : this.ingredientMap.keySet()){
                    RecipeItem recipeItem = this.ingredientMap.get(key);
                    RecipeItem craftItem = shapedRecipe.ingredientMap.get(key);
                    if (!(craftItem.getCount() >= recipeItem.getCount() && (craftItem.getId().equals(recipeItem.getId())
                            || (!recipeItem.getOreDict().equals("") && recipeItem.getOreDict().equals(craftItem.getOreDict()))))){
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    public net.minecraft.world.item.crafting.Recipe<?> toNMS(String recipeName){
        if (getKey() == null){
            setKey(recipeName);
        }

        //https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftShapedRecipe.java#55
        //https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventoryCrafting.java#7,11
        //https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/inventory/RecipeChoice.java#16,66,169
        int width = shape[0].length();
        int height = shape.length;
        NonNullList<RecipeItem> ingredients = NonNullList.withSize(height * width, new RecipeItem());

        for (int i = 0; i < shape.length * shape[0].length(); i++){
            if (ingredientMap.get(shape[i / width].charAt(i % width)) != null)
            ingredients.set(i, ingredientMap.get(shape[i / width].charAt(i % width)));
        }
        return new ShapedNMSRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), getGroup(), getShape()[0].length(), getShape().length, ingredients, getResultRecipeItem());
    }
}
