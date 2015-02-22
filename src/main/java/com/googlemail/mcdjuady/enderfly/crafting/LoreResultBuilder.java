/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly.crafting;

import com.googlemail.mcdjuady.craftutils.validators.ShapedResultBuilder;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class LoreResultBuilder implements ShapedResultBuilder{
    
    private final ItemStack result;
    
    public LoreResultBuilder(ItemStack result) {
        this.result = result.clone();
    }
    
    @Override
    public int[] generateCostMatrix(ItemStack[] matrix) {
        int[] costs = new int[matrix.length];
        Arrays.fill(costs, 0);
        for (int i=0;i<matrix.length;i++) {
            if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                costs[i] = 1;
            }
        }
        return costs;
    }

    @Override
    public ItemStack getResult(ItemStack[] matrix) {
        ItemStack resultStack = result.clone();
        resultStack.addEnchantments(matrix[4].getEnchantments());
        return resultStack;
    }
    
}
