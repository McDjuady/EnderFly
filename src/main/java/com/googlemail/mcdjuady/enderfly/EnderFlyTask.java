/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
public class EnderFlyTask extends BukkitRunnable {

    private ItemStack item;
    private short ammount;
    private short max;
    private Player player;

    public EnderFlyTask(Player p, ItemStack item, short ammount, short max) {
        this.item = item;
        this.ammount = ammount;
        this.max = max;
        this.player = p;
    }

    @Override
    public void run() {

        //check the durability
        short durability = (short) (ammount + item.getDurability());
        if (durability % EnderFly.ONE_SEC == 0) {
            //we need to update the lore
            List<String> lore = item.getItemMeta().getLore();
            String info = Util.unhideString(lore.remove(2));
            String[] numbers = info.replace("[" + EnderFly.ENDERFLY_PREFIX + "]", "").split("-");
            int armorDurability = Integer.valueOf(numbers[2]);
            int maxTime = item.getType().getMaxDurability() / EnderFly.ONE_SEC;
            int timeLeft = maxTime - (durability / EnderFly.ONE_SEC);
            info = String.format(EnderFly.ENDERFLY_STRING, 1, timeLeft, armorDurability);
            lore.remove(1);
            String visibleLore = String.format(EnderFly.ENDERFLY_TIME, timeLeft + "s", maxTime + "s");
            lore.add(visibleLore);
            lore.add(Util.hideString(info));
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        if (durability >= max) {
            //end the task
            EnderFly.enableEnderFly(item, player, false);
        } else {
            //update the potion effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, EnderFly.ENDERFLY_MININGMODIFIER, true, false), true);
            item.setDurability(durability);
        }
    }

}
