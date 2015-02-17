/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 *
 * @author Max
 */
public class EnderFlyListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (EnderFly.hasEnderFly(p) && p.getInventory().getChestplate().equals(e.getCurrentItem())) {
            switch (e.getClick()) {
                case MIDDLE:
                    EnderFly.toggleEnderFly(p);
                    break;
                default:
                    //Disable it if it is enabled
                    if (EnderFly.isEnderFlyEnabled(p)) {
                        EnderFly.toggleEnderFly(p);
                    }
            }
            //p.updateInventory(); //Update the inv since the durability sometimes doesn't get sent
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || e.isFlying()) {
            return;
        }
        if (EnderFly.hasEnderFly(p) && EnderFly.isEnderFlyEnabled(p)) {
            EnderFly.toggleEnderFly(p);
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (EnderFly.hasEnderFly(p) && EnderFly.isEnderFlyEnabled(p)) {
            EnderFly.stopTask(p);
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (EnderFly.hasEnderFly(p) && EnderFly.isEnderFlyEnabled(p)) {
            EnderFly.startTask(p, new EnderFlyTask(p)); //start the task back up
            p.setAllowFlight(true);
            p.teleport(p.getLocation().add(0, .1, 0)); //port up so flight doesn't get cancled instantly
            p.setFlying(true);
        }
    }
    
    public void onGamemodeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (e.getNewGameMode() == GameMode.CREATIVE || e.getNewGameMode() == GameMode.SPECTATOR) {
            if (EnderFly.hasEnderFly(p) && EnderFly.isEnderFlyEnabled(p)) {
                EnderFly.toggleEnderFly(p); //turn it off
            }
        }       
    }

}
