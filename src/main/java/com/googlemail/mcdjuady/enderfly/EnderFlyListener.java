/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max
 */
public class EnderFlyListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        e.getWhoClicked().sendMessage("Click " + e.getClick());
        if (!(e.getWhoClicked() instanceof Player) || e.getCurrentItem() == null) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        ItemStack currentItem = e.getCurrentItem();
        if (player.getGameMode() == GameMode.CREATIVE || currentItem == null || !currentItem.equals(player.getInventory().getChestplate()) || !currentItem.hasItemMeta()) {
            return;
        }
        List<String> lore = currentItem.getItemMeta().getLore();
        if (lore == null || lore.size() != 3) {
            return;
        }
        String info = Util.unhideString(lore.get(2));
        if (!info.matches(EnderFly.ENDERFLY_REGEX)) {
            return;
        }
        switch (e.getClick()) {
            case MIDDLE:
                String[] numbers = info.replace("[" + EnderFly.ENDERFLY_PREFIX + "]", "").split("-");
                e.getWhoClicked().sendMessage(Arrays.toString(numbers));
                EnderFly.enableEnderFly(currentItem, player, Integer.valueOf(numbers[0]) != 1);
                player.updateInventory();
                break;
            default:
                //disable if take out
                EnderFly.enableEnderFly(currentItem, player, false);
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        e.getPlayer().sendMessage("Toggle " + e.isFlying());
        if (e.isCancelled()) {
            return;
        }
        if (!e.isFlying()) {
            Player p = e.getPlayer();
            ItemStack chest = p.getInventory().getChestplate();
            if (p.getGameMode() == GameMode.CREATIVE || chest == null || !chest.hasItemMeta()) {
                return;
            }
            EnderFly.enableEnderFly(chest, p, false); //checks are done inside here
        }
    }

}
