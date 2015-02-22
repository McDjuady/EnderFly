/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly.crafting;

import com.googlemail.mcdjuady.craftutils.validators.ShapelessValidator;
import com.googlemail.mcdjuady.enderfly.EnderFly;
import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Max
 */
public class RefillValidator implements ShapelessValidator {

    @Override
    public ItemStack getResult(List<ItemStack> ingredients) {
        ItemStack enderPearl, enderFly;
        if (ingredients.get(0).getType() == Material.ENDER_PEARL) {
            enderPearl = ingredients.get(0);
            enderFly = ingredients.get(1).clone();
        } else {
            enderPearl = ingredients.get(1);
            enderFly = ingredients.get(0).clone();
        }
        List<String> lore = enderFly.getItemMeta().getLore();
        String info = Util.unhideString(lore.get(2));
        String[] numbers = info.replace("[" + EnderFly.ENDERFLY_PREFIX + "]", "").split("-");
        int timeLeft = Integer.valueOf(numbers[1]);
        int actualPearls = getNumPearls(enderFly, enderPearl);
        if (actualPearls == 0) {
            return null;
        }
        timeLeft = Math.min(actualPearls * EnderFly.SEC_PER_PEARL + timeLeft, enderFly.getType().getMaxDurability() / EnderFly.ONE_SEC);
        String time = String.format(EnderFly.ENDERFLY_TIME, timeLeft, enderFly.getType().getMaxDurability() / EnderFly.ONE_SEC);
        info = Util.hideString(String.format(EnderFly.ENDERFLY_STRING, numbers[0], timeLeft, numbers[2]));
        ItemMeta meta = enderFly.getItemMeta();
        lore.remove(2);
        lore.remove(1);
        lore.add(time);
        lore.add(info);
        meta.setLore(lore);
        enderFly.setItemMeta(meta);
        return enderFly;
    }

    private int getNumPearls(ItemStack enderFly, ItemStack enderPearl) {
        List<String> lore = enderFly.getItemMeta().getLore();
        String info = Util.unhideString(lore.get(2));
        String[] numbers = info.replace("[" + EnderFly.ENDERFLY_PREFIX + "]", "").split("-");
        int timeLeft = Integer.valueOf(numbers[1]);
        int timeToFill = enderFly.getType().getMaxDurability() / EnderFly.ONE_SEC - timeLeft;
        if (timeToFill == 0) {
            return 0;
        }
        int maxPearls = timeToFill / EnderFly.SEC_PER_PEARL;
        if (timeToFill % EnderFly.SEC_PER_PEARL != 0) {
            maxPearls += 1;
        }
        return Math.min(maxPearls, enderPearl.getAmount());
    }

    @Override
    public boolean validate(List<ItemStack> ingredients) {
        if (ingredients.size() != 2) {
            return false;
        }
        ItemStack enderPearl, enderFly;
        if (ingredients.get(0).getType() == Material.ENDER_PEARL) {
            enderPearl = ingredients.get(0);
            enderFly = ingredients.get(1).clone();
        } else {
            enderPearl = ingredients.get(1);
            enderFly = ingredients.get(0).clone();
        }
        if (!enderFly.hasItemMeta()) {
            return false;
        }
        List<String> lore = enderFly.getItemMeta().getLore();
        if (lore == null || lore.size() != 3) {
            return false;
        }
        String info = Util.unhideString(lore.get(2));
        if (!info.matches(EnderFly.ENDERFLY_REGEX)) {
            return false;
        }
        return getNumPearls(enderFly, enderPearl) > 0;
    }

    @Override
    public Map<ItemStack, Integer> costMatrix(List<ItemStack> ingredients) {
        ItemStack enderPearl, enderFly;
        if (ingredients.get(0).getType() == Material.ENDER_PEARL) {
            enderPearl = ingredients.get(0);
            enderFly = ingredients.get(1).clone();
        } else {
            enderPearl = ingredients.get(1);
            enderFly = ingredients.get(0).clone();
        }
        int actualPearls = getNumPearls(enderFly, enderPearl);
        Map<ItemStack, Integer> map = new HashMap<>();
        map.put(enderPearl, actualPearls);
        map.put(enderFly, 1);
        return map;
    }

}
