/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly.crafting;

import com.googlemail.mcdjuady.craftutils.validators.IngredientValidator;
import com.googlemail.mcdjuady.enderfly.EnderFly;
import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class EnderFlyValidator implements IngredientValidator{
    
    @Override
    public boolean validate(ItemStack ingredient) {
        if (!ingredient.hasItemMeta())
            return false;
        List<String> lore = ingredient.getItemMeta().getLore();
        if (lore == null || lore.isEmpty() || lore.size() < 3)
            return false;
        String info = Util.unhideString(lore.get(2));
        return info.matches(EnderFly.ENDERFLY_REGEX);
    }
    
    
    
}
