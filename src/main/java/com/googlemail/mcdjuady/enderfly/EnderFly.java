/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import com.googlemail.mcdjuady.craftutils.CraftUtils;
import com.googlemail.mcdjuady.enderfly.crafting.EnderFlyUpgradeResultBuilder;
import com.googlemail.mcdjuady.enderfly.crafting.EnderFlyValidator;
import com.googlemail.mcdjuady.craftutils.recipes.ShapedAdvancedRecipe;
import com.googlemail.mcdjuady.craftutils.recipes.ShapelessAdvancedRecipe;
import com.googlemail.mcdjuady.enderfly.crafting.LoreResultBuilder;
import com.googlemail.mcdjuady.enderfly.crafting.RefillValidator;
import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Max
 */
public class EnderFly extends JavaPlugin {

    public static final int SEC_PER_PEARL = 10;
    public static final short ONE_SEC = 4;
    public static final short MAX_GOLD = 112;
    public static final short MAX_IRON = 240;
    public static final short MAX_DIAMOND = 528;
    //format [EnderFly]Active-TimeLeft-ArmorDurability
    public static final String ENDERFLY_PREFIX = "EnderFly";
    public static final String ENDERFLY_REGEX = "^\\[" + ENDERFLY_PREFIX + "\\]\\d-\\d*-\\d*$";
    public static final String ENDERFLY_STRING = "[" + ENDERFLY_PREFIX + "]%1$s-%2$s-%3$s";
    public static final String ENDERFLY_TIME = "Time left: %1$ss / %2$ss";
    public static final int ENDERFLY_MININGMODIFIER = 18;

    private static Map<Player, BukkitRunnable> tasks = new HashMap<>();

    public static void startTask(Player p, BukkitRunnable task) {
        if (p == null || task == null) {
            return;
        }
        stopTask(p); //cancel any task that is already running
        task.runTaskTimer(EnderFly.getPlugin(EnderFly.class), 20 / ONE_SEC, 20 / ONE_SEC); //decrease durability by 
        tasks.put(p, task);
    }

    public static void stopTask(Player p) {
        if (p == null) {
            return;
        }
        if (tasks.containsKey(p)) {
            BukkitRunnable toCancel = tasks.get(p);
            if (toCancel != null) {
                toCancel.cancel();
            }
        }
    }

    public static boolean hasEnderFly(Player p) {
        if (p == null) {
            return false;
        }
        ItemStack chest = p.getInventory().getChestplate();
        if (chest == null || chest.getType() == Material.AIR || !chest.hasItemMeta()) {
            return false;
        }
        List<String> lore = chest.getItemMeta().getLore();
        if (lore == null || lore.size() != 3) {
            return false;
        }
        String info = Util.unhideString(lore.get(2));
        return info.matches(ENDERFLY_REGEX);
    }

