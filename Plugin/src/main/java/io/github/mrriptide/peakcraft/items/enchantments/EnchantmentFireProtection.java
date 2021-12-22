package io.github.mrriptide.peakcraft.items.enchantments;

import io.github.mrriptide.peakcraft.actions.Action;
import io.github.mrriptide.peakcraft.actions.Damage;
import io.github.mrriptide.peakcraft.actions.DamagedAction;
import io.github.mrriptide.peakcraft.items.ArmorItem;
import io.github.mrriptide.peakcraft.items.EnchantableItem;

public class EnchantmentFireProtection extends EnchantmentData {
    public EnchantmentFireProtection() {
        super("fire_protection", "Fire Protection", 5);
    }

    @Override
    public int getCost(int level) {
        return 2 * level;
    }

    @Override
    public boolean listensTo(Action action) {
        return action instanceof DamagedAction;
    }

    @Override
    public boolean validateEnchant(EnchantableItem item) {
        return item instanceof ArmorItem;
    }

    @Override
    public void onAction(Action action, int level) {
        ((DamagedAction) action).getDamage().getProtection(Damage.DamageType.FIRE).addMulti(0.04*level);
    }
}
