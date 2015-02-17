/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
public class EnderFlyTask extends BukkitRunnable {

    private ItemStack item;
    private int max;
    private Player player;

    public EnderFlyTask(Player p) {
        if (!EnderFly.hasEnderFly(p)) {
            return;
        }
        this.player = p;
        item = p.getInventory().getChestplate();
        max = item.getType().getMaxDurability();

    }

    @Override
    public void run() {
        if (player == null) {
            cancel();
            return;
        }
        int durability = Math.max((player.isSprinting() ? 3 : 1) + item.getDurability(), 0); //Sprinting costs 3 times as much as normal flight
        int prevTimeLeft = (max - item.getDurability()) / EnderFly.ONE_SEC;
        int newTimeLeft = (max - durability) / EnderFly.ONE_SEC;
        item.setDurability((short) durability);
        if (prevTimeLeft != newTimeLeft) {
            int[] numbers = EnderFly.getNumbers(item);
            numbers[1] = (max - durability) / EnderFly.ONE_SEC;
            EnderFly.writeLore(item, numbers);
        }
        if (durability >= max) {
            //toggle off
            EnderFly.toggleEnderFly(player);
        } else {
            //update the potion effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, EnderFly.ENDERFLY_MININGMODIFIER, true, false), true);
        }
    }

}