    public static int[] getNumbers(ItemStack enderFly) {
        String info = Util.unhideString(enderFly.getItemMeta().getLore().get(2));
        String[] split = info.replace("[" + ENDERFLY_PREFIX + "]", "").split("-");
        int[] numbers = new int[3];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.valueOf(split[i]);
        }
        return numbers;
    }

    public static boolean isEnderFlyEnabled(Player p) {
        ItemStack enderFly = p.getInventory().getChestplate();
        int[] numbers = getNumbers(enderFly);
        return numbers[0] == 1;
    }

    public static void toggleEnderFly(Player p) {
        ItemStack enderFly = p.getInventory().getChestplate();
        int[] numbers = getNumbers(enderFly);

        int durability = 0;
        if (numbers[0] == 1) {
            stopTask(p);
            durability = numbers[2];
            p.removePotionEffect(PotionEffectType.FAST_DIGGING);
            p.setFallDistance(0);
            p.setAllowFlight(false);
        } else {
            if (numbers[1] <= 0) {
                return; //don't enable if we have no time left
            }
            startTask(p, new EnderFlyTask(p));
            durability = enderFly.getType().getMaxDurability() - numbers[1] * ONE_SEC;
            numbers[2] = enderFly.getDurability();
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, ENDERFLY_MININGMODIFIER), true);
            p.setAllowFlight(true);
            p.teleport(p.getLocation().add(0, .1, 0)); //port up so flight doesn't get cancled instantly
            p.setFlying(true);
        }
        numbers[0] = (numbers[0] + 1) % 2; //yes i had to ( 1 -> 0 | 0 -> 1)
        enderFly.setDurability((short) durability);
        writeLore(enderFly, numbers);
    }

    public static void writeLore(ItemStack enderFly, int[] numbers) {
        List<String> lore = new ArrayList<String>();
        lore.add("EnderFly - " + (numbers[0] == 1 ? "Enabled" : "Disabled"));
        lore.add(String.format(ENDERFLY_TIME, numbers[1], enderFly.getType().getMaxDurability() / ONE_SEC));
        lore.add(Util.hideString(String.format(ENDERFLY_STRING, numbers[0], numbers[1], numbers[2])));
        ItemMeta meta = enderFly.getItemMeta();
        meta.setLore(lore);
        enderFly.setItemMeta(meta);
    }

    public void onEnable() {
        ItemStack goldEnderFly = new ItemStack(Material.GOLD_CHESTPLATE);
        ItemStack ironEnderFly = new ItemStack(Material.IRON_CHESTPLATE);
        ItemStack chainEnderFly = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemStack diamondEnderFly = new ItemStack(Material.DIAMOND_CHESTPLATE);
        List<String> lore = new ArrayList<String>();
        lore.add("EnderFly - Disabled");
        lore.add("Time Left: 0s / " + MAX_GOLD / ONE_SEC);
        lore.add(Util.hideString(String.format(ENDERFLY_STRING, 0, 0, 0)));

        ItemMeta goldMeta = goldEnderFly.getItemMeta();
        goldMeta.setDisplayName("Gold Ender Fly");
        goldMeta.setLore(lore);
        goldEnderFly.setItemMeta(goldMeta);
        goldEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_IRON / ONE_SEC);

        ItemMeta iroMeta = ironEnderFly.getItemMeta();
        iroMeta.setDisplayName("Iron Ender Fly");
        iroMeta.setLore(lore);
        ironEnderFly.setItemMeta(iroMeta);
        ironEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_IRON / ONE_SEC);

        ItemMeta chainMeta = chainEnderFly.getItemMeta();
        chainMeta.setDisplayName("Chain Ender Fly");
        chainMeta.setLore(lore);
        chainEnderFly.setItemMeta(chainMeta);
        chainEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_DIAMOND / ONE_SEC);

        ItemMeta diamondMeta = diamondEnderFly.getItemMeta();
        diamondMeta.setDisplayName("Diamond Ender Fly");
        diamondMeta.setLore(lore);
        diamondEnderFly.setItemMeta(diamondMeta);
        diamondEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);

        //Normal recipes
        //Lore doesn't get copied by normal recipes
        //Use AdvancedRecipes to fix that
        ShapedAdvancedRecipe ironEnderFlyRecipe = new ShapedAdvancedRecipe(ironEnderFly, new LoreResultBuilder(ironEnderFly));
        ironEnderFlyRecipe.shape(" e ", "fcf", " r ").setIngredient('e', Material.EYE_OF_ENDER).setIngredient('f', Material.FEATHER)
                .setIngredient('c', Material.IRON_CHESTPLATE).setIngredient('r', Material.REDSTONE);

        ShapedAdvancedRecipe goldEnderFlyRecipe = new ShapedAdvancedRecipe(goldEnderFly, new LoreResultBuilder(goldEnderFly));
        goldEnderFlyRecipe.shape(" e ", "fcf", " r ").setIngredient('e', Material.EYE_OF_ENDER).setIngredient('f', Material.FEATHER)
                .setIngredient('c', Material.GOLD_CHESTPLATE).setIngredient('r', Material.REDSTONE);

        ShapedAdvancedRecipe chainEnderFlyRecipe = new ShapedAdvancedRecipe(chainEnderFly, new LoreResultBuilder(chainEnderFly));
        chainEnderFlyRecipe.shape(" e ", "fcf", " r ").setIngredient('e', Material.EYE_OF_ENDER).setIngredient('f', Material.FEATHER)
                .setIngredient('c', Material.CHAINMAIL_CHESTPLATE).setIngredient('r', Material.REDSTONE);

        ShapedAdvancedRecipe diamondEnderFlyRecipe = new ShapedAdvancedRecipe(diamondEnderFly, new LoreResultBuilder(diamondEnderFly));
        diamondEnderFlyRecipe.shape(" e ", "fcf", " r ").setIngredient('e', Material.EYE_OF_ENDER).setIngredient('f', Material.FEATHER)
                .setIngredient('c', Material.DIAMOND_CHESTPLATE).setIngredient('r', Material.REDSTONE);

        //Upgrade recipes
        ShapedAdvancedRecipe ironUpgradeRecipe = new ShapedAdvancedRecipe(ironEnderFly, new EnderFlyUpgradeResultBuilder(ironEnderFly));
        ironUpgradeRecipe.shape("d d", "fcf", "ddd").setIngredient('c', Material.GOLD_CHESTPLATE, new EnderFlyValidator()).setIngredient('f', Material.FEATHER)
                .setIngredient('d', Material.IRON_INGOT);

        ShapedAdvancedRecipe diamondUpgradeRecipe1 = new ShapedAdvancedRecipe(diamondEnderFly, new EnderFlyUpgradeResultBuilder(diamondEnderFly));
        diamondUpgradeRecipe1.shape("d d", "fcf", "ddd").setIngredient('c', Material.IRON_CHESTPLATE, new EnderFlyValidator()).setIngredient('f', Material.FEATHER)
                .setIngredient('d', Material.DIAMOND);

        ShapedAdvancedRecipe diamondUpgradeRecipe2 = new ShapedAdvancedRecipe(diamondEnderFly, new EnderFlyUpgradeResultBuilder(diamondEnderFly));
        diamondUpgradeRecipe2.shape("d d", "fcf", "ddd").setIngredient('c', Material.CHAINMAIL_CHESTPLATE, new EnderFlyValidator()).setIngredient('f', Material.FEATHER)
                .setIngredient('d', Material.DIAMOND);

        //refill recipes
        //functions are deprecated but we need to set the raw data so it allows for damaged items
        ShapelessAdvancedRecipe goldRefillRecipe = new ShapelessAdvancedRecipe(goldEnderFly, new RefillValidator());
        goldRefillRecipe.addIngredient(Material.ENDER_PEARL).addIngredient(Material.GOLD_CHESTPLATE, Short.MAX_VALUE);

        ShapelessAdvancedRecipe ironRefillRecipe = new ShapelessAdvancedRecipe(ironEnderFly, new RefillValidator());
        ironRefillRecipe.addIngredient(Material.ENDER_PEARL).addIngredient(Material.IRON_CHESTPLATE, Short.MAX_VALUE);

        ShapelessAdvancedRecipe chainRefillRecipe = new ShapelessAdvancedRecipe(chainEnderFly, new RefillValidator());
        chainRefillRecipe.addIngredient(Material.ENDER_PEARL).addIngredient(Material.CHAINMAIL_CHESTPLATE, Short.MAX_VALUE);

        ShapelessAdvancedRecipe diamondRefillRecipe = new ShapelessAdvancedRecipe(diamondEnderFly, new RefillValidator());
        diamondRefillRecipe.addIngredient(Material.ENDER_PEARL).addIngredient(Material.DIAMOND_CHESTPLATE, Short.MAX_VALUE);

        CraftUtils.getRecipeManager().addRecipe(goldEnderFlyRecipe);
        CraftUtils.getRecipeManager().addRecipe(ironEnderFlyRecipe);
        CraftUtils.getRecipeManager().addRecipe(chainEnderFlyRecipe);
        CraftUtils.getRecipeManager().addRecipe(diamondEnderFlyRecipe);
        CraftUtils.getRecipeManager().addRecipe(ironUpgradeRecipe);
        CraftUtils.getRecipeManager().addRecipe(diamondUpgradeRecipe1);
        CraftUtils.getRecipeManager().addRecipe(diamondUpgradeRecipe2);

        CraftUtils.getRecipeManager().addRecipe(goldRefillRecipe);
        CraftUtils.getRecipeManager().addRecipe(ironRefillRecipe);
        CraftUtils.getRecipeManager().addRecipe(chainRefillRecipe);
        CraftUtils.getRecipeManager().addRecipe(diamondRefillRecipe);

        Bukkit.getPluginManager().registerEvents(new EnderFlyListener(), this);

        //if we reload we wan't to check if some players wer enabled
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (hasEnderFly(p) && isEnderFlyEnabled(p)) {
                startTask(p, new EnderFlyTask(p));
            }
        }
    }

}
