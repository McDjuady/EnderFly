/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly.crafting;

import com.googlemail.mcdjuady.craftutils.validators.ShapedResultBuilder;
import com.googlemail.mcdjuady.enderfly.EnderFly;
import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class EnderFlyUpgradeResultBuilder implements ShapedResultBuilder {
    
    ItemStack resultEnderFly;
    
    public EnderFlyUpgradeResultBuilder(ItemStack result) {
        resultEnderFly = result;
    }
    
    @Override
    public int[] generateCostMatrix(ItemStack[] matrix) {
        int[] cost = new int[matrix.length];
        Arrays.fill(cost, 1);
        return cost;
    }

    @Override
    public ItemStack getResult(ItemStack[] matrix) {
        if (matrix == null || matrix.length != 10 || matrix[4] == null || !matrix[4].hasItemMeta()) {
            return null;
        }
        ItemStack result = resultEnderFly.clone();
        List<String> lore = matrix[4].getItemMeta().getLore();
        if (lore == null || lore.isEmpty() || lore.size() != 3) {
            return null;
        }
        String info = Util.unhideString(lore.remove(2));
        if (!info.matches(EnderFly.ENDERFLY_REGEX)) {
            return null;
        }
        info = info.replace("["+EnderFly.ENDERFLY_PREFIX+"]", "");
        String[] numbers = info.split("-");
        info = String.format(EnderFly.ENDERFLY_STRING, 0, numbers[1], 0); //copy over the time left
        lore.add(Util.hideString(info));
        result.getItemMeta().setLore(lore);
        result.addEnchantments(matrix[4].getEnchantments()); //copy the enchantments
        return result;
    }
    
}
