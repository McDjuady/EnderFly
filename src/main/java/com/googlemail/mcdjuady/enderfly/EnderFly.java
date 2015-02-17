/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlemail.mcdjuady.enderfly;

import com.googlemail.mcdjuady.craftutils.CraftUtils;
import com.googlemail.mcdjuady.craftutils.CustomRecipeManager;
import com.googlemail.mcdjuady.enderfly.crafting.EnderFlyUpgradeResultBuilder;
import com.googlemail.mcdjuady.enderfly.crafting.EnderFlyValidator;
import com.googlemail.mcdjuady.craftutils.recipes.ShapedAdvancedRecipe;
import com.googlemail.mcdjuady.craftutils.recipes.ShapelessAdvancedRecipe;
import com.googlemail.mcdjuady.enderfly.crafting.LoreResultBuilder;
import com.googlemail.mcdjuady.enderfly.crafting.RefillValidator;
import com.googlemail.mcdjuady.enderfly.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    public static final String ENDERFLY_TIME = "Time left: %1$s / %2$s";
    public static final int ENDERFLY_MININGMODIFIER = 18;

    private static Map<Player, Integer> tasks = new HashMap<>();

    public static void registerTask(Player p, int taskId) {
        tasks.put(p, taskId);
    }

    public static int getTask(Player p) {
        return tasks.get(p);
    }

    public static int removeTask(Player p) {
        return tasks.remove(p);
    }

    public static void enableEnderFly(ItemStack enderFly, Player player, boolean enable) {
        if (enderFly == null || !enderFly.hasItemMeta()) {
            System.out.println("Nullfly");
            return;
        }
        List<String> lore = enderFly.getItemMeta().getLore();
        if (lore.size() != 3) {
            System.out.println("Nolore");
            return;
        }
        String info = Util.unhideString(lore.remove(2));
        String name = lore.remove(0);
        if (!info.matches(EnderFly.ENDERFLY_REGEX)) {
            System.out.println("nomatch");
            return;
        }
        String[] numbers = info.replace("[" + EnderFly.ENDERFLY_PREFIX + "]", "").split("-");
        boolean enabled = Integer.valueOf(numbers[0]) == 1;
        int timeLeft = Integer.valueOf(numbers[1]);
        if (timeLeft < 1 && enable) {
            player.sendMessage("Ender Fly is empty! Please refill!");
            return;
        }
        int armorDurability = Integer.valueOf(numbers[2]);
        //if we it's already enabled but we don't have a task  continue
        if (enabled == enable && !(enable && !tasks.containsKey(player))) {
            System.out.println("same");
            return;
        }
        if (enable) {
            //switch the durability
            armorDurability = enderFly.getDurability();
            enderFly.setDurability((short) (enderFly.getType().getMaxDurability() - ONE_SEC * timeLeft));
            //apply flight
            player.setAllowFlight(true);
            //tp .1 up so flight doesn't get canceld imediately
            player.teleport(player.getLocation().add(0, 0.1, 0));
            player.setFlying(true);
            //apply buffs for regular mining
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, ENDERFLY_MININGMODIFIER, true, false), true);
            //start the task
            EnderFlyTask task = new EnderFlyTask(player, enderFly, (short) 1, enderFly.getType().getMaxDurability());
            int taskId = task.runTaskTimer(Bukkit.getPluginManager().getPlugin("EnderFly"), 20 / ONE_SEC, 20 / ONE_SEC).getTaskId();
            registerTask(player, taskId);
            //create the new String
            info = String.format(ENDERFLY_STRING, 1, timeLeft, armorDurability);
            name = "EnderFly - Enabled";
        } else {
            //switch the durability
            timeLeft = (enderFly.getType().getMaxDurability() - enderFly.getDurability()) / ONE_SEC;
            enderFly.setDurability((short) armorDurability);
            //apply flight
            player.setAllowFlight(false);
            //remove buff
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            //reset fall damage
            player.setFallDistance(0);
            //stop the task
            int taskId = removeTask(player);
            Bukkit.getScheduler().cancelTask(taskId);
            //create the new String
            info = String.format(ENDERFLY_STRING, 0, timeLeft, armorDurability);
            name = "EnderFly - Disabled";
        }
        lore.add(0, name);
        lore.add(Util.hideString(info));
        ItemMeta meta = enderFly.getItemMeta();
        meta.setLore(lore);
        enderFly.setItemMeta(meta);
        //player.updateInventory();
    }

    public void onEnable() {
        ItemStack goldEnderFly = new ItemStack(Material.GOLD_CHESTPLATE);
        ItemStack ironEnderFly = new ItemStack(Material.IRON_CHESTPLATE);
        ItemStack chainEnderFly = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemStack diamondEnderFly = new ItemStack(Material.DIAMOND_CHESTPLATE);
        List<String> lore = new ArrayList<String>();
        lore.add("EnderFly - Disabled");
        lore.add("Time Left: 0s / " + MAX_GOLD / ONE_SEC + "s");
        lore.add(Util.hideString(String.format(ENDERFLY_STRING, 0, 0, 0)));

        ItemMeta goldMeta = goldEnderFly.getItemMeta();
        goldMeta.setDisplayName("Gold Ender Fly");
        goldMeta.setLore(lore);
        goldEnderFly.setItemMeta(goldMeta);
        goldEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);
        //goldEnderFly.setDurability(MAX_GOLD);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_IRON / ONE_SEC + "s");

        ItemMeta iroMeta = ironEnderFly.getItemMeta();
        iroMeta.setDisplayName("Iron Ender Fly");
        iroMeta.setLore(lore);
        ironEnderFly.setItemMeta(iroMeta);
        ironEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);
        //ironEnderFly.setDurability(MAX_IRON);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_IRON / ONE_SEC + "s");

        ItemMeta chainMeta = chainEnderFly.getItemMeta();
        chainMeta.setDisplayName("Chain Ender Fly");
        chainMeta.setLore(lore);
        chainEnderFly.setItemMeta(chainMeta);
        chainEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);
        //chainEnderFly.setDurability(MAX_IRON);

        lore.remove(1);
        lore.add(1, "Time Left: 0s / " + MAX_DIAMOND / ONE_SEC + "s");

        ItemMeta diamondMeta = diamondEnderFly.getItemMeta();
        diamondMeta.setDisplayName("Diamond Ender Fly");
        diamondMeta.setLore(lore);
        diamondEnderFly.setItemMeta(diamondMeta);
        diamondEnderFly.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 5);
        //diamondEnderFly.setDurability(MAX_DIAMOND);

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

        this.getCommand("durability").setExecutor(new TestCommand());
        Bukkit.getPluginManager().registerEvents(new EnderFlyListener(), this);
    }

}
